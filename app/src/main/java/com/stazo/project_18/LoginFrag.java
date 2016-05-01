package com.stazo.project_18;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

public class LoginFrag extends Fragment {

    private TextView mTextDetails;

    private String userName;
    private String userId;

    /* For call back from activity or fragment */
    private CallbackManager mCallbackManager;
    /* Will tell us if the login is successful, fail, or error */
    private FacebookCallback<LoginResult> mCallback = new FacebookCallback<LoginResult>() {
        @Override
        public void onSuccess(LoginResult loginResult) {

            AccessToken accessToken = loginResult.getAccessToken(); // Access token for FB
            Profile profile = Profile.getCurrentProfile(); // gets the current profile, it could be null

            if (profile != null) {
                userName = profile.getName();
                userId = profile.getId();
                mTextDetails.setText("Welcome " + userName);
            }
            else {
                userName = "null";
                userId = "null";
                mTextDetails.setText("Welcome " + userName);
            }

            Log.d("FB SDK", "Name: " + userName);
            Log.d("FB SDK", "UserId: " + userId);
            System.out.println ("Name: " + userName);
            System.out.println ("UserId: " + userId);

        }

        @Override
        public void onCancel() {
            Log.d("FB SDK", "Facebook Login Cancelled");

        }

        @Override
        public void onError(FacebookException error) {
            Log.d("FB SDK", "Facebook Login Error");

        }
    };

    public LoginFrag() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FacebookSdk.sdkInitialize(getActivity().getApplicationContext());
        mCallbackManager = CallbackManager.Factory.create();
        Log.d("FB SDK", "onCreate Completed");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d("FB SDK", "onCreateView Completed");
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // initialize button from xml
        LoginButton loginButton = (LoginButton) view.findViewById(R.id.login_button);
        loginButton.setFragment(this); // passes reference to current fragment
        loginButton.registerCallback(mCallbackManager, mCallback);
        Log.d("FB SDK", "onViewCreate Completed");

        setupTextDetails(view);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
        Log.d("FB SDK", "onActivityResult Completed");
    }

    private void setupTextDetails(View view) {
        mTextDetails = (TextView) view.findViewById(R.id.title_text);
    }
}