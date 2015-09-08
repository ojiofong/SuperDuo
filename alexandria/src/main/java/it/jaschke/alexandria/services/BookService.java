package it.jaschke.alexandria.services;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.IntDef;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.net.HttpURLConnection;
import java.net.URL;

import it.jaschke.alexandria.AddBook;
import it.jaschke.alexandria.MainActivity;
import it.jaschke.alexandria.R;
import it.jaschke.alexandria.api.Util;
import it.jaschke.alexandria.data.AlexandriaContract;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 */
public class BookService extends IntentService {

    private final String LOG_TAG = BookService.class.getSimpleName();

    public static final String FETCH_BOOK = "it.jaschke.alexandria.services.action.FETCH_BOOK";
    public static final String DELETE_BOOK = "it.jaschke.alexandria.services.action.DELETE_BOOK";

    public static final String EAN = "it.jaschke.alexandria.services.extra.EAN";

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({BOOK_STATUS_OK, BOOK_STATUS_SERVER_DOWN, BOOK_STATUS_SERVER_INVALID, BOOK_STATUS_UNKNOWN, BOOK_STATUS_NO_BOOKS})
    public @interface BookStatus {
    }

    public static final int BOOK_STATUS_OK = 0;
    public static final int BOOK_STATUS_SERVER_DOWN = 1;
    public static final int BOOK_STATUS_SERVER_INVALID = 2;
    public static final int BOOK_STATUS_UNKNOWN = 3;
    public static final int BOOK_STATUS_NO_BOOKS = 4;

//    @BookStatus
//    public abstract int getBookStatus();
//    public abstract void setBookStatus(int bookStatus);

    public BookService() {
        super("Alexandria");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (FETCH_BOOK.equals(action)) {
                final String ean = intent.getStringExtra(EAN);
                fetchBook(ean);
            } else if (DELETE_BOOK.equals(action)) {
                final String ean = intent.getStringExtra(EAN);
                deleteBook(ean);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void deleteBook(String ean) {
        if (ean != null) {
            getContentResolver().delete(AlexandriaContract.BookEntry.buildBookUri(Long.parseLong(ean)), null, null);
        }
    }

    /**
     * Handle action fetchBook in the provided background thread with the provided
     * parameters.
     */
    private void fetchBook(String ean) {

        if (ean.length() != 13) {
            return;
        }

        // Enure that cursor is closed in finally block
        Cursor bookEntry = null;
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String bookJsonString = null;

        try {

            bookEntry = getContentResolver().query(
                    AlexandriaContract.BookEntry.buildBookUri(Long.parseLong(ean)),
                    null, // leaving "columns" null just returns all the columns.
                    null, // cols for "where" clause
                    null, // values for "where" clause
                    null  // sort order
            );

            if (bookEntry.getCount() > 0) {
                bookEntry.close();
                return;
            }


            final String FORECAST_BASE_URL = "https://www.googleapis.com/books/v1/volumes?";
            final String QUERY_PARAM = "q";

            final String ISBN_PARAM = "isbn:" + ean;

            Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                    .appendQueryParameter(QUERY_PARAM, ISBN_PARAM)
                    .build();

            URL url = new URL(builtUri.toString());

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuilder builder = new StringBuilder();
            if (inputStream == null) {
                return;
            }

            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
                builder.append("\n");
            }

            if (builder.length() == 0) {
                // STATUS - SERVER DOWN
                setBookStatus(BOOK_STATUS_SERVER_DOWN);
                return;
            }
            bookJsonString = builder.toString();
        } catch (IOException e) {
            // STATUS - SERVER DOWN
            setBookStatus(BOOK_STATUS_SERVER_DOWN);
            Log.e(LOG_TAG, "Error ", e);
        } finally {
            if (bookEntry != null)
                bookEntry.close();

            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }

        }

        final String ITEMS = "items";

        final String VOLUME_INFO = "volumeInfo";

        final String TITLE = "title";
        final String SUBTITLE = "subtitle";
        final String AUTHORS = "authors";
        final String DESC = "description";
        final String CATEGORIES = "categories";
        final String IMG_URL_PATH = "imageLinks";
        final String IMG_URL = "thumbnail";

        // Prevents NullPointerException crash when adding book if there's no network
        if (bookJsonString == null) {

            if (!Util.isInternetConnected(getApplicationContext()))
                setBookStatus(BOOK_STATUS_SERVER_DOWN);

            return;
        }

        try {
            JSONObject bookJson = new JSONObject(bookJsonString);
            JSONArray bookArray;
            if (bookJson.has(ITEMS)) {
                bookArray = bookJson.getJSONArray(ITEMS);
            } else {
//                Intent messageIntent = new Intent(MainActivity.MESSAGE_EVENT);
//                messageIntent.putExtra(MainActivity.MESSAGE_KEY, getResources().getString(R.string.not_found));
//                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(messageIntent);
                setBookStatus(BOOK_STATUS_NO_BOOKS);
                return;
            }

            JSONObject bookInfo = ((JSONObject) bookArray.get(0)).getJSONObject(VOLUME_INFO);

            String title = bookInfo.getString(TITLE);

            String subtitle = "";
            if (bookInfo.has(SUBTITLE)) {
                subtitle = bookInfo.getString(SUBTITLE);
            }

            String desc = "";
            if (bookInfo.has(DESC)) {
                desc = bookInfo.getString(DESC);
            }

            String imgUrl = "";
            if (bookInfo.has(IMG_URL_PATH) && bookInfo.getJSONObject(IMG_URL_PATH).has(IMG_URL)) {
                imgUrl = bookInfo.getJSONObject(IMG_URL_PATH).getString(IMG_URL);
            }

            writeBackBook(ean, title, subtitle, desc, imgUrl);

            if (bookInfo.has(AUTHORS)) {
                writeBackAuthors(ean, bookInfo.getJSONArray(AUTHORS));
            }
            if (bookInfo.has(CATEGORIES)) {
                writeBackCategories(ean, bookInfo.getJSONArray(CATEGORIES));
            }


            // STATUS - SERVER OKAY
            setBookStatus(BOOK_STATUS_OK);

        } catch (JSONException e) {
            // STATUS - SERVER iNVALID
            setBookStatus(BOOK_STATUS_SERVER_INVALID);
            Log.e(LOG_TAG, "Error ", e);
        }
    }

