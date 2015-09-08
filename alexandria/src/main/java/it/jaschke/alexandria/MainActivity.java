package it.jaschke.alexandria;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import it.jaschke.alexandria.api.Callback;


public class MainActivity extends AppCompatActivity implements NavigationDrawerFragment.NavigationDrawerCallbacks, Callback, BookDetail.BookDetailCallback {

    private static String TAG = MainActivity.class.getSimpleName();
    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment navigationDrawerFragment;
    public Toolbar toolbar;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence title;
    public static boolean IS_TABLET = false;
    private BroadcastReceiver messageReciever;

    public static final String MESSAGE_EVENT = "MESSAGE_EVENT";
    public static final String MESSAGE_KEY = "MESSAGE_EXTRA";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        IS_TABLET = isTablet();
//        if (IS_TABLET) {
//            setContentView(R.layout.activity_main_tablet);
//        } else {
//            setContentView(R.layout.activity_main);
//        }

//        messageReciever = new MessageReciever();
 //       IntentFilter filter = new IntentFilter(MESSAGE_EVENT);
        //  LocalBroadcastManager.getInstance(this).registerReceiver(messageReciever, filter);

        initializeToolbar();

        navigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        title = getTitle();

        // Set up the drawer.
        navigationDrawerFragment.setUp(R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        receiveScanIntent();

    }


    private void initializeToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {

        switch (position) {
            default:
            case 0:
                goToFragment(new ListOfBooks(), R.string.books);
                break;
            case 1:
                goToFragment(new AddBook(), R.string.scan);
                break;
            case 2:
                goToFragment(new About(), R.string.about);
                break;

        }

    }

    private void goToFragment(Fragment nextFragment, int title) {

        String mTitle = getString(title);

        isAddedToBackstack(mTitle);


        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.container, nextFragment, mTitle);
        ft.addToBackStack(mTitle);
        ft.commit();

    }

    public void setTitle(int titleId) {
        title = getString(titleId);
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            //  actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setTitle(title);

        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!navigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        // LocalBroadcastManager.getInstance(this).unregisterReceiver(messageReciever);
        super.onDestroy();
    }

    @Override
    public void onItemSelected(String ean) {
        Bundle args = new Bundle();
        args.putString(BookDetail.EAN_KEY, ean);

        BookDetail fragment = new BookDetail();
        fragment.setArguments(args);


        if (isDualPane()) {
            findViewById(R.id.emptyFrameLayoutText).setVisibility(View.GONE); // Hide TextView
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.right_container, fragment)
                    .commit();
        } else {
            Intent intent = new Intent(getApplicationContext(), DetailActivity.class);
            intent.putExtra(BookDetail.EAN_KEY, ean);
            startActivity(intent);
        }

    }

    @Override
    public void onScanButtonClicked() {
        startActivity(new Intent(MainActivity.this, ScannerActivity.class));
        finish();
    }

    private boolean isDualPane() {
        return findViewById(R.id.right_container) != null;
    }

    @Override
    public void onDeleteBook() {
        findViewById(R.id.emptyFrameLayoutText).setVisibility(View.VISIBLE); // Hide TextView

    }

//    private class MessageReciever extends BroadcastReceiver {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            if (intent.getStringExtra(MESSAGE_KEY) != null) {
//                String msg = intent.getStringExtra(MESSAGE_KEY);
//                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show();
//                updateAddBookStatus(msg);
//            }
//        }
//    }

//    private void updateAddBookStatus(String msg) {
//        final AddBook addBook = (AddBook) getSupportFragmentManager().findFragmentByTag(getString(R.string.scan));
//        if (addBook != null && addBook.isAdded()) {
//            addBook.updateStatus(msg);
//        } else Toast.makeText(MainActivity.this, "addbook is null", Toast.LENGTH_LONG).show();
//    }

    public void goBack(View view) {
        if (!IS_TABLET)
            getSupportFragmentManager().popBackStack();
    }

    private boolean isTablet() {
        return (getApplicationContext().getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    @Override
    public void onBackPressed() {
        if (navigationDrawerFragment.isDrawerOpen()) {
            navigationDrawerFragment.closeDrawer();
            return;
        }

        if (getSupportFragmentManager().getBackStackEntryCount() < 2) {
            finish();
        }
        super.onBackPressed();
    }


    void receiveScanIntent() {
        String isbn = getIntent().getStringExtra(AddBook.SCAN_CONTENTS);
        String format = getIntent().getStringExtra(AddBook.SCAN_FORMAT);
        if (isbn != null && format != null) {
            Bundle args = new Bundle();
            args.putString(AddBook.SCAN_CONTENTS, isbn);
            args.putString(AddBook.SCAN_FORMAT, format);
            AddBook addBook = new AddBook();
            addBook.setArguments(args);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, addBook)
                    .addToBackStack((String) title)
                    .commit();

            if (navigationDrawerFragment.isDrawerOpen())
                navigationDrawerFragment.closeDrawer();
        }
    }

    private boolean isAddedToBackstack(String name) {
        FragmentManager fm = getSupportFragmentManager();
        String entry;
        for (int i = 0; i < fm.getBackStackEntryCount(); i++) {
            entry = fm.getBackStackEntryAt(i).getName();
            if (entry != null && entry.equals(name)) {
                Log.d(TAG, "Found backstack -> " + entry);
                getSupportFragmentManager().popBackStack(entry, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                return true;
            }
        }

        Log.d(TAG, "Not Found backstack -> " + name);
        return false;
    }

    private void closeDrawer() {
        if (navigationDrawerFragment.isDrawerOpen())
            navigationDrawerFragment.closeDrawer();
    }

}