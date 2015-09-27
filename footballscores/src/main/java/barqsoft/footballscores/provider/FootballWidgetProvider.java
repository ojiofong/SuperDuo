package barqsoft.footballscores.provider;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.widget.RemoteViews;

import barqsoft.footballscores.DbUtils;
import barqsoft.footballscores.MainActivity;
import barqsoft.footballscores.R;
import barqsoft.footballscores.Utilies;
import barqsoft.footballscores.service.myFetchService;

/**
 * Created by oofong25 on 9/26/15.
 */
public class FootballWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        final int N = appWidgetIds.length;
        for (int i = 0; i < N; i++) {
            int appWidgetId = appWidgetIds[i];

            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.scores_list_item);
            updateWigetView(context, views);

            Intent clickItent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, clickItent, 0);
            views.setOnClickPendingIntent(R.id.scores_list_item_id, pendingIntent);

            appWidgetManager.updateAppWidget(appWidgetId, views);

        }

    }


    private void updateWigetView(Context context, RemoteViews views) {
//        views = new RemoteViews(context.getPackageName(), R.layout.scores_list_item);
        Cursor cursor = DbUtils.getCursorForToday(context);
        if (DbUtils.cursorHasContent(cursor) && cursor.moveToFirst()) {

            String home = cursor.getString(DbUtils.COL_HOME);
            String away = cursor.getString(DbUtils.COL_AWAY);
            String time = cursor.getString(DbUtils.COL_MATCHTIME);
            String score = Utilies.getScores(cursor.getInt(DbUtils.COL_HOME_GOALS), cursor.getInt(DbUtils.COL_AWAY_GOALS));
            int iconHome = Utilies.getTeamCrestByTeamName(cursor.getString(DbUtils.COL_HOME));
            int iconAway = Utilies.getTeamCrestByTeamName(cursor.getString(DbUtils.COL_AWAY));

            views.setTextViewText(R.id.home_name, home);
            views.setTextViewText(R.id.away_name, away);
            views.setImageViewResource(R.id.home_crest, iconHome);
            views.setImageViewResource(R.id.away_crest, iconAway);
            views.setTextViewText(R.id.data_textview, time);
            views.setTextViewText(R.id.score_textview, score);


        }

        DbUtils.closeCursor(cursor);

    }

    private void update_scores(Context context) {
        Intent service_start = new Intent(context, myFetchService.class);
        context.startService(service_start);
    }
}
