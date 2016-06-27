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
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
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
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


public class AddTrailsAct extends AppCompatActivity {

    private SearchView searchView = null;
    private SearchView.OnQueryTextListener queryTextListener;
    private HashMap<String, String> relevantUsers = new HashMap<String, String>();  // id to name
    private HashMap<String, String> allUsers = new HashMap<String, String>();  // id to name
    private HashMap<String, String> friends = new HashMap<String, String>();  // id to name

    //private ArrayList<LinearLayout> rows = new ArrayList<LinearLayout>();
    private Firebase fb;
    private LinearLayout currentRow;
    private LinearLayout usersLayout;
    private InteractiveScrollView scrollView;
    private int rowIndex;
    private AddTrailsAct instance = this;
    private SetButtonTask currentTask = null;
    private boolean changedTrails = false;
    private int numToLoad = 0;
    private int pageNumber = 0;
    private static final int SECTION_SIZE = 12;
    //private Bitmap profPicBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_trails);

        // get firebase reference
        fb = ((Project_18) getApplication()).getFB();

        // set usersLayout
        usersLayout = (LinearLayout) findViewById(R.id.usersLayout);

        // set scrollView
        scrollView = (InteractiveScrollView) findViewById(R.id.addTrailsScrollView);

        scrollView.setOnBottomReachedListener(
                new InteractiveScrollView.OnBottomReachedListener() {
                    @Override
                    public void onBottomReached() {
                        // do something
                        loadMore();
                    }
                }
        );

