package com.stazo.campus;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.facebook.appevents.AppEventsLogger;

import java.util.ArrayList;

public class LoginAct extends AppCompatActivity {

    private String user_name;
    private String user_id;
    ArrayList<String> myEvents = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppEventsLogger.activateApp(this); // tracks App Events
        setContentView(R.layout.login);

        if (findViewById(R.id.login_button) != null) {
            if (savedInstanceState != null) {
                return;
            }
        }
    }
}
