package com.keemsa.seasonify.util;

import android.content.ContentValues;
import android.content.Context;
import android.provider.MediaStore;

/**
 * Created by sebastian on 4/26/17.
 */

public class SeasonifyImage {

    public static void addImageToGallery(final String filePath, final Context context) {

        ContentValues values = new ContentValues();

        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.MediaColumns.DATA, filePath);

        context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }
}
