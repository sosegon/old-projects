package com.keemsa.seasonify.data.remote;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.DatabaseReference.CompletionListener;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.keemsa.seasonify.data.model.Prediction;
import com.keemsa.seasonify.data.model.User;

import javax.inject.Singleton;

/**
 * Created by sebastian on 14/07/17.
 */

@Singleton
public class FirebaseHelper {

    // Database
    public static final String PREDICTIONS_KEY = "predictions";
    public static final String PALETTES_KEY = "palettes";
    public static final String SEASONS_KEY = "seasons";
    public static final String USERS_KEY = "users";
    public static final String LAST_PREDICTION_KEY = "lastPrediction";
    public static final String PHOTO_URL_KEY = "photoUrl";

    //Storage
    public static final String FACE_PHOTOS_FIELD = "face_photos";

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mPredictionsDatabaseReference;
    private DatabaseReference mUsersDatabaseReference;
    private DatabaseReference mPalettesDatabaseReference;
    private DatabaseReference mSeasonsDatabaseReference;

    private FirebaseStorage mFirebaseStorage;
    private StorageReference mFacePhotoStorageReference;

    public FirebaseHelper() {
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseDatabase.setPersistenceEnabled(true);
        mPredictionsDatabaseReference = mFirebaseDatabase.getReference().child(PREDICTIONS_KEY);
        mUsersDatabaseReference = mFirebaseDatabase.getReference().child(USERS_KEY);
        mSeasonsDatabaseReference = mFirebaseDatabase.getReference().child(SEASONS_KEY);

        mFirebaseStorage = FirebaseStorage.getInstance();
        mFacePhotoStorageReference = mFirebaseStorage.getReference().child(FACE_PHOTOS_FIELD);
    }

    public StorageReference getFacePhotoReference(String name) {
        return mFacePhotoStorageReference.child(name);
    }

    public DatabaseReference storePrediction(Prediction prediction, CompletionListener val) {
        DatabaseReference predRef = mPredictionsDatabaseReference.push();
        if(val != null) {
            predRef.setValue(prediction, val);
        } else {
            predRef.setValue(prediction);
        }
        return predRef;
    }

    public DatabaseReference retrievePrediction(String predId) {
        return mPredictionsDatabaseReference.child(predId);
    }

    public DatabaseReference storeUser(User user){
        DatabaseReference userRef = mUsersDatabaseReference.push();
        userRef.setValue(user);
        return userRef;
    }

    public DatabaseReference retrieveUser(String userId) {
        return mUsersDatabaseReference.child(userId);
    }

    public DatabaseReference retrieveSeason(String seasonId) {
        return mSeasonsDatabaseReference.child(seasonId);
    }
}
