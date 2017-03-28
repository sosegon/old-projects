package com.keemsa.seasonify.features.start;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by sebastian on 3/10/17.
 */

public class BitmapLoaderAsyncTask extends AsyncTask<String, Void, Bitmap> {

  interface BitmapLoaderAsyncTaskReceiver {
    void classify(Context context, String path, Bitmap bitmap);
  }

  private BitmapLoaderAsyncTaskReceiver mReceiver;
  private Context mContext;
  private int mImageSize;
  private String mPath;

  public BitmapLoaderAsyncTask(Context context, BitmapLoaderAsyncTaskReceiver receiver, int imageSize) {
    super();
    mContext = context;
    mReceiver = receiver;
    mImageSize = imageSize;
  }

  @Override
  protected Bitmap doInBackground(String... params) {
    try{
      mPath = params[0];
      return getBitmap(mPath);

    } catch (IOException e){
      return null;
    }
  }

  @Override
  protected void onPostExecute(Bitmap bitmap) {
    mReceiver.classify(mContext, mPath, bitmap);
  }

  public Bitmap getBitmap(String path) throws IOException {
    InputStream input = new FileInputStream(path);
    Bitmap bitmap = BitmapFactory.decodeStream(input, null, null);
    bitmap = Bitmap.createScaledBitmap(bitmap, mImageSize, mImageSize, false);
    input.close();
    return bitmap;
  }

}
