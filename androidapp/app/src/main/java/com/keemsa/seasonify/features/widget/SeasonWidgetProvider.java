package com.keemsa.seasonify.features.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RemoteViews;

import com.keemsa.seasonify.R;
import com.keemsa.seasonify.features.start.MainActivity;
import com.keemsa.seasonify.features.start.MainPresenter;

/**
 * Created by sebastian on 4/6/17.
 */

public class SeasonWidgetProvider extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        context.startService(new Intent(context, SeasonWidgetIntentService.class));
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        context.startService(new Intent(context, SeasonWidgetIntentService.class));
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if(MainPresenter.ACTION_DATA_UPDATED.equals(intent.getAction())){
            context.startService(new Intent(context, SeasonWidgetIntentService.class));
        }
    }
}
