package com.stazo.project_18;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.FacebookSdk;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class InitialAct extends AppCompatActivity {
    public static final String MyPREFERENCES = "MyPrefs" ;

    private String userName;
    private String userId;
    ArrayList<String> myEvents = new ArrayList<String>();
    Firebase fb;
    SharedPreferences sharedPreferences;
    AccessTokenTracker accessTokenTracker;
    AccessToken accessToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext()); // Needed anytime FB SDK is used
        setContentView(R.layout.initial);

        sharedPreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        userId = sharedPreferences.getString("userId", "0"); // "0" default value
        Firebase.setAndroidContext(this);
        fb = ((Project_18) getApplication()).getFB();

        EventHandler EH = new EventHandler();
        //EH.clearEvents();
        //EH.generateEvents();
        NotificationHandler NH = new NotificationHandler();
        //NH.generateNotifications();
        //NH.pullNotifications();
        //NH.testViewed();

                ((Project_18) getApplication()).pullAllUsers();

        // if the user logs in for the first time
        if(sharedPreferences.getBoolean("isLoggedIn", false)) { // check if they have logged in before
            // tracks if the AccessToken has changed and sees if it is still valid
            accessToken = AccessToken.getCurrentAccessToken();
            updateWithToken(accessToken);
        }
        else { // first time logging in
            Log.d("FB SDK", "InitialAct: First Time Logging In");
            goToLoginAct();
        }
    }

    // Checks if the AccessToken is null and runs the appropriate method
    private void updateWithToken(AccessToken currentAccessToken) {
        Log.d("FB SDK", "Initial Act: Checking the accessTokens");
        if (currentAccessToken != null) {
            Log.d("FB SDK", "InitialAct: AccessToken is not null");
            tryAccount(); // check sharedPrefs to see if user is logged in
        }
        else {
            Log.d("FB SDK", "InitialAct: AccessToken is null");
            goToLoginAct(); // else go to loginAct
        }
        //accessTokenTracker.stopTracking();
    }

    // tries to login with the current user_id
    private void tryAccount() {
        fb.child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // if the user exists, pull their data and goToMapAct
                if (dataSnapshot.child(userId).exists()) {

                    User me = new User((HashMap<String, Object>)
                            dataSnapshot.child(userId).getValue());

                    // save the user to the application
                    ((Project_18) getApplication()).setMe(me);

                    // construct friends
                    me.constructFriends(fb);

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

        // If I have not been welcomed
        if (sharedPreferences.getBoolean("beenWelcomed", false)) {
            startActivity(new Intent(this, WelcomeActivity.class));
        }
        else {
            Toast.makeText(getApplicationContext(), "Welcome back", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MainAct.class));
            finish();
        }
    }

    private void goToLoginAct(){

        startActivity(new Intent(this, LoginAct.class));
        finish();
    }

    @Override
    public void onResume() {
        super.onResume();
        Firebase.setAndroidContext(this);
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}
