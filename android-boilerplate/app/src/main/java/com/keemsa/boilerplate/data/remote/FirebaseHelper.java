package com.keemsa.boilerplate.data.remote;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.keemsa.boilerplate.data.model.Message;

import javax.inject.Singleton;

@Singleton
public class FirebaseHelper {

    // Database
    public static final String USERS_KEY = "users";

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mMessagesDatabaseReference;

    private FirebaseStorage mFirebaseStorage;

    public FirebaseHelper() {
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseDatabase.setPersistenceEnabled(true);
        mMessagesDatabaseReference = mFirebaseDatabase.getReference().child(USERS_KEY);

        mFirebaseStorage = FirebaseStorage.getInstance();
    }

    public DatabaseReference storeMessage(Message message){
        DatabaseReference messageRef = mMessagesDatabaseReference.push();
        messageRef.setValue(message);
        return messageRef;
    }

    public DatabaseReference retrieveMessage(String messageId) {
        return mMessagesDatabaseReference.child(messageId);
    }
}