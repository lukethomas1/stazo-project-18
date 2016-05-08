package com.stazo.project_18;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;

public class InitialAct extends AppCompatActivity {
    public static final String MyPREFERENCES = "MyPrefs" ;

    private String userName;
    private String userId;
    ArrayList<String> myEvents = new ArrayList<String>();
    Firebase fb;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.initial);

        sharedPreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        userId = sharedPreferences.getString("userId", "0"); // "0" default value
        Firebase.setAndroidContext(this);
        fb = ((Project_18) getApplication()).getFB();
        tryAccount();
    }

    // tries to login with the current user_id
    private void tryAccount() {
        fb.child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // if the user exists, pull their data and goToMapAct
                if (dataSnapshot.child(userId).exists()) {

                    // pull data
                    userName = ((String) dataSnapshot.child(userId).child("name").getValue());
                    myEvents = ((ArrayList<String>)
                            dataSnapshot.child(userId).child("my_events").getValue());
                    User me = new User(userName, userId, myEvents);

                    // save the user to the application
                    ((Project_18) getApplication()).setMe(me);

                    // remove listener
                    fb.child("Users").removeEventListener(this);

                    // go to the Map screen
                    goToMainAct();
                }

                // if the user doesn't exist, goToLoginAct
                else {
                    // remove listener
                    fb.child("Users").removeEventListener(this);

                    // go to Login screen
                    goToLoginAct();
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });
    }

    private void goToMainAct(){
        startActivity(new Intent(this, MainAct.class));
    }
    private void goToLoginAct(){
        startActivity(new Intent(this, LoginAct.class));
    }


}