    private void writeBackBook(String ean, String title, String subtitle, String desc, String imgUrl) {
        ContentValues values = new ContentValues();
        values.put(AlexandriaContract.BookEntry._ID, ean);
        values.put(AlexandriaContract.BookEntry.TITLE, title);
        values.put(AlexandriaContract.BookEntry.IMAGE_URL, imgUrl);
        values.put(AlexandriaContract.BookEntry.SUBTITLE, subtitle);
        values.put(AlexandriaContract.BookEntry.DESC, desc);
        getContentResolver().insert(AlexandriaContract.BookEntry.CONTENT_URI, values);
    }

    private void writeBackAuthors(String ean, JSONArray jsonArray) throws JSONException {
        ContentValues values = new ContentValues();
        for (int i = 0; i < jsonArray.length(); i++) {
            values.put(AlexandriaContract.AuthorEntry._ID, ean);
            values.put(AlexandriaContract.AuthorEntry.AUTHOR, jsonArray.getString(i));
            getContentResolver().insert(AlexandriaContract.AuthorEntry.CONTENT_URI, values);
            values = new ContentValues();
        }
    }

    private void writeBackCategories(String ean, JSONArray jsonArray) throws JSONException {
        ContentValues values = new ContentValues();
        for (int i = 0; i < jsonArray.length(); i++) {
            values.put(AlexandriaContract.CategoryEntry._ID, ean);
            values.put(AlexandriaContract.CategoryEntry.CATEGORY, jsonArray.getString(i));
            getContentResolver().insert(AlexandriaContract.CategoryEntry.CONTENT_URI, values);
            values = new ContentValues();
        }
    }

    private void setBookStatus(@BookStatus int status) {

        PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .edit().putInt(getString(R.string.pref_book_status_key), status).commit();
    }
}