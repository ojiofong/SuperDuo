package barqsoft.footballscores;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by oofong25 on 9/26/15.
 */
public abstract class DbUtils {


    public static final int COL_HOME = 3;
    public static final int COL_AWAY = 4;
    public static final int COL_HOME_GOALS = 6;
    public static final int COL_AWAY_GOALS = 7;
    public static final int COL_DATE = 1;
    public static final int COL_LEAGUE = 5;
    public static final int COL_MATCHDAY = 9;
    public static final int COL_ID = 8;
    public static final int COL_MATCHTIME = 2;
    public double detail_match_id = 0;


    public static Cursor getCursorForToday(Context context) {
        Uri uri = DatabaseContract.scores_table.buildScoreWithDate();
        return context.getContentResolver().query(uri, null, null, new String[]{getTodaysDate()}, null);
    }

    public static boolean cursorHasContent(Cursor cursor) {
        return cursor != null && cursor.getCount() > 0;
    }

    public static void closeCursor(Cursor cursor) {
        if (cursor != null)
            cursor.close();
    }


    private static String getTodaysDate() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        return simpleDateFormat.format(new Date());
    }


}
