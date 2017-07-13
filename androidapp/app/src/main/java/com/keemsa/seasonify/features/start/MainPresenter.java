package com.keemsa.seasonify.features.start;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.keemsa.colorwheel.ColorElement;
import com.keemsa.colorwheel.ColorPickerView;
import com.keemsa.seasonify.BuildConfig;
import com.keemsa.seasonify.R;
import com.keemsa.seasonify.base.BasePresenter;
import com.keemsa.seasonify.model.Prediction;
import com.keemsa.seasonify.util.Cluster;
import com.keemsa.seasonify.util.SeasonifyImage;
import com.keemsa.seasonify.util.SeasonifyUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import butterknife.BindArray;
import butterknife.BindString;
import butterknife.ButterKnife;

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

/**
 * Created by sebastian on 3/27/17.
 */

public class MainPresenter extends BasePresenter<MainMvpView> implements BitmapLoaderAsyncTask.BitmapLoaderAsyncTaskReceiver {

    private final String LOG_TAG = MainPresenter.class.getSimpleName();
    private Classifier classifier;
    private Executor executor = Executors.newSingleThreadExecutor();

    private static final int INPUT_SIZE = 128;
    private static final int IMAGE_MEAN = 117;
    private static final float IMAGE_STD = 1;
    private static final String INPUT_NAME = "input_images_input";
    private static final String OUTPUT_NAME = "output_labels/Softmax";
    public static final String ACTION_DATA_UPDATED = "com.keemsa.seasonify.ACTION_DATA_UPDATED";

    private static final String MODEL_FILE = "file:///android_asset/seasonify.pb";
    private static final String LABEL_FILE = "file:///android_asset/seasonify.txt";

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mPredictionsDatabaseReference;
    private FirebaseStorage mFirebaseStorage;
    private StorageReference mFacePhotoStorageReference;

    private CascadeClassifier mJavaDetector;
    private File mCascadeFile;

    @BindString(R.string.prf_prev_photo)
    String mPrfPrevPhotoKey;

    @BindString(R.string.prf_prev_prediction)
    String mPrfPrevPredictionKey;

    @BindString(R.string.prf_prev_selection_type)
    String mPrfPrevSelectionType;

    @BindString(R.string.prf_prev_color_coords)
    String mPrfPrevColorCoords;

    @BindArray(R.array.autumn_colors)
    int[] autumn_colors;

    @BindArray(R.array.spring_colors)
    int[] spring_colors;

    @BindArray(R.array.summer_colors)
    int[] summer_colors;

    @BindArray(R.array.winter_colors)
    int[] winter_colors;

    public MainPresenter(Activity activity) {
        ButterKnife.bind(this, activity);
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mPredictionsDatabaseReference = mFirebaseDatabase.getReference().child("predictions");
        mFirebaseStorage = FirebaseStorage.getInstance();
        mFacePhotoStorageReference = mFirebaseStorage.getReference().child("face_photos");
    }

    public boolean hasPreviousResults(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String result = preferences.getString(mPrfPrevPredictionKey, "");
        String path = preferences.getString(mPrfPrevPhotoKey, "");

        return !(result.equals("") && path.equals(""));
    }

    public void classifyImage(Context context, File photoFile) {
        BitmapLoaderAsyncTask task = new BitmapLoaderAsyncTask(context, this, INPUT_SIZE, 0);
        task.execute(photoFile.getAbsolutePath());
    }

