package com.stazo.project_18;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class LoginFrag extends Fragment {

    public static final String MyPREFERENCES = "MyPrefs" ;
    private TextView mTextDetails;
    private String userName;
    private String userId;
    private Profile profile;
    private AccessToken accessToken;
    ArrayList<String> myEvents = new ArrayList<String>();
    SharedPreferences sharedPreferences;
    Firebase fb;

    /* ProfileTracker to receive notifications of profile changes */
    private ProfileTracker mProfileTracker;
    /* For call back from activity or fragment */
    private CallbackManager mCallbackManager;
    /* Will tell us if the login is successful, fail, or error */
    private FacebookCallback<LoginResult> mCallback = new FacebookCallback<LoginResult>() {

        @Override
        public void onSuccess(LoginResult loginResult) {
            // go to the Map screen
            goToMainAct();

            if(Profile.getCurrentProfile() == null) {
                mProfileTracker = new ProfileTracker() {
                    @Override
                    protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                        profile = currentProfile.getCurrentProfile(); // gets the current profile, it could be null

                        mProfileTracker.stopTracking();
                        userName = profile.getName();
                        userId = profile.getId();
                        //mTextDetails.setText("Welcome\n" + userName);

                        // try logging in with the account
                        tryAccount();
                    }
                };
                mProfileTracker.startTracking();
            }
            else {
                profile = Profile.getCurrentProfile(); // gets the current profile, it could be null
                accessToken = loginResult.getAccessToken();
                userName = profile.getName();
                userId = profile.getId();
                //mTextDetails.setText("Welcome\n" + userName);

                // try logging in with the account
                tryAccount();
            }
            Toast.makeText(getActivity().getApplicationContext(), "Logged in", Toast.LENGTH_SHORT).show();
            Log.d("FB SDK", "Name: " + userName);
            Log.d("FB SDK", "UserId: " + userId);

        }

        @Override
        public void onCancel() {
            Log.d("FB SDK", "Facebook Login Cancelled");
            Toast.makeText(getActivity().getApplicationContext(), "Login Cancelled", Toast.LENGTH_SHORT).show();

        }

        @Override
        public void onError(FacebookException error) {
            Log.d("FB SDK", "Facebook Login Error");
            Toast.makeText(getActivity().getApplicationContext(), "Login Error: Try again later", Toast.LENGTH_SHORT).show();

        }
    };

    public LoginFrag() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FacebookSdk.sdkInitialize(getActivity().getApplicationContext());
        mCallbackManager = CallbackManager.Factory.create();

        fb = ((Project_18)getActivity().getApplication()).getFB();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // initialize button from xml
        LoginButton loginButton = (LoginButton) view.findViewById(R.id.login_button);
        loginButton.setFragment(this); // passes reference to current fragment
        loginButton.registerCallback(mCallbackManager, mCallback);

        setupTextDetails(view);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void setupTextDetails(View view) {
        mTextDetails = (TextView) view.findViewById(R.id.title_text);
    }

    // tries to login with the current user_id: not identical to InitialAct's version
    private void tryAccount() {
        fb.child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // if the user exists, pull their data and goToMapAct
                if (dataSnapshot.child(userId).exists()) {

                    // pull data
                    //userName = ((String) dataSnapshot.child(userId).child("name").getValue());
                    /*myEvents = ((ArrayList<String>)
                            dataSnapshot.child(userId).child("myEvents").getValue());
                            */
                    //User me = new User(userId, userName, myEvents);
                    User me = new User((HashMap<String,Object>)
                            dataSnapshot.child(userId).getValue());

                    // save the user to the application
                    ((Project_18) getActivity().getApplication()).setMe(me);

                    // save the userId to this device
                    setSharedPreferences();

                    // go to the Map screen
                    goToMainAct();

                    // remove listener
                    fb.child("Users").removeEventListener(this);
                }

                // if the user doesn't exist, create a User and goToMapAct
                else {
                    // create a User with the current info
                    createUser();

                    // save the userId to this device
                    setSharedPreferences();

                    //go to the Map screen
                    goToMainAct();

                    // remove listener
                    fb.child("Users").removeEventListener(this);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });
    }

    // save the userId to sharedPreferences so they don't have to relog
    private void setSharedPreferences() {
        sharedPreferences = getActivity().getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("userId", userId);
        editor.putBoolean("isLoggedIn", true);
        editor.apply();
    }

    private void goToMainAct(){
        startActivity(new Intent(getActivity(), MainAct.class));
        getActivity().finish();
    }

    // creates a User and pushes it to Firebase
    private void createUser() {
        // add user to firebase
        User me = new User(userId, userName);
        //fb.child("Users").child(userId).setValue(me);
        me.pushToFirebase(fb);

        // save the user to the application
        ((Project_18) getActivity().getApplication()).setMe(me);
    }

    @Override
    public void onResume() {
        super.onResume();
        Firebase.setAndroidContext(getContext());
    }

}