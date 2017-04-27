package com.keemsa.seasonify.util;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by sebastian on 4/26/17.
 */

public class SeasonifyImage {

    private static final String LOG_TAG = SeasonifyImage.class.getSimpleName();

    public static void addImageToGallery(final Context context, final String filePath) {

        ContentValues values = new ContentValues();

        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.MediaColumns.DATA, filePath);

        context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }

    public static void saveImage(Bitmap bitmapImage, String path) {
        try {
            FileOutputStream out = new FileOutputStream(path);
            bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error saving face bitmap: " + e.getMessage());
        }
    }

    public static void saveImage(Mat matImage, String path) {
        Imgproc.cvtColor(matImage, matImage, Imgproc.COLOR_RGB2BGR);
        Bitmap bmp = Bitmap.createBitmap(matImage.cols(), matImage.rows(), Bitmap.Config.RGB_565);
        Utils.matToBitmap(matImage, bmp);

        saveImage(bmp, path);
    }
}
