package com.stazo.project_18;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoginAct extends AppCompatActivity {

    private String user_name;
    private String user_id;
    ArrayList<String> myEvents = new ArrayList<String>();
    //Firebase fb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppEventsLogger.activateApp(this); // tracks App Events
        setContentView(R.layout.login);

        if (findViewById(R.id.login_button) != null) {
            if (savedInstanceState != null) {
                return;
            }

            /*getSupportFragmentManager().beginTransaction()
                    .add(R.id.login_button, new ContentFragment()).commit();*/
        }

                //   fb = ((Project_18) getApplication()).getFB();
    }
/*
    // tries to login with the current user_id
    private void tryAccount() {
        fb.child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // if the user exists, pull their data and goToMapAct
                if (dataSnapshot.child(user_id).exists()) {

                    // pull data
                    user_name = ((String) dataSnapshot.child(user_id).child("name").getValue());
                    myEvents = ((ArrayList<String>)
                            dataSnapshot.child(user_id).child("my_events").getValue());
                    User me = new User(user_name, user_id, myEvents);

                    // save the user to the application
                    ((Project_18) getApplication()).setMe(me);

                    // go to the Map screen
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

    // create and push new user to Firebase
    private void createUser() {

        // add user to firebase
        User me = new User(user_name, user_id);
        fb.child("Users").child(user_id).setValue(me);

        // save the user to the application
        ((Project_18) getApplication()).setMe(me);
    }

    // proceed to the Map activity

*/

    private void goToMapAct(){
        startActivity(new Intent(this, MapAct.class));
    }




}
