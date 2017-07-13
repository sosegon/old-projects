package com.keemsa.seasonify.features.widget;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;

import com.keemsa.seasonify.R;
import com.keemsa.seasonify.features.start.MainActivity;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by sebastian on 4/6/17.
 */

public class SeasonWidgetIntentService extends IntentService {

    public final static String LOG_TAG = SeasonWidgetIntentService.class.getSimpleName();

    public SeasonWidgetIntentService() {
        super(SeasonWidgetIntentService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);

        String predicton = pref.getString(getResources().getString(R.string.prf_prediction), "");
        String path = pref.getString(getResources().getString(R.string.prf_photo_path), "");

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this, SeasonWidgetProvider.class));

        for(int appWidgetId : appWidgetIds){
            int layoutId = R.layout.widget_season;

            RemoteViews views = new RemoteViews(getPackageName(), layoutId);
            views.setTextViewText(R.id.txt_w_season, predicton);

            try {
                InputStream input = new FileInputStream(path);
                BitmapFactory.Options bounds = new BitmapFactory.Options();
                Bitmap bitmap = BitmapFactory.decodeStream(input, null, bounds);

                views.setImageViewBitmap(R.id.imv_w_face, bitmap);
            } catch (IOException e) {
                Log.e(LOG_TAG, e.getMessage());
            }

            Intent launchIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, launchIntent, 0);
            views.setOnClickPendingIntent(R.id.ll_w, pendingIntent);

            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }
}
