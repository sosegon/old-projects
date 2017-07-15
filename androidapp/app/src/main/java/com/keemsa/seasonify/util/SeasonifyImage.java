package com.keemsa.seasonify.util;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.provider.MediaStore;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.FileOutputStream;
import java.io.IOException;

import timber.log.Timber;

/**
 * Created by sebastian on 4/26/17.
 */

public class SeasonifyImage {

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
            Timber.e("Error saving face bitmap: " + e.getMessage());
        }
    }

    public static void saveImage(Mat matImage, String path) {
        Imgproc.cvtColor(matImage, matImage, Imgproc.COLOR_RGB2BGR);
        Bitmap bmp = Bitmap.createBitmap(matImage.cols(), matImage.rows(), Bitmap.Config.RGB_565);
        Utils.matToBitmap(matImage, bmp);

        saveImage(bmp, path);
    }
}
