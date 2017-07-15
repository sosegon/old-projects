package com.keemsa.seasonify.processing;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.keemsa.seasonify.BuildConfig;
import com.keemsa.seasonify.R;
import com.keemsa.seasonify.injection.ApplicationContext;
import com.keemsa.seasonify.util.Cluster;
import com.keemsa.seasonify.util.SeasonifyImage;
import com.keemsa.seasonify.util.SeasonifyUtils;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by sebastian on 14/07/17.
 */

@Singleton
public class ImageProcessingHelper {

    private static final String LOG_TAG = ImageProcessingHelper.class.getSimpleName();

    private final Context mContext;
    private CascadeClassifier mJavaDetector;
    private File mCascadeFile;
    private BaseLoaderCallback mBaseLoaderCallback;

    @Inject
    public ImageProcessingHelper(@ApplicationContext Context context) {
        mContext = context;
        initBaseLoaderCallback();
    }

    public Bitmap detectFace(String path, int frameSize) {

        if (!OpenCVLoader.initDebug()) {
            Log.d(LOG_TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, mContext, mBaseLoaderCallback);
        } else {
            Log.d(LOG_TAG, "OpenCV library found inside package. Using it!");
            mBaseLoaderCallback.onManagerConnected(BaseLoaderCallback.SUCCESS);
        }

        Mat imgMAT = Imgcodecs.imread(path, Imgcodecs.CV_LOAD_IMAGE_COLOR);
        MatOfRect faces = new MatOfRect();

        if (mJavaDetector != null) {
            mJavaDetector.detectMultiScale(imgMAT, faces);
        }

        Rect[] facesArray = faces.toArray();
        if (facesArray.length > 0) {
            Mat faceMAT = imgMAT.submat(facesArray[0]);
            Mat nFaceMAT = faceMAT.clone();

            Imgproc.resize(faceMAT, nFaceMAT, new Size(frameSize, frameSize)); // resize to pass to mClassifier
            Imgproc.cvtColor(nFaceMAT, nFaceMAT, Imgproc.COLOR_RGB2BGR); // bitmap is BGR

            if(BuildConfig.DEBUG){
                List<Mat> clusters = Cluster.cluster(nFaceMAT, 3);
                String baseName = SeasonifyUtils.getFileNameNoExtension(path);

                for(int i = 0; i < clusters.size(); i++){
                    String clusterName = baseName + "k_" + i + ".jpg";
                    SeasonifyImage.saveImage(clusters.get(i), clusterName);
                    SeasonifyImage.addImageToGallery(mContext, clusterName);
                }
            }

            Bitmap bmp = Bitmap.createBitmap(nFaceMAT.cols(), nFaceMAT.rows(), Bitmap.Config.RGB_565);
            Utils.matToBitmap(nFaceMAT, bmp);

            return bmp;
        }

        return null;
    }

    // based on https://github.com/joaopedronardari/OpenCV-AndroidSamples/blob/master/app/src/main/java/com/jnardari/opencv_androidsamples/activities/FaceDetectionActivity.java#L62
    private void initBaseLoaderCallback() {
        mBaseLoaderCallback = new BaseLoaderCallback(mContext) {
            @Override
            public void onManagerConnected(int status) {
                switch (status) {
                    case BaseLoaderCallback.SUCCESS: {
                        try {

                            // load cascade file from application resources
                            InputStream is = mContext.getResources().openRawResource(R.raw.haarcascade_frontalface_default);
                            File cascadeDir = mContext.getDir("cascade", Context.MODE_PRIVATE);
                            mCascadeFile = new File(cascadeDir, "haarcascade_frontalface_default.xml");
                            FileOutputStream os = new FileOutputStream(mCascadeFile);

                            byte[] buffer = new byte[4096];
                            int bytesRead;
                            while ((bytesRead = is.read(buffer)) != -1) {
                                os.write(buffer, 0, bytesRead);
                            }
                            is.close();
                            os.close();

                            mJavaDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
                            if (mJavaDetector.empty()) {
                                Log.e(LOG_TAG, "Failed to load cascade mClassifier");
                                mJavaDetector = null;
                            } else
                                Log.i(LOG_TAG, "Loaded cascade mClassifier from " + mCascadeFile.getAbsolutePath());

                            cascadeDir.delete();
                        } catch (IOException e) {
                            Log.e(LOG_TAG, "Failed to load cascade: + " + e.getMessage());
                        }
                    }
                    break;
                    default: {
                        super.onManagerConnected(status);
                    }
                    break;
                }
            }
        };
    }
}
