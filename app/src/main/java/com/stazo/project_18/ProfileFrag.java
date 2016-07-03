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
import android.net.Uri;
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
import android.widget.FrameLayout;
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
    private static int eventsTextSize = 16;
    private static int detailsTextSize = 12;
    private static int followingImageSize = 120;
    private static int followersImageSize = 120;

    private ArrayList<Event> myEvents =  new ArrayList<Event>();
    private ArrayList<Event> attendingEvents = new ArrayList<Event>();
    private Toolbar toolbar;
    private ArrayList<Integer> categoryTrails = new ArrayList<Integer>();
    private ArrayList<String> userTrails = new ArrayList<String>();
    private ArrayList<String> userFollowers = new ArrayList<String>();
    private Bitmap profPicBitmap;
    //private LinearLayout currentRow;
    private LinearLayout trailsLayout;
    private LinearLayout followersLayout;
    //private int rowIndex = 0;
    private float startTime = System.nanoTime();
    private boolean heldDown = false;
    private ArrayList<ImageButton> nullButtons = new ArrayList<ImageButton>();
    private int SECTION_SIZE = 8;
    private int page = 0;
    private int page2 = 0;
    private InteractiveScrollViewHorizontal scrollViewTrails;
    private InteractiveScrollViewHorizontal scrollViewFollowers;


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
        followersLayout = (LinearLayout) v.findViewById(R.id.followersLayout);

        // start by hiding everything
        v.findViewById(R.id.profileLayout).setVisibility(View.INVISIBLE);

        // set scrollView
        scrollViewTrails = (InteractiveScrollViewHorizontal)
                getActivity().findViewById(R.id.trailsScrollView);
        scrollViewFollowers = (InteractiveScrollViewHorizontal)
                getActivity().findViewById(R.id.followersScrollView);

        scrollViewTrails.setOnBottomReachedListener(
                new InteractiveScrollViewHorizontal.OnBottomReachedListener() {
                    @Override
                    public void onBottomReached() {
                        // do something
                        loadMoreTrails();
                    }
                }
        );
        scrollViewFollowers.setOnBottomReachedListener(
                new InteractiveScrollViewHorizontal.OnBottomReachedListener() {
                    @Override
                    public void onBottomReached() {
                        // do something
                        loadMoreFollowers();
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

    private synchronized void loadMoreTrails() {
        if ((page+1) * SECTION_SIZE < userTrails.size()) {
            page++;
            generateTrails();
        }
    }
    private synchronized void loadMoreFollowers() {
        if ((page2+1) * SECTION_SIZE < userFollowers.size()) {
            page2++;
            generateFollowers();
        }
    }

    // does everything that requires data (Firebase, userId, isMe)
    private void grabInfo() {

        if (!isMe) {
            // hide me-specific info
            v.findViewById(R.id.userOptions).setVisibility(View.GONE);
            setFollowButton(Project_18.me.getUserTrails().contains(user_ID));

            v.findViewById(R.id.goToCreateEventButton).setVisibility(View.GONE);
            v.findViewById(R.id.goToAddTrailsButton).setVisibility(View.GONE);

            // set top margin
            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams)
                    (v.findViewById(R.id.profileLayout)).getLayoutParams();
            lp.topMargin = MainAct.viewPager.getTop();

            v.findViewById(R.id.profileLayout).setLayoutParams(lp);
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

                        userFollowers = user.getUserFollowers();

                        ((TextView) v.findViewById(R.id.myTrailsTextView)).
                                setText("Following (" + userTrails.size() + ")");
                        ((TextView) v.findViewById(R.id.myFollowersTextView)).
                                setText("Followers (" + userFollowers.size() + ")");

                        // empty case
                        if (userTrails.isEmpty()) {
                            // set title to be visible
                            v.findViewById(R.id.trailsFullLayout).setVisibility(View.VISIBLE);
                            if (!isMe) {
                                ((TextView) v.findViewById(R.id.emptyTrailsText)).setText(
                                        user.getName().split(" ")[0] + " isn't following anyone.");
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

                        // empty case
                        if (userFollowers.isEmpty()) {
                            // set title to be visible
                            v.findViewById(R.id.followersFullLayout).setVisibility(View.VISIBLE);
                            if (!isMe) {
                                ((TextView) v.findViewById(R.id.emptyFollowersText)).setText(
                                        user.getName().split(" ")[0] + " has no followers.");
                            }
                        }
                        // not empty
                        else {
                            // remove empty textview
                            ((LinearLayout) v.findViewById(R.id.followersFullLayout)).removeView(
                                    v.findViewById(R.id.emptyFollowersTextContainer));

                            // get pictures and display imageButtons
                            generateFollowers();
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
                    Log.d("debug", event_id);
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

                ArrayList<Event> allEvents = new ArrayList<Event>();
                allEvents.addAll(myEvents);
                allEvents.addAll(attendingEvents);

                if (allEvents.isEmpty()) {
                    if (!isMe) {
                        ((TextView) v.findViewById(R.id.emptyHostingText)).setText(
                                user.getName().split(" ")[0] + " has no ongoing events");
                    }
                } else {
                    v.findViewById(R.id.emptyHostingTextContainer).setVisibility(View.GONE);
                }

                /* display myEvents */
                for (final Event e : allEvents) {

                    LinearLayout container = new LinearLayout(getActivity());
                    EventButtonOnTouchListener listener = new EventButtonOnTouchListener(e, container);

                    ((Project_18) getActivity().getApplication()).makeEventButton
                            (getActivity(), e, container, listener, true, Project_18.me);

                    eventsLayout.addView(container);
                }

                fb.child("Events").removeEventListener(this);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });
    }

    /*private void makePretty(TextView eventName, ImageView iv, TextView numGoing,
                            TextView info, TextView hostOrJoined, LinearLayout container) {

        LinearLayout.LayoutParams containerLP = new LinearLayout.
                LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        containerLP.gravity = Gravity.CENTER_VERTICAL;
        container.setLayoutParams(containerLP);
        container.setOrientation(LinearLayout.HORIZONTAL);
        container.setBackground(getResources().getDrawable(R.drawable.border_event_button));
        //container.setBackgroundColor(getResources().getColor(R.color.colorPrimary));

        LinearLayout eventNameAndInfo = new LinearLayout(getActivity());
        LinearLayout.LayoutParams lp = new
                LinearLayout.LayoutParams(950,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.gravity=Gravity.NO_GRAVITY;
        eventNameAndInfo.setLayoutParams(lp);
        eventNameAndInfo.setOrientation(LinearLayout.VERTICAL);

        LinearLayout nameAndHost = new LinearLayout(getActivity());
        LinearLayout.LayoutParams nameAndHostLP = new
                LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        nameAndHost.setLayoutParams(nameAndHostLP);
        nameAndHost.setOrientation(LinearLayout.HORIZONTAL);

        hostOrJoined.setPadding(50, 0, 0, 60);
        hostOrJoined.setTextSize(detailsTextSize);
        hostOrJoined.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams hostLP = new
                LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        //hostLP.gravity = Gravity.RIGHT;
        hostOrJoined.setLayoutParams(hostLP);

        eventName.setTextSize(eventsTextSize);
        eventName.setTypeface(null, Typeface.NORMAL);
        eventName.setGravity(Gravity.CENTER_VERTICAL);
        eventName.setPadding(80, 0, 0, 0);
        eventName.setWidth(740);
        eventName.setAllCaps(false);
        eventName.setBackground(null);

        nameAndHost.addView(eventName);
        nameAndHost.addView(hostOrJoined);

        info.setTextColor(getResources().getColor(R.color.colorDivider));
        info.setTextSize(detailsTextSize);
        info.setPadding(160, 0, 0, 20);
        info.setGravity(Gravity.CENTER_VERTICAL);

        eventNameAndInfo.addView(nameAndHost);
        eventNameAndInfo.addView(info);

        LinearLayout.LayoutParams numGoingLP=new LinearLayout.
                LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        numGoingLP.gravity = Gravity.CENTER_VERTICAL;
        numGoingLP.rightMargin = 20;
        numGoing.setLayoutParams(numGoingLP);
        numGoing.setTextColor(getResources().getColor(R.color.colorAccentDark));

        LinearLayout.LayoutParams ivLP=new LinearLayout.
                LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        ivLP.gravity=Gravity.CENTER_VERTICAL;
        ivLP.rightMargin = 20;
        iv.setLayoutParams(ivLP);
        iv.setColorFilter(getResources().getColor(R.color.colorTextPrimary));

        container.addView(eventNameAndInfo);
        container.addView(iv);
        container.addView(numGoing);
    }*/

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
        ((MainAct) getActivity()).goToEventInfo(event_id, true);
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
                (new SetButtonTask(idToName, true)).execute();
                break;
            }

            final String id = userTrails.get(i);

            idToName.put(id, Project_18.allUsers.get(id));
            numToLoad--;
        }
    }

    private void generateFollowers() {
        //(new UserNamesTask(fb)).execute();
        final HashMap<String, String> idToName = new HashMap<String, String>();
        int startingPoint = page2 * SECTION_SIZE;
        if (startingPoint >= userFollowers.size()) {
            return;
        }

        int numToLoad = Math.min(SECTION_SIZE, userFollowers.size() - startingPoint);

        for (int i = page2 * SECTION_SIZE; ; i++) {
            if (numToLoad == 0) {
                (new SetButtonTask(idToName, false)).execute();
                break;
            }

            final String id = userFollowers.get(i);

            idToName.put(id, Project_18.allUsers.get(id));
            numToLoad--;
        }
    }

    private class SetButtonTask extends AsyncTask<Void, Void, Void> {

        private ConcurrentHashMap<String, Bitmap> idToBitmap =
                new ConcurrentHashMap<String, Bitmap>();

        private HashMap<String, String> userList = new HashMap<String, String>();
        private boolean forTrails;

        public SetButtonTask(HashMap<String, String> userList,
                             boolean forTrails) {
            this.userList = userList;
            this.forTrails = forTrails;
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
                        followersImageSize,
                        followersImageSize));
            }

            constructUsersLayout(idToBitmap, userList, forTrails);
            if (forTrails) {
                scrollViewTrails.ready();
            }
            else {
                scrollViewFollowers.ready();
            }
        }
    }

    private synchronized void constructUsersLayout (ConcurrentHashMap<String, Bitmap> idToBitmap,
                                                    HashMap<String, String> idToName,
                                                    boolean forTrails) {

        for (String id: idToBitmap.keySet()) {
            addToUsersLayout(idToBitmap.get(id), idToName.get(id), id, forTrails);
        }
    }

    // add button to the usersLayout
    private void addToUsersLayout(final Bitmap profPicBitmap, final String name,
                                  final String id, final boolean forTrails) {


        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

                // the button we'll be building
                final ImageButton b = new ImageButton(getActivity().getApplicationContext());

                b.setImageBitmap(profPicBitmap);

                // FOR TRAILS

                if (forTrails) {
                    // touch animation
                    b.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {

                            // set filter when pressed
                            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                                if (!nullButtons.contains(b)) {
                                    b.setColorFilter(new
                                            PorterDuffColorFilter(getResources().getColor(R.color.colorDividerLight),
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
                }

                // FOR FOLLOWERS
                else {
                    // touch animation
                    b.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {

                            // set filter when pressed
                            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                                b.setColorFilter(new
                                        PorterDuffColorFilter(getResources().getColor(R.color.colorDividerLight),
                                        PorterDuff.Mode.MULTIPLY));

                            }

                            // remove filter on release/cancel
                            if (event.getAction() == MotionEvent.ACTION_CANCEL) {
                                b.clearColorFilter();
                            }

                            // handle "click"
                            if (event.getAction() == MotionEvent.ACTION_UP) {

                                b.clearColorFilter();
                                ((MainAct) getActivity()).goToOtherProfile(id);
                            }

                            return true;
                        }
                    });
                }
                // contains button and name of the user
                LinearLayout buttonLayout = new LinearLayout(getActivity().getApplicationContext());

                // make button look good and add to buttonLayout
                makePretty(b, name, buttonLayout);

                if (forTrails) {
                    trailsLayout.addView(buttonLayout);
                }
                else {
                    followersLayout.addView(buttonLayout);
                }
            }
        });
    }

    private void makePretty(ImageButton b, String userName, LinearLayout buttonLayout) {
        //b.setBackground(getResources().getDrawable(R.drawable.button_pressed));
        b.setBackgroundColor(getResources().getColor(R.color.white));
        b.setPadding(15, 0, 15, 20);
        TextView tv = new TextView(getActivity().getApplicationContext());
        tv.setText(userName.split(" ")[0]);

        makePretty(tv);

        LinearLayout.LayoutParams lp =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);

        buttonLayout.setOrientation(LinearLayout.VERTICAL);

        buttonLayout.addView(b);
        //buttonLayout.addView(tv);
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
            Project_18.me.removeTrail(fb, user_ID);

            setFollowButton(false);
        }
        else {
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
            ((Button) v.findViewById(R.id.followButton)).
                    setTextColor(getResources().getColor(R.color.colorDivider));
            ((Button) v.findViewById(R.id.followButton)).setTypeface(null, Typeface.ITALIC);
        }

        else {
            Log.d("flw", "setting to follow");
            ((Button) v.findViewById(R.id.followButton)).setText("Follow");
            v.findViewById(R.id.followButton).
                    setBackgroundColor(getResources().getColor(R.color.colorAccent));
            ((Button) v.findViewById(R.id.followButton)).
                    setTextColor(getResources().getColor(R.color.colorTextPrimary));
            ((Button) v.findViewById(R.id.followButton)).setTypeface(null, Typeface.BOLD);
        }
    }

    public class EventButtonOnTouchListener implements View.OnTouchListener {

        private Event e;
        private LinearLayout container;

        public EventButtonOnTouchListener(Event e, LinearLayout container) {
            this.e = e;
            this.container = container;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {

            if (event.getAction() == MotionEvent.ACTION_UP) {
                goToEventInfo(e.getEvent_id());
                container.setBackground(getResources().getDrawable(R.drawable.border_event_button));
            }
            if (event.getAction() == MotionEvent.ACTION_CANCEL) {
                container.setBackground(getResources().getDrawable(R.drawable.border_event_button));
            }
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                container.setBackground(getResources().
                        getDrawable(R.drawable.border_event_button_pressed));
            }
            return true;
        }

        public void setE(Event e) {
            this.e = e;
        }
    }

}
