package com.keemsa.seasonify.data;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.keemsa.colorwheel.ColorElement;
import com.keemsa.seasonify.data.local.PreferencesHelper;
import com.keemsa.seasonify.data.model.Prediction;
import com.keemsa.seasonify.data.model.User;
import com.keemsa.seasonify.data.remote.FirebaseHelper;
import com.keemsa.seasonify.util.RxEvent;
import com.keemsa.seasonify.util.RxEventBus;
import com.keemsa.seasonify.util.SeasonifyImage;

import java.io.File;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

import static com.keemsa.colorwheel.ColorPickerView.COLOR_SELECTION;
import static com.keemsa.seasonify.data.remote.FirebaseHelper.LAST_PREDICTION_KEY;
import static com.keemsa.seasonify.data.remote.FirebaseHelper.PHOTO_URL_KEY;
import static com.keemsa.seasonify.data.remote.FirebaseHelper.PREDICTIONS_KEY;
import static com.keemsa.seasonify.data.remote.FirebaseHelper.USERS_KEY;
import static com.keemsa.seasonify.util.RxEvent.RX_EVENT_TYPE.COLOR_PALETTE_LIKED;
import static com.keemsa.seasonify.util.RxEvent.RX_EVENT_TYPE.COLOR_PALETTE_UPDATED;
import static com.keemsa.seasonify.util.RxEvent.RX_EVENT_TYPE.COLOR_COORDS_SELECTED;
import static com.keemsa.seasonify.util.RxEvent.RX_EVENT_TYPE.COLOR_SELECTION_SELECTED;
import static com.keemsa.seasonify.util.RxEvent.RX_EVENT_TYPE.COLOR_SELECTION_UPDATED;
import static com.keemsa.seasonify.util.RxEvent.RX_EVENT_TYPE.PREDICTION_CHANGED;
import static com.keemsa.seasonify.util.RxEvent.RX_EVENT_TYPE.PREDICTION_QUERIED;
import static com.keemsa.seasonify.util.RxEvent.RX_EVENT_TYPE.PREDICTION_REQUESTED;
import static com.keemsa.seasonify.util.RxEvent.RX_EVENT_TYPE.PREDICTION_UPDATED;


/**
 * Created by sebastian on 14/07/17.
 */

@Singleton
public class DataManager {

    public static final String ACTION_DATA_UPDATED = "com.keemsa.seasonify.ACTION_DATA_UPDATED";

    private final PreferencesHelper mPreferencesHelper;
    private final FirebaseHelper mFirebaseHelper;
    private final RxEventBus mEventBus;

    @Inject
    public DataManager(PreferencesHelper preferencesHelper, RxEventBus eventBus) {
        mPreferencesHelper = preferencesHelper;
        mFirebaseHelper = new FirebaseHelper(); // Does not depend on anything
        mEventBus = eventBus;
        initBus();
    }

    public PreferencesHelper getPreferencesHelper() {
        return mPreferencesHelper;
    }

    public FirebaseHelper getFirebaseHelper() {
        return mFirebaseHelper;
    }

    public Observable<Bitmap> loadImage(String path, int imageWidth, int imageHeight) {
        return Observable.fromCallable(() -> SeasonifyImage.retrieveFromFile(path, imageWidth, imageHeight))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<Bitmap> classifyImage(String path, int imageSize) {

        return Observable.fromCallable(() -> SeasonifyImage.retrieveFromFileToClassify(path, imageSize))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());

    }

    public void storePrediction(Context context, String season, String facePhotoPath) {
        mPreferencesHelper.storePrediction(season);
        mPreferencesHelper.storePhotoPath(facePhotoPath);

        // If user has not been added, create a new one
        String userId = mPreferencesHelper.retrieveUserId();

        if(userId.equals("")) {
            DatabaseReference userRef =
                    mFirebaseHelper
                            .storeUser(new User(""));

            // store the key locally
            userId = userRef.getKey();
            mPreferencesHelper.storeUserId(userId);
        }

        // Store the prediction
        DatabaseReference predRef =
                mFirebaseHelper
                        .storePrediction(new Prediction(season, "", userId), listenerCompletionPrediction(context));

        String predId = predRef.getKey();
        mPreferencesHelper.storePredictionId(predId);

        // Update the season with new prediction and user
        DatabaseReference seasRef = mFirebaseHelper.retrieveSeason(season);
        seasRef.child(PREDICTIONS_KEY).child(predId).setValue(true);
        seasRef.child(USERS_KEY).child(userId).setValue(true);

        // Update the user with new prediction
        DatabaseReference userRef = mFirebaseHelper.retrieveUser(userId);
        userRef.child(LAST_PREDICTION_KEY).setValue(predId);
        userRef.child(PREDICTIONS_KEY).child(predId).setValue(true);
    }

