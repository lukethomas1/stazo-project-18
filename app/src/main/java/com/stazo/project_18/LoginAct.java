package com.stazo.project_18;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LoginAct extends AppCompatActivity {

    private String user_name;
    private String user_id;
    ArrayList<String> myEvents = new ArrayList<String>();
    Firebase fb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext()); // initializes FB sdk
        AppEventsLogger.activateApp(this); // tracks App Events
        setContentView(R.layout.login);

        if (findViewById(R.id.login_button) != null) {
            if (savedInstanceState != null) {
                return;
            }

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.login_button, new ContentFragment()).commit();
        }


        fb = ((Project_18) getApplication()).getFB();
    }

    // tries to login with the current user_id
    private void tryAccount() {
        fb.child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // if the user exists, pull their data and goToMapAct
                if (dataSnapshot.child(user_id).exists()) {
                    user_name = ((String) dataSnapshot.child(user_id).child("name").getValue());
                    myEvents = ((ArrayList<String>)
                            dataSnapshot.child(user_id).child("my_events").getValue());
                    goToMapAct();
                }

                // if the user doesn't exist, remove this listener
                else {
                    fb.child("Users").removeEventListener(this);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });
    }

    // creating a new user
    private void pushUserToFirebase() {

        // users will be represented as a hashmap
        HashMap<String, Object> user = new HashMap<String, Object>();

        // add name and my_events to the user
        user.put("name", user_name);
        user.put("my_events", myEvents);
        fb.child("Users").child(user_id).setValue(user);
    }

    // proceed to the Map activity
    private void goToMapAct(){}



}
