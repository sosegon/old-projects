package com.keemsa.seasonify.data.remote;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.keemsa.seasonify.model.Prediction;

import javax.inject.Singleton;

/**
 * Created by sebastian on 14/07/17.
 */

@Singleton
public class FirebaseHelper {

    public static final String PREDICTIONS_FIELD = "predictions";
    public static final String FACE_PHOTOS_FIELD = "face_photos";

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mPredictionsDatabaseReference;
    private FirebaseStorage mFirebaseStorage;
    private StorageReference mFacePhotoStorageReference;

    public FirebaseHelper() {
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mPredictionsDatabaseReference = mFirebaseDatabase.getReference().child(PREDICTIONS_FIELD);

        mFirebaseStorage = FirebaseStorage.getInstance();
        mFacePhotoStorageReference = mFirebaseStorage.getReference().child(FACE_PHOTOS_FIELD);
    }

    public StorageReference getFacePhotoReference(String name) {
        return mFacePhotoStorageReference.child(name);
    }

    public void storePrediction(Prediction prediction) {
        mPredictionsDatabaseReference.push().setValue(prediction);
    }
}
