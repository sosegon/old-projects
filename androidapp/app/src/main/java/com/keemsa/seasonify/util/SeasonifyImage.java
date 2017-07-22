package com.keemsa.seasonify.util;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.provider.MediaStore;

import com.keemsa.seasonify.R;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import timber.log.Timber;

/**
 * Created by sebastian on 4/26/17.
 */

public class SeasonifyImage {

    public static File createImageFile(Context context) throws IOException {

        // The directory for the picture
        final String appName = context.getResources().getString(R.string.app_name);
        File storageDir = context.getExternalFilesDir(appName);

        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        return image;
    }

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

    public static Bitmap retrieveFromFile(String path, int imageWidth, int imageHeight) {
        try {
            InputStream input = new FileInputStream(path);
            BitmapFactory.Options bounds = new BitmapFactory.Options();
            Bitmap bitmap = BitmapFactory.decodeStream(input, null, bounds);

            int height = bitmap.getHeight();
            int width = bitmap.getWidth();

            Matrix matrix = new Matrix();
            matrix.postScale(imageWidth / width, imageHeight /height);
            Bitmap scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, false);

            input.close();
            return scaledBitmap;
        } catch(FileNotFoundException e) {
            Timber.e(e.getMessage());
        } catch (IOException e) {
            Timber.e(e.getMessage());
        }
        return null;
    }

    public static Bitmap retrieveFromFileToClassify(String path, int imageSize) {
        try {
            InputStream input = new FileInputStream(path);
            BitmapFactory.Options bounds = new BitmapFactory.Options();
            Bitmap bitmap = BitmapFactory.decodeStream(input, null, bounds);

            int rotationAngle = getCameraPhotoOrientation(path);
            Timber.e("Rotate angle: " + rotationAngle);
            Matrix matrix = new Matrix();
            matrix.postRotate(rotationAngle, (float) bitmap.getWidth() / 2, (float) bitmap.getHeight() / 2);

            // passed to the classifier
            Bitmap scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0, imageSize, imageSize, matrix, false);

            // saved to storage
            Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bounds.outWidth, bounds.outHeight, matrix, false);
            SeasonifyImage.saveImage(rotatedBitmap, path);

            input.close();

            return scaledBitmap;

        } catch(FileNotFoundException e) {
            Timber.e(e.getMessage());
        } catch (IOException e) {
            Timber.e(e.getMessage());
        }
        return null;
    }

    private static int getCameraPhotoOrientation(String path) {
        int rotate = 0;
        try {

            ExifInterface exif = new ExifInterface(path);
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED);
            switch (orientation) {
                case ExifInterface.ORIENTATION_NORMAL:
                    rotate = 0;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return rotate;
    }

    public static int[] sortColors(int[] colors) {
        int[] iComb = Arrays.copyOf(colors, colors.length); // copy to avoid problems in the palette
        Arrays.sort(iComb); // sort so when converting to string combinations are not repeated
        return iComb;
    }

    public static String colorsAsString(int[] colors) {
        String sComb = "";
        for(int color : colors) {
            sComb += String.valueOf(color) + ";";
        }
        sComb = sComb.substring(0, sComb.length() - 1);

        return sComb;
    }
}
