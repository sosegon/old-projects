package com.keemsa.seasonify.data.remote;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.DatabaseReference.CompletionListener;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.keemsa.seasonify.data.model.Palette;
import com.keemsa.seasonify.data.model.Prediction;
import com.keemsa.seasonify.data.model.User;
import com.keemsa.seasonify.util.SeasonifyImage;

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
    public static final String IS_FAV_KEY = "fav";

    //Storage
    public static final String FACE_PHOTOS_FIELD = "face_photos";

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mPredictionsDatabaseReference;
    private DatabaseReference mPalettesDatabaseReference;
    private DatabaseReference mSeasonsDatabaseReference;
    private DatabaseReference mUsersDatabaseReference;

    private FirebaseStorage mFirebaseStorage;
    private StorageReference mFacePhotoStorageReference;

    public FirebaseHelper() {
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseDatabase.setPersistenceEnabled(true);
        mPredictionsDatabaseReference = mFirebaseDatabase.getReference().child(PREDICTIONS_KEY);
        mPalettesDatabaseReference = mFirebaseDatabase.getReference().child(PALETTES_KEY);
        mSeasonsDatabaseReference = mFirebaseDatabase.getReference().child(SEASONS_KEY);
        mUsersDatabaseReference = mFirebaseDatabase.getReference().child(USERS_KEY);

        mPalettesDatabaseReference.keepSynced(true);

        mFirebaseStorage = FirebaseStorage.getInstance();
        mFacePhotoStorageReference = mFirebaseStorage.getReference().child(FACE_PHOTOS_FIELD);
    }

    public DatabaseReference retrievePalettes(String userId) {
        return mUsersDatabaseReference.child(userId).child(PALETTES_KEY);
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

    public void processColorPalette(String userId, int[] colors, String season, boolean hasFavPalette) {
        String sPalette = SeasonifyImage.colorsAsString(colors);

        // TODO: check if this can be done with transaction
        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean paletteExists = false;
                String paletteId = "";
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Palette palette = snapshot.getValue(Palette.class);
                    if(palette.getColors().equals(sPalette)) {
                        paletteExists = true;
                        paletteId = snapshot.getKey();
                        break;
                    }
                }

                if(paletteExists) {
                    // Update palette in user
                    mUsersDatabaseReference
                            .child(userId)
                            .child(PALETTES_KEY)
                            .child(paletteId)
                            .child(IS_FAV_KEY)
                            .setValue(hasFavPalette);
                } else {
                    // Create palette in user
                    Palette palette = new Palette(userId, sPalette, season, hasFavPalette);
                    DatabaseReference paletteRef = mUsersDatabaseReference
                            .child(userId)
                            .child(PALETTES_KEY)
                            .push();

                    final String npaletteId = paletteRef.getKey();
                    paletteRef.setValue(palette,
                        (de, dr) -> {
                            // Add palette to palettes
                            mPalettesDatabaseReference.child(npaletteId).setValue(palette);

                            // Add palette to seasons
                            mSeasonsDatabaseReference.child(season).child(PALETTES_KEY).child(npaletteId).setValue(true);
                        }
                    );
                }
                mUsersDatabaseReference.child(userId).child(PALETTES_KEY).removeEventListener(this); // to avoid leaks
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        mUsersDatabaseReference.child(userId).child(PALETTES_KEY).addListenerForSingleValueEvent(listener);
    }
}
