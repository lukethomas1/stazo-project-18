package com.stazo.project_18;

import android.app.ActionBar;
import android.app.Activity;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.PorterDuffXfermode;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.os.AsyncTaskCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.firebase.client.Firebase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;


public class AddTrailsAct extends AppCompatActivity {

    private SearchView searchView = null;
    private SearchView.OnQueryTextListener queryTextListener;
    private HashMap<String, String> relevantUsers = new HashMap<String, String>();  // name to id
    private HashMap<String, String> allUsers = new HashMap<String, String>();  // name to id

    private HashMap<String, View> buttonMap = new HashMap<String, View>(); // name to buttonLayout
    //private ArrayList<LinearLayout> rows = new ArrayList<LinearLayout>();
    private Firebase fb;
    private LinearLayout currentRow;
    private LinearLayout usersLayout;
    private int rowIndex;
    private AddTrailsAct instance = this;
    private SetButtonTask currentTask = null;
    private boolean changedTrails = false;
    //private Bitmap profPicBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_trails);

        // get firebase reference
        fb = ((Project_18) getApplication()).getFB();

        // get allUsers
        allUsers = ((Project_18) getApplication()).getMe().getFriends();

        Log.d("shit", "Project_18.me trails is size " +
                ((Project_18) getApplication()).me.getUserTrails().size());

        // take out users we are already following
        filterFollowedUsers();

        // relevantUsers = the ones displayed
        relevantUsers = new HashMap<String, String>(allUsers);

