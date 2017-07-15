package com.keemsa.seasonify.features.widget;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.RemoteViews;

import com.keemsa.seasonify.R;
import com.keemsa.seasonify.SeasonifyApplication;
import com.keemsa.seasonify.data.DataManager;
import com.keemsa.seasonify.features.start.MainActivity;
import com.keemsa.seasonify.injection.component.DaggerServiceComponent;
import com.keemsa.seasonify.injection.component.ServiceComponent;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.inject.Inject;

import timber.log.Timber;

/**
 * Created by sebastian on 4/6/17.
 */

public class SeasonWidgetIntentService extends IntentService {

    private ServiceComponent mServiceComponent;

    @Inject
    DataManager mDataManager;

    public SeasonWidgetIntentService() {
        super(SeasonWidgetIntentService.class.getName());
    }

    public ServiceComponent getServiceComponent() {
        if(mServiceComponent == null) {
            mServiceComponent = DaggerServiceComponent.builder()
                                .applicationComponent(SeasonifyApplication.get(this).getComponent())
                                .build();
        }

        return mServiceComponent;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        getServiceComponent().inject(this);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String predicton = mDataManager.getPreferencesHelper().retrievePrediction();
        String path = mDataManager.getPreferencesHelper().retrievePhotoPath();

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
                Timber.e(e.getMessage());
            }

            Intent launchIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, launchIntent, 0);
            views.setOnClickPendingIntent(R.id.ll_w, pendingIntent);

            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }
}
