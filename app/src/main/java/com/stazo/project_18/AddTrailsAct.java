package com.stazo.project_18;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.firebase.client.Firebase;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class AddTrailsAct extends AppCompatActivity {

    private SearchView searchView = null;
    private SearchView.OnQueryTextListener queryTextListener;
    private HashMap<String, String> relevantUsers = new HashMap<String, String>();  // name to id
    private HashMap<String, View> buttonMap = new HashMap<String, View>();
    private Firebase fb;
    //private Bitmap profPicBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_trails);

        // get firebase reference
        fb = ((Project_18) getApplication()).getFB();

        // set relevantUsers
        relevantUsers = ((Project_18) getApplication()).getMe().getFriends();
        generateButtons();

        // search view things
        SearchManager searchManager = (SearchManager) this.getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) findViewById(R.id.searchView);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(this.getComponentName()));
        queryTextListener = new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
                updateUserSection(newText);
                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {

                // hide keyboard
                searchView.clearFocus();
                return true;
            }
        };
        searchView.setOnQueryTextListener(queryTextListener);
    }

    public void addCategoryTrail(View v) {
        Integer trailNum = Integer.parseInt((String) v.getTag());
        ((Project_18) getApplication()).getMe().addTrail(fb, trailNum - 1);
    }

    public void goToProfile(View v) {
        Intent i = new Intent(this, Profile.class);
        i.putExtra("userID", ((Project_18) getApplication()).getMe().getID());
        startActivity(i);
        finish();
    }

    public void updateUserSection(String text){
        LinearLayout usersLayout = (LinearLayout) findViewById(R.id.usersLayout);
        for(String key: relevantUsers.keySet()) {
            if (!(key.toLowerCase().contains(text.toLowerCase()))) {
                // hide users that don't exist if they aren't already removed
                if (buttonMap.get(key).getParent() != null) {
                    usersLayout.removeView(buttonMap.get(key));
                }
            }
            else {
                // if a user was previously removed, unremove them
                if (buttonMap.get(key).getParent() == null) {
                    usersLayout.addView(buttonMap.get(key));
                }
            }
        }
    }

    private void generateButtons() {
        LinearLayout usersLayout = (LinearLayout) findViewById(R.id.usersLayout);
        for (final String name: relevantUsers.keySet()) {
            final ImageButton b = new ImageButton(getApplicationContext());
            new Thread(new Runnable() {
                public void run() {

                    final Bitmap profPicBitmap;

                    try {
                        // pull image from FB
                        URL imageURL = new URL("https://graph.facebook.com/" +
                                relevantUsers.get(name) + "/picture?type=large");

                        // set profile picture bitmap
                        profPicBitmap = Bitmap.createScaledBitmap(
                                BitmapFactory.decodeStream(imageURL.openConnection().getInputStream()),
                                200,
                                200,
                                true);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    b.post(new Runnable() {
                        public void run() {
                            b.setImageBitmap(profPicBitmap);
                            b.setVisibility(View.VISIBLE);
                        }
                    });
                }
            }).start();

            // make button look good
            makePretty(b);

            // touch animation
            b.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    // set filter when pressed
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        b.setColorFilter(new
                                PorterDuffColorFilter(getResources().getColor(R.color.skyBlue),
                                PorterDuff.Mode.MULTIPLY));
                    } else if (event.getAction() == MotionEvent.ACTION_UP ||
                            event.getAction() == MotionEvent.ACTION_CANCEL) {
                        // remove filter when released
                        b.clearColorFilter();
                    }
                    return true;
                }
            });
            // onClick
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Log.d("myTag", "imageButton pressed");
                    // add the trail
                    ((Project_18) getApplication()).getMe().addTrail(fb, relevantUsers.get(name));
                }
            });
            buttonMap.put(name, b);
            usersLayout.addView(b);
        }
    }

    private void makePretty(ImageButton b) {
        //b.setBackground(getResources().getDrawable(R.drawable.button_pressed));
        b.setBackgroundColor(getResources().getColor(R.color.white));
        b.setPadding(40, 0, 40, 0);
    }

    @Override
    public void onResume() {
        super.onResume();
        Firebase.setAndroidContext(this);
    }


}
