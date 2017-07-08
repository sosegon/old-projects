package com.keemsa.seasonify.features.start;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.AsyncTask;
import android.util.Log;

import com.keemsa.seasonify.util.SeasonifyImage;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by sebastian on 3/10/17.
 */

public class BitmapLoaderAsyncTask extends AsyncTask<String, Void, Bitmap> {

  interface BitmapLoaderAsyncTaskReceiver {
    void classify(Context context, String path, Bitmap bitmap);
    void load(Context context, Bitmap bitmap);
  }

  public enum TASK_TYPE {
    LOAD, CLASSIFY;

    public static TASK_TYPE indexOf(int index) {
      switch (index) {
        case 0:
          return CLASSIFY;
        case 1:
          return LOAD;
      }
      return LOAD;
    }
  }


  private BitmapLoaderAsyncTaskReceiver mReceiver;
  private Context mContext;
  private int mImageSize;
  private String mPath;
  private TASK_TYPE mTaskType;

  public BitmapLoaderAsyncTask(Context context, BitmapLoaderAsyncTaskReceiver receiver, int imageSize, int taskType) {
    super();
    mContext = context;
    mReceiver = receiver;
    mImageSize = imageSize;
    mTaskType = TASK_TYPE.indexOf(taskType);
  }

  @Override
  protected Bitmap doInBackground(String... params) {
    try{
      mPath = params[0];
      switch (mTaskType) {
        case LOAD:
          return getBitmapLoad(mPath);
        case CLASSIFY:
          return getBitmapClassify(mPath);
      }
      return null;

    } catch (IOException e){
      return null;
    }
  }

  @Override
  protected void onPostExecute(Bitmap bitmap) {
    switch (mTaskType) {
      case LOAD:
        mReceiver.load(mContext, bitmap);
        break;
      case CLASSIFY:
        mReceiver.classify(mContext, mPath, bitmap);
    }
  }

  public Bitmap getBitmapClassify(String path) throws IOException {
    InputStream input = new FileInputStream(path);
    BitmapFactory.Options bounds = new BitmapFactory.Options();
    Bitmap bitmap = BitmapFactory.decodeStream(input, null, bounds);

    int rotationAngle = getCameraPhotoOrientation(mContext, mPath);
    Log.e("ASync", "Rotate angle: " + rotationAngle);
    Matrix matrix = new Matrix();
    matrix.postRotate(rotationAngle, (float) bitmap.getWidth() / 2, (float) bitmap.getHeight() / 2);

    // passed to the classifier
    Bitmap scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0, mImageSize, mImageSize, matrix, false);

    // saved to storage
    Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bounds.outWidth, bounds.outHeight, matrix, false);
    SeasonifyImage.saveImage(rotatedBitmap, mPath);

    input.close();

    return scaledBitmap;
  }

  public Bitmap getBitmapLoad(String path) throws IOException {
    InputStream input = new FileInputStream(path);
    BitmapFactory.Options bounds = new BitmapFactory.Options();
    Bitmap bitmap = BitmapFactory.decodeStream(input, null, bounds);

    int height = bitmap.getHeight();
    int width = bitmap.getWidth();

    Matrix matrix = new Matrix();
    matrix.postScale(mImageSize / width, mImageSize /height);
    Bitmap scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, false);

    input.close();
    return scaledBitmap;
  }

  // from http://stackoverflow.com/a/28404421
  private int getCameraPhotoOrientation(Context context, String path) {
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

}
