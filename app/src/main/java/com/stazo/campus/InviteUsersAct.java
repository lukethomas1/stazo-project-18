package com.stazo.campus;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.view.View;
import android.widget.TextView;

import com.firebase.client.Firebase;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;


public class InviteUsersAct extends AppCompatActivity {

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
    private SetButtonTask currentTask = null;
    private int numToLoad = 0;
    private int pageNumber = 0;
    private static final int SECTION_SIZE = 16;
    private ArrayList<String> invitedUserIds = new ArrayList<String>();
    public static Event event;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.invite_users);

        // get firebase reference
        fb = ((Campus) getApplication()).getFB();

        // set usersLayout
        usersLayout = (LinearLayout) findViewById(R.id.usersLayout);

        // set title text
        ((TextView) findViewById(R.id.eventNameText)).setText(event.getName());

        // set scrollView
        scrollView = (InteractiveScrollView) findViewById(R.id.inviteUsersScrollView);

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

        // all Users = friends + people I'm following + people who are following me

        friends = new HashMap(((Campus) getApplication()).getMe().getFriends());
        allUsers.putAll(friends);

        for (String id: ((Campus) getApplication()).getMe().getUserTrails()) {
            if (Campus.allUsers.containsKey(id)) {
                allUsers.put(id, Campus.allUsers.get(id));
            }
        }

        for (String id: ((Campus) getApplication()).getMe().getUserFollowers()) {
            if (Campus.allUsers.containsKey(id)) {
                allUsers.put(id, Campus.allUsers.get(id));
            }
        }

        if (allUsers.containsKey(event.getCreator_id())) {
            allUsers.remove(event.getCreator_id());
        }

        // relevantUsers = the ones displayed
        relevantUsers = new HashMap<String, String>(allUsers);

        generateButtons(true);
    }

    public void goToProfile(View v) {

        //onBackPressed();
        Intent i = new Intent(this, MainAct.class);
        i.putExtra("toProfile", true);
        startActivity(i);
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
        generateButtons(true);
    }

    // generates buttons
    private synchronized void generateButtons(boolean clearLayout) {
        // optimization
        if (currentTask != null) {
            currentTask.cancel(true);
        }
        currentTask = new SetButtonTask(relevantUsers, clearLayout, pageNumber * 12);
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

                if (((Campus) getApplication()).getBitmapFromMemCache(id) != null) {
                    idToBitmap.put(id, ((Campus) getApplication()).getBitmapFromMemCache(id));
                    numToLoad--;
                }

                else {
                    try {
                        idToBitmap.put(id,
                                BitmapFactory.decodeStream((new URL("https://graph.facebook.com/" +
                                        id +
                                        "/picture?width=" +
                                        Campus.pictureSizeLow)).openConnection().getInputStream()));
                        numToLoad--;


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
                ((Campus) getApplication()).addBitmapToMemoryCache(id, idToBitmap.get(id));

                idToBitmap.put(id, Campus.BITMAP_RESIZER(idToBitmap.get(id), 180, 180));

            }

            constructUsersLayout(idToBitmap, userList, clearLayout);
            currentTask = null;
            scrollView.ready();
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
            addToUsersLayout(idToBitmap.get(id), userList.get(id), id);
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

                            // add the trail

                            // already added
                            if (invitedUserIds.contains(id)) {
                                invitedUserIds.remove(id);

                                // remove border
                                b.setBackgroundResource(0);
                            }

                            // not yet added
                            else {

                                // add to invite list
                                invitedUserIds.add(id);

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

    public void inviteUsers(View v) {
        ArrayList<String> meList = new ArrayList<>();
        meList.add(Campus.me.getName());
        (new NotificationInviteEvent(Notification2.TYPE_INVITE_EVENT, meList,
                event.getEvent_id(),
                event.getName(),
                Campus.me.getID())).pushToFirebase(fb, invitedUserIds);
        finish();
    }


}