        // set usersLayout
        usersLayout = (LinearLayout) findViewById(R.id.usersLayout);

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
                updateUserSection(query);
                // hide keyboard
                searchView.clearFocus();
                return true;
            }
        };

        SearchView.OnCloseListener closeListener = new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                updateUserSection("");
                return false;
            }
        };

        searchView.setOnCloseListener(closeListener);
        searchView.setOnQueryTextListener(queryTextListener);
    }

    /*public void addCategoryTrail(View v) {
        Integer trailNum = Integer.parseInt((String) v.getTag());
        ((Project_18) getApplication()).getMe().addTrail(fb, trailNum - 1);
    }*/

    public void goToProfile(View v) {
        //if (!changedTrails) {
            onBackPressed();
        //}
        /*else {
            Intent i = new Intent(this, MainAct.class);
            i.putExtra("toProfile", true);
            startActivity(i);
        }*/
    }
    public void filterMe() {
        for (String id: allUsers.values()) {
            if (id.equals(((Project_18) getApplication()).getMe().getID())) {
                System.out.println("Is me, filtering out " + id);
                allUsers.values().remove(id);
                return;
            }
        }
    }

    // removes users from allUsers if you're already following them
    public void filterFollowedUsers() {
        for (String id: ((Project_18) getApplication()).getMe().getUserTrails()) {
            if (allUsers.values().contains(id)) {
                System.out.println("Already following, filtering out " + id);
                allUsers.values().remove(id);
            }
        }
    }

    public void updateUserSection(String text){

        relevantUsers.clear();

        for(String key: allUsers.keySet()) {
            if ((key.toLowerCase().contains(text.toLowerCase()))) {
                relevantUsers.put(key, allUsers.get(key));
            }
        }

        // instance is a private reference to this AddTrailsAct, probably doesn't matter
        instance.generateButtons();
    }

    private synchronized void generateButtons() {
        // optimization
        if (currentTask != null) {
            currentTask.cancel(true);
        }
        currentTask = new SetButtonTask(relevantUsers);
        currentTask.execute();
    }

    private class SetButtonTask extends AsyncTask<Void, Void, Void> {

        private ConcurrentHashMap<String, String> userList =
            new ConcurrentHashMap<String, String>();

        private ConcurrentHashMap<String, Bitmap> nameToBitmap =
                new ConcurrentHashMap<String, Bitmap>();

        public SetButtonTask(HashMap<String, String> userList) {
            for (String key: userList.keySet()) {
                this.userList.put(key, userList.get(key));
            }
        }

        @Override
        protected Void doInBackground(Void... v) {

            for (String name: userList.keySet()) {
                try {
                    nameToBitmap.put(name, Bitmap.createScaledBitmap(
                            BitmapFactory.decodeStream((new URL("https://graph.facebook.com/" +
                                    userList.get(name) +
                                    "/picture?type=large")).openConnection().getInputStream()),
                            180,
                            180,
                            true));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            constructUsersLayout(nameToBitmap, userList);
            currentTask = null;
        }
    }

    private synchronized void constructUsersLayout (ConcurrentHashMap<String, Bitmap> nameToBitmap,
                                                    ConcurrentHashMap<String, String> userList) {

        // clear layout
        usersLayout.removeAllViewsInLayout();

        // make the first row
        currentRow = new LinearLayout(getApplicationContext());

        // make it pretty
        makePretty(currentRow);

        // add the first row
        usersLayout.addView(currentRow);

        // limits 3 buttons per row
        rowIndex = 0;

        for (String name: nameToBitmap.keySet()) {
            instance.addToUsersLayout(nameToBitmap.get(name), name, userList.get(name));
        }
    }

    // add button to the usersLayout
    private void addToUsersLayout(final Bitmap profPicBitmap, final String name,
                                  final String id) {


        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                // the button we'll be building
                final ImageButton b = new ImageButton(getApplicationContext());

                b.setImageBitmap(profPicBitmap);

                // touch animation
                b.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        // set filter when pressed
                        if (event.getAction() == MotionEvent.ACTION_DOWN) {
                            b.setColorFilter(new
                                    PorterDuffColorFilter(getResources().getColor(R.color.colorPrimaryLight),
                                    PorterDuff.Mode.MULTIPLY));
                        }

                        // handle "click"
                        if (event.getAction() == MotionEvent.ACTION_UP) {
                            Log.d("myTag", "imageButton pressed");

                            // add the trail

                            // already added
                            if (!((Project_18) getApplication()).me.addTrail(fb, id)) {
                                ((Project_18) getApplication()).me.removeTrail(fb, id);
                                /*Toast.makeText(getApplicationContext(),
                                        "No longer following " + name.split(" ")[0],
                                        Toast.LENGTH_SHORT).show();*/
                                changedTrails = true;

                                // remove border
                                b.setBackgroundResource(0);
                            }

                            // not yet added
                            else {
                                /*Toast.makeText(getApplicationContext(),
                                        "Followed " + name.split(" ")[0],
                                        Toast.LENGTH_SHORT).show();*/

                                // add border
                                b.setBackground(getResources().
                                        getDrawable(R.drawable.border_add_trails_button));
                            }
                        }

                        // remove filter on release/cancel
                        if (event.getAction() == MotionEvent.ACTION_UP ||
                                event.getAction() == MotionEvent.ACTION_CANCEL) {
                            b.clearColorFilter();
                        }
                        return true;
                    }
                });

                // contains button and name of the user
                LinearLayout buttonLayout = new LinearLayout(getApplicationContext());

                // make button look good and add to buttonLayout
                makePretty(b, name, buttonLayout);

                // add to buttonMap
                buttonMap.put(name, buttonLayout);

                // add buttonLayout to row
                currentRow.addView(buttonLayout);

                // row index handling
                if (rowIndex < 3) {
                    rowIndex ++;
                } else {

                    // reset index
                    rowIndex = 0;

                    // make new row
                    currentRow = new LinearLayout(getApplicationContext());
                    makePretty(currentRow);

                    // add new row to the layout
                    usersLayout.addView(currentRow);
                }
            }
        });
    }

    private void makePretty(ImageButton b, String userName, LinearLayout buttonLayout) {
        //b.setBackground(getResources().getDrawable(R.drawable.button_pressed));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(25,0,25,0);

        b.setBackgroundColor(getResources().getColor(R.color.white));
        b.setPadding(6, 6, 6, 6);
        b.setLayoutParams(lp);
        TextView tv = new TextView(getApplicationContext());
        tv.setText(userName.split(" ")[0]);

        makePretty(tv);

        LinearLayout.LayoutParams lp2 =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);

        buttonLayout.setOrientation(LinearLayout.VERTICAL);

        buttonLayout.addView(b);
        buttonLayout.addView(tv);
    }

    private void makePretty(TextView tv) {
        tv.setTextColor(getResources().getColor(R.color.colorTextPrimary));
        tv.setGravity(Gravity.CENTER_HORIZONTAL);
    }

    private void makePretty(LinearLayout row) {
        /*LinearLayout.LayoutParams lp =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);*/

        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, 0, 0, 40);
        row.setGravity(Gravity.CENTER_HORIZONTAL);
    }

    @Override
     public void onResume() {
        super.onResume();
        Firebase.setAndroidContext(this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }


}
