package com.stazo.project_18;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
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
    private LinearLayout currentRow;
    private LinearLayout trailsLayout;
    private int rowIndex = 0;
    private float startTime = System.nanoTime();
    private boolean heldDown = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.profile, container, false);
        fb = ((Project_18) this.getActivity().getApplication()).getFB();
        // clear eventslayout


        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        trailsLayout = (LinearLayout) v.findViewById(R.id.trailsLayout);

        // start by hiding everything
        v.findViewById(R.id.profileLayout).setVisibility(View.INVISIBLE);

        // grab user and fill screen with correct info
        grabInfo();

        // set color of plus
        ((ImageButton) getActivity().findViewById(R.id.goToAddTrailsButton)).
                //setColorFilter(getResources().getColor(R.color.colorAccent));
                        setColorFilter(getResources().getColor(R.color.white));

    }

    private void grabInfo() {
        myEvents.clear();
        attendingEvents.clear();

        fb.child("Users").child(this.user_ID).
                addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // set user and grab data
                        user = new User((HashMap<String, Object>) dataSnapshot.getValue());
                        ((TextView) v.findViewById(R.id.nameTextView)).setText(user.getName());

                        Log.d("myTag", "user name is " + user.getName());

                        // display events
                        grabAndDisplayEvents();

                        // display trails
                        userTrails = user.getUserTrails();

                        // empty case
                        if (userTrails.isEmpty()) {
                            // set title to be visible
                            v.findViewById(R.id.trailsFullLayout).setVisibility(View.VISIBLE);
                        }
                        // not empty
                        else {
                            // remove empty textview
                            v.findViewById(R.id.trailsFullLayout).setVisibility(View.INVISIBLE);
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
                    Log.d("myTag", "myEvents is " + user.getMyEvents());
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
                LinearLayout attendingLayout = (LinearLayout) v.findViewById(R.id.attendingLayout);
                //myEvents.clear();
                //attendingEvents.clear();

                /* no adventures case */
                if (myEvents.isEmpty() && attendingEvents.isEmpty()) {
                    Button eventButton = new Button(getContext());
                    eventButton.setText("No adventures, no worries! " +
                            "Just tap to explore.");
                    makePretty(eventButton);
                    eventButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            goToBrowse();
                        }
                    });
                    eventsLayout.addView(eventButton);

                    // unhide-layout
                    (v.findViewById(R.id.profileLayout)).setVisibility(View.VISIBLE);

                    fb.child("Events").removeEventListener(this);
                    return;
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
                    attendingLayout.addView(eventButton);
                }

                // unhide-layout
                (v.findViewById(R.id.profileLayout)).setVisibility(View.VISIBLE);

                /*// set hosting background
                getActivity().findViewById(R.id.hostingLayout).setBackground(
                        getResources().getDrawable(R.drawable.border_layout));*/

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
        //button.setTypeface(Typeface.MONOSPACE);
        button.setAllCaps(false);
        button.setGravity(Gravity.CENTER);
        button.setLayoutParams(lp);
        button.setBackgroundColor(getResources().getColor(R.color.white));
    }

    // pull and set profile picture
    private void setProfilePicture() {
        new Thread(new Runnable() {
            public void run() {
                try {
                    URL imageURL = new URL("https://graph.facebook.com/" + user.getID() + "/picture?type=large");
                    profPicBitmap = Bitmap.createScaledBitmap(
                            BitmapFactory.decodeStream(imageURL.openConnection().getInputStream()),
                            200,
                            200,
                            true);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                final ImageView iv = (ImageView) v.findViewById(R.id.profilePicture);

                iv.post(new Runnable() {
                    public void run() {
                        iv.setImageBitmap(profPicBitmap);
                        iv.setVisibility(View.VISIBLE);
                    }
                });
            }
        }).start();
    }


    private void goToEventInfo(String event_id) {
        ((MainAct) getActivity()).goToEventInfo(event_id);
    }

    public void goToAddTrails(View view) {
//        Intent i = new Intent(this, AddTrailsAct.class);
//        //i.putExtra("userID", user.getID());
//        startActivity(i);
    }

    private void goToBrowse() {
    }

    /* go to your own profile */
    public void goToProfile() {
//        goToProfile(((Project_18) getApplication()).getMe().getID(), true);
    }

    /* go to someone else's profile */
    public void goToProfile(String userID, boolean isMe) {
//        Intent i = new Intent(this, Profile.class);
//        i.putExtra("userID", userID);
//        i.putExtra("isMe", isMe);
//        startActivity(i);

        ProfileFrag profileFrag = new ProfileFrag();
        profileFrag.setUser_ID(userID);
        profileFrag.setIsMe(isMe);
        FragmentTransaction transaction = this.getActivity().getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.show_viewProfile, profileFrag).addToBackStack("CreateEventFrag").commit();
    }

    public void setIsMe(boolean isMe) {
        this.isMe = isMe;
    }

    public void setUser_ID(String user_ID) {
        this.user_ID = user_ID;
    }

    private void generateTrails() {
        //(new UserNamesTask(fb)).execute();
        final HashMap<String, String> nameToId = new HashMap<String, String>();

        for (final String id : userTrails) {
            fb.child("Users").child(id).child("name").
                    addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            nameToId.put((String) dataSnapshot.getValue(), id);
                            if (nameToId.keySet().size() == userTrails.size()) {
                                Log.d("newIsh", "userTrails is " + userTrails.toString());
                                Log.d("newIsh", "nameToId is " + nameToId.toString());
                                (new SetButtonTask(nameToId)).execute();
                            }
                        }

                        @Override
                        public void onCancelled(FirebaseError firebaseError) {

                        }
                    });
        }
    }

    private class SetButtonTask extends AsyncTask<Void, Void, Void> {

        private ConcurrentHashMap<String, Bitmap> nameToBitmap =
                new ConcurrentHashMap<String, Bitmap>();
        private HashMap<String, String> userList = new HashMap<String, String>();

        public SetButtonTask(HashMap<String, String> userList) {
            this.userList = userList;
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
        }
    }

    private synchronized void constructUsersLayout (ConcurrentHashMap<String, Bitmap> nameToBitmap,
                                                    HashMap<String, String> nameToId) {

        // make the first row
        currentRow = new LinearLayout(getActivity().getApplicationContext());

        // make it pretty
        makePretty(currentRow);

        // add the first row
        trailsLayout.addView(currentRow);

        // limits 3 buttons per row
        rowIndex = 0;

        for (String name: nameToBitmap.keySet()) {
            addToUsersLayout(nameToBitmap.get(name), name, nameToId.get(name));
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
                            b.setColorFilter(new
                                    PorterDuffColorFilter(getResources().getColor(R.color.colorPrimaryLight),
                                    PorterDuff.Mode.MULTIPLY));
                            heldDown = true;
                            startTime = System.nanoTime();
                        }

                        // remove filter on release/cancel
                        if (event.getAction() == MotionEvent.ACTION_CANCEL) {
                            b.clearColorFilter();
                            heldDown = false;
                        }

                        // handle "release"
                        if (event.getAction() == MotionEvent.ACTION_UP) {
                            Log.d("myTag", "imageButton pressed");
                            b.clearColorFilter();

                            // start dialog
                            if (heldDown && System.nanoTime() - startTime > 0.5 * (1000000000)) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int dialogId) {
                                        // User clicked OK button

                                        if (((Project_18) getActivity().getApplication()).getMe().
                                                removeTrail(fb, id)) {
                                            Toast.makeText(getActivity().getApplicationContext(),
                                                    "Unfollowed " + name.split(" ")[0],
                                                    Toast.LENGTH_SHORT).show();
                                            b.setColorFilter(
                                                    getResources().getColor(R.color.colorDividerDark),
                                                    PorterDuff.Mode.MULTIPLY);
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
                currentRow.addView(buttonLayout);

                // row index handling
                if (rowIndex < 3) {
                    rowIndex++;
                } else {

                    // reset index
                    rowIndex = 0;

                    // make new row
                    currentRow = new LinearLayout(getActivity().getApplicationContext());
                    makePretty(currentRow);

                    // add new row to the layout
                    trailsLayout.addView(currentRow);
                }

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


}