    public File createImageFile(Context context) throws IOException {

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

    public void initTensorFlowAndLoadModel(final ContextWrapper contextWrapper) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    classifier = TensorFlowImageClassifier.create(
                            contextWrapper.getAssets(),
                            MODEL_FILE,
                            LABEL_FILE,
                            INPUT_SIZE,
                            IMAGE_MEAN,
                            IMAGE_STD,
                            INPUT_NAME,
                            OUTPUT_NAME);
                } catch (final Exception e) {
                    throw new RuntimeException("Error initializing TensorFlow!", e);
                }
            }
        });
    }

    @Override
    public void load(Context context, Bitmap bitmap) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String result = preferences.getString(mPrfPrevPredictionKey, "");

        if (isViewAttached()) {
            if (bitmap != null && !result.equals("")) {
                updateViewUponResult(context, result, bitmap);
            }
        }
    }

    @Override
    public void classify(final Context context, String path, Bitmap bitmap) {
        Bitmap faceBitmap = detectFace(context, path);

        if (faceBitmap != null) {

            String faceOnlyPath = SeasonifyUtils.getFileNameNoExtension(path) + "_faceOnly.jpg";
            SeasonifyImage.saveImage(faceBitmap, faceOnlyPath);

            if (BuildConfig.DEBUG) {
                SeasonifyImage.addImageToGallery(context, faceOnlyPath);
            }

            final List<Classifier.Recognition> results = classifier.recognizeImage(faceBitmap);

            if (isViewAttached()) {

                String season = results.get(0).getTitle();
                Uri photoUri = generateUri(context, new File(path)); // To update the view
                Uri photoUri2 = Uri.fromFile(new File(faceOnlyPath)); // To store in firebase

                updateViewUponResult(context, season, faceBitmap);

                storeResults(context, faceOnlyPath, season);

                final int seasonInt = getPredictionAsInteger(season);
                StorageReference facePhotoRef = mFacePhotoStorageReference.child(photoUri2.getLastPathSegment());
                facePhotoRef.putFile(photoUri2).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Uri downloadUrl = taskSnapshot.getDownloadUrl();
                        Prediction prediction = new Prediction(seasonInt, downloadUrl.toString());
                        mPredictionsDatabaseReference.push().setValue(prediction);

                        // Send broadcast to update the widgets
                        Intent updateIntent = new Intent(ACTION_DATA_UPDATED);
                        updateIntent.setPackage(context.getPackageName());
                        context.sendBroadcast(updateIntent);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(LOG_TAG, "Error when storing file: " + e.getMessage());
                    }
                });
            }
        } else {
            getMvpView().showToastMessage(context.getString(R.string.msg_face_no_detected));
        }

    }

    public Uri generateUri(Context context, File file) {
        return FileProvider.getUriForFile(
                context,
                "com.keemsa.seasonify.fileprovider",
                file
        );
    }

    public void loadPreviousResults(Context context) {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String path = preferences.getString(mPrfPrevPhotoKey, "");

        BitmapLoaderAsyncTask task = new BitmapLoaderAsyncTask(context, this, INPUT_SIZE, 1);
        task.execute(path);
    }

    public int updateColorSelectionType(Context context, ColorPickerView.COLOR_SELECTION colorSelection) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        int index = ColorPickerView.COLOR_SELECTION.indexOf(colorSelection);
        editor.putInt(mPrfPrevSelectionType, index);
        editor.apply();

        return index;
    }

    public void updateSelectedColorCoords(Context context, List<ColorElement> colors) {
        try {
            ColorElement main = colors.get(0);
            float x = main.getX();
            float y = main.getY();
            String coords = String.valueOf(x) + ";" + String.valueOf(y);
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(mPrfPrevColorCoords, coords);
            editor.apply();
        } catch (IndexOutOfBoundsException e) {
            Log.e(LOG_TAG, e.getMessage());
        }
    }

    public float[] getPreviousSelectedColorCoords(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String coords = preferences.getString(mPrfPrevColorCoords, "0;0");
        StringTokenizer st = new StringTokenizer(coords, ";");
        float x = Float.parseFloat(st.nextToken());
        float y = Float.parseFloat(st.nextToken());

        return new float[]{x, y};
    }

    public String getPreviousResult(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String result = preferences.getString(mPrfPrevPredictionKey, "");
        return result;
    }

    private void storeResults(Context context, String path, String prediction) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(mPrfPrevPhotoKey, path);
        editor.putString(mPrfPrevPredictionKey, prediction);
        editor.apply();
    }

    private int[] getSeasonalColors(String season) {
        if (season.equals("autumn")) {
            return autumn_colors;
        } else if (season.equals("spring")) {
            return spring_colors;
        } else if (season.equals("summer")) {
            return summer_colors;
        } else if (season.equals("winter")) {
            return winter_colors;
        }

        return new int[]{};
    }

    private int getPredictionAsInteger(String season) {
        if (season.equals("autumn")) {
            return 0;
        } else if (season.equals("spring")) {
            return 1;
        } else if (season.equals("summer")) {
            return 2;
        } else if (season.equals("winter")) {
            return 3;
        }
        return -1;
    }

    // based on https://github.com/joaopedronardari/OpenCV-AndroidSamples/blob/master/app/src/main/java/com/jnardari/opencv_androidsamples/activities/FaceDetectionActivity.java#L62
    private BaseLoaderCallback generateLoaderCallback(final Context context) {
        return new BaseLoaderCallback(context) {
            @Override
            public void onManagerConnected(int status) {
                switch (status) {
                    case BaseLoaderCallback.SUCCESS: {
                        try {

                            // load cascade file from application resources
                            InputStream is = context.getResources().openRawResource(R.raw.haarcascade_frontalface_default);
                            File cascadeDir = context.getDir("cascade", Context.MODE_PRIVATE);
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
                                Log.e(LOG_TAG, "Failed to load cascade classifier");
                                mJavaDetector = null;
                            } else
                                Log.i(LOG_TAG, "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());

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

    private Bitmap detectFace(final Context context, String path) {
        BaseLoaderCallback loaderCallback = generateLoaderCallback(context);

        if (!OpenCVLoader.initDebug()) {
            Log.d(LOG_TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, context, loaderCallback);
        } else {
            Log.d(LOG_TAG, "OpenCV library found inside package. Using it!");
            loaderCallback.onManagerConnected(BaseLoaderCallback.SUCCESS);
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

            Imgproc.resize(faceMAT, nFaceMAT, new Size(INPUT_SIZE, INPUT_SIZE)); // resize to pass to classifier
            Imgproc.cvtColor(nFaceMAT, nFaceMAT, Imgproc.COLOR_RGB2BGR); // bitmap is BGR

            if(BuildConfig.DEBUG){
                List<Mat> clusters = Cluster.cluster(nFaceMAT, 3);
                String baseName = SeasonifyUtils.getFileNameNoExtension(path);

                for(int i = 0; i < clusters.size(); i++){
                    String clusterName = baseName + "k_" + i + ".jpg";
                    SeasonifyImage.saveImage(clusters.get(i), clusterName);
                    SeasonifyImage.addImageToGallery(context, clusterName);
                }
            }

            Bitmap bmp = Bitmap.createBitmap(nFaceMAT.cols(), nFaceMAT.rows(), Bitmap.Config.RGB_565);
            Utils.matToBitmap(nFaceMAT, bmp);

            return bmp;
        }

        return null;
    }

    private void updateViewUponResult(Context context, String result, Bitmap bitmap) {
        getMvpView().updateResult(result);
        getMvpView().updateColorWheel(getSeasonalColors(result), bitmap);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        int selectionType = preferences.getInt(mPrfPrevSelectionType, 0);
        getMvpView().updateColorSelection(selectionType);
    }
}
