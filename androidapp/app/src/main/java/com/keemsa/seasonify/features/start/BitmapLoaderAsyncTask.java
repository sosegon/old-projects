package com.keemsa.seasonify.features.start;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by sebastian on 3/10/17.
 */

public class BitmapLoaderAsyncTask extends AsyncTask<Uri, Void, Bitmap> {

  interface BitmapLoaderAsyncTaskReceiver {
    void classify(Bitmap bitmap);
  }

  private BitmapLoaderAsyncTaskReceiver mReceiver;
  private Context mContext;
  private int mImageSize;

  public BitmapLoaderAsyncTask(Context context, BitmapLoaderAsyncTaskReceiver receiver, int imageSize) {
    super();
    mContext = context;
    mReceiver = receiver;
    mImageSize = imageSize;
  }

  @Override
  protected Bitmap doInBackground(Uri... params) {
    try{
      return getBitmap(params[0]);

    } catch (IOException e){
      return null;
    }
  }

  @Override
  protected void onPostExecute(Bitmap bitmap) {
    mReceiver.classify(bitmap);
  }

  public Bitmap getBitmap(Uri uri) throws IOException {
    InputStream input = mContext.getContentResolver().openInputStream(uri);
    Bitmap bitmap = BitmapFactory.decodeStream(input, null, null);
    bitmap = Bitmap.createScaledBitmap(bitmap, mImageSize, mImageSize, false);
    input.close();
    return bitmap;
  }

}
