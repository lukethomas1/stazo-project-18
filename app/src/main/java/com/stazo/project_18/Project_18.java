package com.stazo.project_18;

import android.app.Application;

import com.firebase.client.Firebase;

/**
 * Created by isaacwang on 4/25/16.
 */
public class Project_18 extends Application {
    private static final String fb = "https://stazo-project-18.firebaseio.com/";
    public Firebase getFB() { return new Firebase(fb);}
}