        // search view things
        SearchManager searchManager = (SearchManager) this.getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) findViewById(R.id.searchView);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(this.getComponentName()));
        queryTextListener = new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
                pageNumber = 0;
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

        pullUsersAndLoad(fb);
    }

    public void pullUsersAndLoad(Firebase fb) {
        fb.child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> usersIterable = dataSnapshot.getChildren();
                for (DataSnapshot user: usersIterable) {
                    //System.out.println(commentIterable.iterator().next().getValue());
                    allUsers.put(user.getKey(), (String) user.child("name").getValue());
                }

                // get allUsers
                //allUsers = (HashMap<String, String>) idToName.getValue();

                // set friends
                friends = new HashMap(((Project_18) getApplication()).getMe().getFriends());

                // testing purposes
                generateFakeAccounts();

                // take out users we are already following
                filterUsers();

                // relevantUsers = the ones displayed
                relevantUsers = new HashMap<String, String>(allUsers);

                generateButtons(true);

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    public void generateFakeAccounts() {
        allUsers.put("0", "James");
        allUsers.put("1", "James");
        allUsers.put("2", "James");
        allUsers.put("3", "James");
        allUsers.put("4", "James");
        allUsers.put("5", "James");
        allUsers.put("6", "James");
        allUsers.put("7", "James");
        allUsers.put("8", "James");
        allUsers.put("9", "James");
        allUsers.put("10", "James");
        allUsers.put("11", "James");
        allUsers.put("12", "James");
        allUsers.put("13", "James");
        allUsers.put("14", "James");
        allUsers.put("15", "James");
        allUsers.put("16", "James");
        allUsers.put("17", "James");
        allUsers.put("18", "James");
        allUsers.put("19", "James");
        allUsers.put("20", "James");
        allUsers.put("21", "James");
        allUsers.put("22", "James");
        allUsers.put("23", "James");
        allUsers.put("24", "James");
        allUsers.put("25", "James");
        allUsers.put("26", "James");
        allUsers.put("27", "James");
        allUsers.put("28", "James");
        allUsers.put("29", "James");
        allUsers.put("30", "James");

        /*allUsers.put("1070949549640758", "Gates Zeng");
        allUsers.put("1076100269116381", "Eric Zhang");
        allUsers.put("1131880253542315", "Luke Thomas");
        allUsers.put("1138117392898486", "Matthew Ung");
        allUsers.put("1177156832304841", "Ansel Blume");
        allUsers.put("1184188798300386", "Brian Chan");
        allUsers.put("1196215920412322", "Isaac Wang");
        allUsers.put("10209766334938822", "Justin Ang");*/
    }

    /*public void addCategoryTrail(View v) {
        Integer trailNum = Integer.parseInt((String) v.getTag());
        ((Project_18) getApplication()).getMe().addTrail(fb, trailNum - 1);
    }*/

    public void goToProfile(View v) {

        //onBackPressed();
        Intent i = new Intent(this, MainAct.class);
        i.putExtra("toProfile", true);
        startActivity(i);
    }

    // removes users from allUsers if you're already following them or if they're you
    public void filterUsers() {
        for (String id: ((Project_18) getApplication()).getMe().getUserTrails()) {
            if (allUsers.keySet().contains(id)) {
                allUsers.remove(id);
            }
        }
        allUsers.remove(Project_18.me.getID());

    }

    private ArrayList<String> sortByFriends(Collection<String> users) {
        ArrayList<String> sorted = new ArrayList<>();
        for (String id: users) {
            if (friends.containsKey(id)) {
                sorted.add(0, id);
            }
            else {
                sorted.add(id);
            }
        }
        return sorted;
    }

    private void loadMore() {
        pageNumber ++;
        generateButtons(false);
    }

    // clears user section and updates it with new relevantUsers
    public void updateUserSection(String text){

        relevantUsers.clear();

        /*for(String key: allUsers.keySet()) {
            if ((key.toLowerCase().contains(text.toLowerCase()))) {
                relevantUsers.put(key, allUsers.get(key));
            }
        }*/

        // filter users that match
        for(String id: allUsers.keySet()) {
            if ((allUsers.get(id).toLowerCase().contains(text.toLowerCase()))) {
                relevantUsers.put(id, allUsers.get(id));
            }
        }


        // instance is a private reference to this AddTrailsAct, probably doesn't matter
        instance.generateButtons(true);
    }

    // generates buttons
    private synchronized void generateButtons(boolean clearLayout) {

        // optimization
        if (currentTask != null) {
            Log.d("cancel", "task CANCELLED");
            currentTask.cancel(true);
        }
        currentTask = new SetButtonTask(relevantUsers, clearLayout, pageNumber * 12);
        Log.d("cancel", "task EXECUTING...");
        currentTask.execute();
    }

    private class SetButtonTask extends AsyncTask<Void, Void, Void> {

        private ConcurrentHashMap<String, String> userList =
                new ConcurrentHashMap<String, String>();

        private ConcurrentHashMap<String, Bitmap> idToBitmap =
                new ConcurrentHashMap<String, Bitmap>();

        private boolean clearLayout;

        private int startingPoint;

        public SetButtonTask(HashMap<String, String> userList, boolean clearLayout, int startingPoint) {
            for (String key: userList.keySet()) {
                this.userList.put(key, userList.get(key));
            }
            this.clearLayout = clearLayout;
            this.startingPoint = startingPoint;
        }

        @Override
        protected Void doInBackground(Void... v) {
            numToLoad = SECTION_SIZE;


            ArrayList<String> ids = new ArrayList<>(userList.keySet());
            ids = sortByFriends(ids);


            // stop if we run out of entries
            for (int i = startingPoint; i < userList.keySet().size();i++) {

                final String id = ids.get(i);

                if (isCancelled()) {
                    break;
                }
                // stop after we've loaded 12
                if (numToLoad == 0) {
                    break;
                }

                if (((Project_18) getApplication()).cachedIdToBitmap.keySet().contains(id)) {
                    Log.d("check", "we have cached entry number " + i);
                    idToBitmap.put(id, ((Project_18) getApplication()).cachedIdToBitmap.get(id));
                    numToLoad--;
                }

                else {
                    try {
                        Log.d("check", "we are processing entry number " + i);

                        // FOR TESTING PURPOSES
                        if (id.length() < 5) {
                            idToBitmap.put(id, Bitmap.createScaledBitmap(
                                    BitmapFactory.decodeStream((new URL("https://graph.facebook.com/" +
                                            "1196215920412322" +
                                            "/picture?type=large")).openConnection().getInputStream()),
                                    180,
                                    180,
                                    true));
                            numToLoad--;
                        }
                        else {
                            Log.d("check", "id: " + id);
                            idToBitmap.put(id, Bitmap.createScaledBitmap(
                                    BitmapFactory.decodeStream((new URL("https://graph.facebook.com/" +
                                            id +
                                            "/picture?type=large")).openConnection().getInputStream()),
                                    180,
                                    180,
                                    true));
                            numToLoad--;
                        }

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }

            }

            return null;
        }

        @Override
        protected void onPostExecute(Void v) {

            // iteration
            for (String id: idToBitmap.keySet()) {
                // store in cache
                ((Project_18) getApplication()).cachedIdToBitmap.put(id, idToBitmap.get(id));

            }

            constructUsersLayout(idToBitmap, userList, clearLayout);
            currentTask = null;
            InteractiveScrollView.isReady = true;
            Log.d("cancel", "task COMPLETED...");
        }
    }

    private synchronized void constructUsersLayout (ConcurrentHashMap<String, Bitmap> idToBitmap,
                                                    ConcurrentHashMap<String, String> userList,
                                                    boolean clearLayout) {

        // clear layout
        if (clearLayout) {
            usersLayout.removeAllViewsInLayout();
        }

        if (idToBitmap.isEmpty()) {
            return;
        }

        // make the first row
        currentRow = new LinearLayout(getApplicationContext());

        // make it pretty
        makePretty(currentRow);

        // add the first row
        usersLayout.addView(currentRow);

        // limits 3 buttons per row
        rowIndex = 0;

        // sort by friends again
        ArrayList<String> ids = sortByFriends(idToBitmap.keySet());

        for (String id: ids) {
            instance.addToUsersLayout(idToBitmap.get(id), userList.get(id), id);
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
