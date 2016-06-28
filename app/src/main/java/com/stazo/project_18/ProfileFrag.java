package com.stazo.project_18;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.GenericTypeIndicator;
import com.firebase.client.ValueEventListener;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by ericzhang on 6/20/16.
 */
public class ProfileFrag extends Fragment {
    private View v;
    private Firebase fb;
    private User user;

    /*to be passed in through setters */
    private boolean isMe;   // is the profile we're looking at my profile?
    private String user_ID;

    /* for grabbing info from firebase */
    private Integer currentCategoryTrail;
    private String currentUserTrail;
    private static int eventsTextSize = 12;
    private ArrayList<Event> myEvents =  new ArrayList<Event>();
    private ArrayList<Event> attendingEvents = new ArrayList<Event>();
    private Toolbar toolbar;
    private ArrayList<Integer> categoryTrails = new ArrayList<Integer>();
    private ArrayList<String> userTrails = new ArrayList<String>();
    private Bitmap profPicBitmap;
    //private LinearLayout currentRow;
    private LinearLayout trailsLayout;
    //private int rowIndex = 0;
    private float startTime = System.nanoTime();
    private boolean heldDown = false;
    private ArrayList<ImageButton> nullButtons = new ArrayList<ImageButton>();
    private int SECTION_SIZE = 5;
    private int page = 0;
    private InteractiveScrollViewHorizontal scrollView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.profile, container, false);
        fb = ((Project_18) this.getActivity().getApplication()).getFB();
        page = 0;

        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        trailsLayout = (LinearLayout) v.findViewById(R.id.trailsLayout);

        // start by hiding everything
        v.findViewById(R.id.profileLayout).setVisibility(View.INVISIBLE);

        // set scrollView
        scrollView = (InteractiveScrollViewHorizontal) getActivity().findViewById(R.id.trailsScrollView);

        scrollView.setOnBottomReachedListener(
                new InteractiveScrollViewHorizontal.OnBottomReachedListener() {
                    @Override
                    public void onBottomReached() {
                        // do something
                        loadMore();
                    }
                }
        );

        grabInfo();
    }

    // gets everything going
    public void setInfo(String user_ID, boolean isMe) {
        this.user_ID = user_ID;
        this.isMe = isMe;
    }

    private synchronized void loadMore() {
        page++;
        generateTrails();
    }

    // does everything that requires data (Firebase, userId, isMe)
    private void grabInfo() {

        if (!isMe) {
            // hide me-specific info
            v.findViewById(R.id.userOptions).setVisibility(View.GONE);
            setFollowButton(Project_18.me.getUserTrails().contains(user_ID));

            v.findViewById(R.id.goToCreateEventButton).setVisibility(View.GONE);
            v.findViewById(R.id.goToAddTrailsButton).setVisibility(View.GONE);

            // set margin top to 0
            /*LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams)
                    v.findViewById(R.id.profileHeaderLayout).getLayoutParams();
            lp.topMargin = 0;
            v.findViewById(R.id.profileHeaderLayout).setLayoutParams(lp);*/

        }
        else {
            // hide not-me-specific info
            v.findViewById(R.id.followButton).setVisibility(View.GONE);
        }

        myEvents.clear();
        attendingEvents.clear();

        // for hacking Justin's account LOL
        // fb.child("Users").child("10209766334938822").

        fb.child("Users").child(this.user_ID).
                addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // set user and grab data
                        user = new User((HashMap<String, Object>) dataSnapshot.getValue());

                        // set name and bio
                        ((TextView) v.findViewById(R.id.nameTextView)).setText(user.getName());
                        ((TextView) v.findViewById(R.id.bio)).setText(user.getBio());

                        // display events
                        grabAndDisplayEvents();

                        // display trails
                        userTrails = user.getUserTrails();

                        ((TextView) v.findViewById(R.id.myTrailsTextView)).
                                setText("Following (" + userTrails.size() + ")");

                        // empty case
                        if (userTrails.isEmpty()) {
                            // set title to be visible
                            v.findViewById(R.id.trailsFullLayout).setVisibility(View.VISIBLE);
                            if (!isMe) {
                                ((TextView) v.findViewById(R.id.emptyTrailsText)).setText(
                                        user.getName() + " isn't following anyone.");
                            }
                        }
                        // not empty
                        else {
                            // remove empty textview
                            ((LinearLayout) v.findViewById(R.id.trailsFullLayout)).removeView(
                                    v.findViewById(R.id.emptyTrailsTextContainer));

                            // get pictures and display imageButtons
                            generateTrails();
                        }

                        // set profile picture
                        setProfilePicture();

                        // remove event listener
                        fb.child("Users").child(user_ID).
                                removeEventListener(this);

                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                });
    }

    private void grabAndDisplayEvents() {
        fb.child("Events").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                /* add myEvents */
                for (String event_id : user.getMyEvents()) {
                    myEvents.add(new Event(dataSnapshot.child(event_id).getValue
                            (new GenericTypeIndicator<HashMap<String, Object>>() {
                            })));
                }

                /* add attendingEvents */
                for (String event_id : user.getAttendingEvents()) {
                    attendingEvents.add(new Event(dataSnapshot.child(event_id).getValue
                            (new GenericTypeIndicator<HashMap<String, Object>>() {
                            })));
                }
                /* dynamically add button */
                LinearLayout eventsLayout = (LinearLayout) v.findViewById(R.id.eventsLayout);

                /*if (myEvents.isEmpty()) {
                    if (!isMe) {
                        ((TextView) v.findViewById(R.id.emptyHostingText)).setText(
                                user.getName() + " isn't hosting any events.");
                    }
                }

                // if myEvents is not empty, remove the empty text
                else {*/
                //}
                if (myEvents.isEmpty() && attendingEvents.isEmpty()) {
                    if (!isMe) {
                        ((TextView) v.findViewById(R.id.emptyHostingText)).setText(
                                user.getName() + " has no ongoing events");
                    }
                } else {
                    v.findViewById(R.id.emptyHostingTextContainer).setVisibility(View.GONE);
                }

                /* display myEvents */
                for (final Event e : myEvents) {
                    Button eventButton = new Button(getContext());
                    eventButton.setText(e.toString()); // + "Host");
                    makePretty(eventButton);
                    eventButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            goToEventInfo(e.getEvent_id());
                        }
                    });
                    eventsLayout.addView(eventButton);
                }

                /* display attendingEvents */
                for (final Event e : attendingEvents) {
                    Button eventButton = new Button(getContext());
                    eventButton.setText(e.toString()); // + "Attending");
                    makePretty(eventButton);
                    eventButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            goToEventInfo(e.getEvent_id());

                        }
                    });
                    eventsLayout.addView(eventButton);
                }

                fb.child("Events").removeEventListener(this);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });

    }

    private void makePretty(Button button) {
        RelativeLayout.LayoutParams lp = new
                RelativeLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        button.setTextSize(eventsTextSize);
        button.setTypeface(null, Typeface.NORMAL);
        //button.setTypeface(Typeface.MONOSPACE);
        button.setAllCaps(false);
        button.setGravity(Gravity.CENTER_VERTICAL);
        button.setPadding(120, 0, 0, 0);
        button.setLayoutParams(lp);
        //button.setBackgroundColor(getResources().getColor(R.color.white));
        button.setBackground(getResources().getDrawable(R.drawable.border_event_button));
    }

    // pull and set profile picture
    private void setProfilePicture() {
        new Thread(new Runnable() {
            public void run() {
                try {
                    URL imageURL = new URL("https://graph.facebook.com/" +
                            user.getID() + "/picture?width=" + Project_18.pictureSizeHigh);
                    profPicBitmap = BitmapFactory.decodeStream(imageURL.openConnection().getInputStream());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                final ImageView iv = (ImageView) v.findViewById(R.id.profilePicture);

                iv.post(new Runnable() {
                    public void run() {

                        // cache image
                        ((Project_18) getActivity().getApplication()).
                                addBitmapToMemoryCache(user.getID(), Bitmap.createBitmap(profPicBitmap));

                        profPicBitmap = Project_18.BITMAP_RESIZER(profPicBitmap, 400, 400);
                        iv.setImageBitmap(profPicBitmap);
                        iv.setVisibility(View.VISIBLE);

                        // unhide-layout
                        (v.findViewById(R.id.profileLayout)).setVisibility(View.VISIBLE);

                        // replace old frag now that it's loaded
                        if (!isMe) {
                            ((MainAct) getActivity()).updateOtherProfileFrag();
                        }
                    }
                });

            }
        }).start();
    }


    private void goToEventInfo(String event_id) {
        ((MainAct) getActivity()).goToEventInfo(event_id);
    }

    private void goToBrowse() {
    }

    /* go to your own profile */
    public void goToProfile() {
    }

    private void generateTrails() {
        //(new UserNamesTask(fb)).execute();
        final HashMap<String, String> idToName = new HashMap<String, String>();
        int startingPoint = page * SECTION_SIZE;
        if (startingPoint >= userTrails.size()) {
            return;
        }

        int numToLoad = Math.min(SECTION_SIZE, userTrails.size() - startingPoint);

        for (int i = page * SECTION_SIZE; ; i++) {
            if (numToLoad == 0) {
                (new SetButtonTask(idToName)).execute();
                break;
            }

            final String id = userTrails.get(i);

            idToName.put(id, Project_18.allUsers.get(id));
            numToLoad--;
        }
    }

    private class SetButtonTask extends AsyncTask<Void, Void, Void> {

        private ConcurrentHashMap<String, Bitmap> idToBitmap =
                new ConcurrentHashMap<String, Bitmap>();

        private HashMap<String, String> userList = new HashMap<String, String>();

        public SetButtonTask(HashMap<String, String> userList) {
            this.userList = userList;
        }

        @Override
        protected Void doInBackground(Void... v) {

            for (String id: userList.keySet()) {


                if (((Project_18) getActivity().getApplication()).
                        getBitmapFromMemCache(id) != null) {
                    idToBitmap.put(id, ((Project_18) getActivity().getApplication()).
                            getBitmapFromMemCache(id));
                }

                else {
                    try {
                        idToBitmap.put(id,
                                BitmapFactory.decodeStream((new URL("https://graph.facebook.com/" +
                                        id +
                                        "/picture?width=" +
                                        Project_18.pictureSize)).openConnection().getInputStream()));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void v) {

            // put the same thing in cached
            for (String id: idToBitmap.keySet()) {
                Bitmap unscaled = Bitmap.createBitmap(idToBitmap.get(id));

                // cache it
                ((Project_18) getActivity().getApplication()).addBitmapToMemoryCache(id, unscaled);

                // scale it
                idToBitmap.put(id, Project_18.BITMAP_RESIZER(unscaled,
                        180,
                        180));
            }

            constructUsersLayout(idToBitmap, userList);
            scrollView.ready();
        }
    }

    private synchronized void constructUsersLayout (ConcurrentHashMap<String, Bitmap> idToBitmap,
                                                    HashMap<String, String> idToName) {

        //trailsLayout.removeAllViews();

        // make the first row
        //currentRow = new LinearLayout(getActivity().getApplicationContext());

        // make it pretty
        //makePretty(currentRow);

        // add the first row
        //trailsLayout.addView(currentRow);

        // limits 3 buttons per row
        //rowIndex = 0;

        for (String id: idToBitmap.keySet()) {
            addToUsersLayout(idToBitmap.get(id), idToName.get(id), id);
        }
    }

    // add button to the usersLayout
    private void addToUsersLayout(final Bitmap profPicBitmap, final String name,
                                  final String id) {


        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

                // the button we'll be building
                final ImageButton b = new ImageButton(getActivity().getApplicationContext());

                b.setImageBitmap(profPicBitmap);

                // touch animation
                b.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {

                        // set filter when pressed
                        if (event.getAction() == MotionEvent.ACTION_DOWN) {
                            if (!nullButtons.contains(b)) {
                                b.setColorFilter(new
                                        PorterDuffColorFilter(getResources().getColor(R.color.colorPrimaryLight),
                                        PorterDuff.Mode.MULTIPLY));
                            }
                            heldDown = true;
                            startTime = System.nanoTime();
                        }

                        // remove filter on release/cancel
                        if (event.getAction() == MotionEvent.ACTION_CANCEL) {
                            if (!nullButtons.contains(b)) {
                                b.clearColorFilter();
                            }
                            heldDown = false;
                        }

                        // handle "release"
                        if (event.getAction() == MotionEvent.ACTION_UP) {

                            if (!nullButtons.contains(b)) {
                                b.clearColorFilter();
                            }

                            // click case
                            if (!(heldDown && System.nanoTime() - startTime > 0.5 * (1000000000))) {
                                ((MainAct) getActivity()).goToOtherProfile(id);
                            }

                            // held case
                            else {
                                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int dialogId) {
                                        // User clicked OK button

                                        if (((Project_18) getActivity().getApplication()).me.
                                                removeTrail(fb, id)) {

                                            Toast.makeText(getActivity().getApplicationContext(),
                                                    "Unfollowed " + name.split(" ")[0],
                                                    Toast.LENGTH_SHORT).show();
                                            b.setColorFilter(
                                                    getResources().getColor(R.color.colorDividerDark),
                                                    PorterDuff.Mode.MULTIPLY);
                                            nullButtons.add(b);

                                        } else {
                                            Toast.makeText(getActivity().getApplicationContext(),
                                                    "Already unfollowed " + name.split(" ")[0],
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int dialogId) {
                                        // User cancelled the dialog
                                    }
                                });
                                builder.setTitle("Unfollow " + name.split(" ")[0] + "?");
                                (builder.create()).show();
                            }

                            heldDown = false;
                        }

                        return true;
                    }
                });

                // contains button and name of the user
                LinearLayout buttonLayout = new LinearLayout(getActivity().getApplicationContext());

                // make button look good and add to buttonLayout
                makePretty(b, name, buttonLayout);

                // add to buttonMap
                //buttonMap.put(name, buttonLayout);

                // add buttonLayout to row
                //currentRow.addView(buttonLayout);

                // row index handling
                /*if (rowIndex < 3) {
                    rowIndex++;
                } else {

                    // reset index
                    rowIndex = 0;

                    // make new row
                    currentRow = new LinearLayout(getActivity().getApplicationContext());
                    makePretty(currentRow);

                    // add new row to the layout
                    trailsLayout.addView(currentRow);
                }*/
                /*ViewGroup.LayoutParams lp = new ViewGroup.
                        LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);*/
                trailsLayout.addView(buttonLayout);

                // set title to be visible
                v.findViewById(R.id.trailsFullLayout).setVisibility(View.VISIBLE);
            }
        });
    }

    private void makePretty(ImageButton b, String userName, LinearLayout buttonLayout) {
        //b.setBackground(getResources().getDrawable(R.drawable.button_pressed));
        b.setBackgroundColor(getResources().getColor(R.color.white));
        b.setPadding(40, 0, 40, 0);
        TextView tv = new TextView(getActivity().getApplicationContext());
        tv.setText(userName.split(" ")[0]);

        makePretty(tv);

        LinearLayout.LayoutParams lp =
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

    public void followUser() {
        Firebase fb = Project_18.getFB();
        if (Project_18.me.getUserTrails().contains(user_ID)) {
            Log.d("flw", "userTrails did contain id, now removing");
            Project_18.me.removeTrail(fb, user_ID);
            setFollowButton(false);
        }
        else {
            Log.d("flw", "userTrails did not contain id, now adding");
            Project_18.me.addTrail(fb, user_ID);
            setFollowButton(true);
        }
    }

    private void setFollowButton(boolean following) {
        if (following) {
            Log.d("flw", "setting to following");
            ((Button) v.findViewById(R.id.followButton)).setText("Following");
            v.findViewById(R.id.followButton).
                    setBackgroundColor(getResources().getColor(R.color.colorDividerLight));
        }

        else {
            Log.d("flw", "setting to follow");
            ((Button) v.findViewById(R.id.followButton)).setText("Follow");
            v.findViewById(R.id.followButton).
                    setBackgroundColor(getResources().getColor(R.color.colorAccent));
        }
    }



}