    private void onSuccessPutFile(Context context, UploadTask.TaskSnapshot taskSnapshot, String predId) {
        try {
            Uri downloadUrl = taskSnapshot.getDownloadUrl();
            DatabaseReference predRef = mFirebaseHelper.retrievePrediction(predId);
            predRef.child(PHOTO_URL_KEY).setValue(downloadUrl.toString());

            // Send broadcast to update the widgets
            Intent updateIntent = new Intent(ACTION_DATA_UPDATED);
            updateIntent.setPackage(context.getPackageName());
            context.sendBroadcast(updateIntent);
        } catch (NullPointerException e) {
            Timber.e(e.getMessage());
        }
    }

    private DatabaseReference.CompletionListener listenerCompletionPrediction(Context context) {
        return (de, dr) -> {
            String predId = mPreferencesHelper.retrievePredictionId();
            String facePath = mPreferencesHelper.retrievePhotoPath();
            Uri photoUri = Uri.fromFile(new File(facePath));
            StorageReference facePhotoRef = mFirebaseHelper.getFacePhotoReference(photoUri.getLastPathSegment());
            facePhotoRef.putFile(photoUri)
                    .addOnSuccessListener((y) -> onSuccessPutFile(context, y, predId))
                    .addOnFailureListener((y) -> Timber.e("Error when storing file: " + y.getMessage()));
        };
    }

    private void initBus() {
        // Favourite color
        Consumer<Object> favouriteColor = (y) -> {
            if(((RxEvent)y).getType() == COLOR_PALETTE_LIKED) {
                try {
                    int[] colors = (int[])(((RxEvent) y).getArgument());
                    int[] sortedColors = SeasonifyImage.sortColors(colors);
                    String season = mPreferencesHelper.retrievePrediction();

                    mPreferencesHelper.processColorPalette(sortedColors);
                    boolean hasColorPalette = mPreferencesHelper.hasColorPalette(sortedColors);

                    // firebase
                    String userId = mPreferencesHelper.retrieveUserId();
                    mFirebaseHelper.processColorPalette(userId, sortedColors, season, hasColorPalette);

                    mEventBus.post(new RxEvent(COLOR_PALETTE_UPDATED, hasColorPalette));
                } catch(ClassCastException e) {
                    Timber.e(e.getMessage());
                }
            }
        };
        mEventBus.observable().subscribe(favouriteColor);

        // Color coords
        Consumer<Object> colorCoords = (y) -> {
            if(((RxEvent)y).getType() == COLOR_COORDS_SELECTED) {
                try {
                    List<ColorElement> colors = (List<ColorElement>)(((RxEvent) y).getArgument());
                    mPreferencesHelper.storeSelectedColorCoords(colors);
                } catch(ClassCastException e) {
                    Timber.e(e.getMessage());
                }
            }
        } ;
        mEventBus.observable().subscribe(colorCoords);

        // Selection type
        Consumer<Object> colorSelection = (y) -> {
            if(((RxEvent)y).getType() == COLOR_SELECTION_SELECTED) {
                try {
                    COLOR_SELECTION selection = (COLOR_SELECTION) (((RxEvent) y).getArgument());
                    int index = mPreferencesHelper.storeColorSelectionType(selection);
                    mEventBus.post(new RxEvent(COLOR_SELECTION_UPDATED, index));
                } catch(ClassCastException e) {
                    Timber.e(e.getMessage());
                }
            }
        } ;
        mEventBus.observable().subscribe(colorSelection);

        // Prediction
        Consumer<Object> predictionQuery = (y) -> {
            if(((RxEvent)y).getType() == PREDICTION_REQUESTED) {
                try {
                    String prediction = mPreferencesHelper.retrievePrediction();
                    mEventBus.post(new RxEvent(PREDICTION_QUERIED, prediction));
                } catch(ClassCastException e) {
                    Timber.e(e.getMessage());
                }
            } else if(((RxEvent)y).getType() == PREDICTION_CHANGED) {
                try {
                    String prediction = (String)(((RxEvent) y).getArgument());
                    mPreferencesHelper.storePrediction(prediction);
                    mEventBus.post(new RxEvent(PREDICTION_UPDATED, prediction));
                } catch(ClassCastException e) {
                    Timber.e(e.getMessage());
                }
            }
        };
        mEventBus.observable().subscribe(predictionQuery);
    }
}
