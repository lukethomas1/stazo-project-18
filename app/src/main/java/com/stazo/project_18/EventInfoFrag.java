package com.stazo.project_18;

/**
 * Created by ericzhang on 5/14/16.
 */
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresPermission;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.GenericTypeIndicator;
import com.firebase.client.ValueEventListener;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

public class EventInfoFrag extends Fragment implements GestureDetector.OnGestureListener {

    private Firebase fb;

    public String passedEventID;
    private User currUser;
    public static Event currEvent;
    private View v;
    private View bottomSheet;
    private BottomSheetBehavior mBottomSheetBehavior;
    private User me;
    private Bitmap picBitmap;

    // Joined scrollview stuff
    private int SECTION_SIZE = 5;
    private int page = 0;
    private InteractiveScrollViewHorizontal scrollView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.event_info, container, false);
        v.setVisibility(View.INVISIBLE);
        fb = ((Project_18) this.getActivity().getApplication()).getFB();
        me = ((Project_18) this.getActivity().getApplication()).getMe();
        // Get the event_id to display
        //String event_id = this.passedEventID;
        // Display event info
        System.out.println("EVENT ID: " + this.passedEventID);
        grabEventInfo(this.passedEventID);

        bottomSheet = v.findViewById(R.id.bottom_sheet);
        mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        //mBottomSheetBehavior.setPeekHeight(680);
        //mBottomSheetBehavior.setPeekHeight(610);
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        mBottomSheetBehavior.setHideable(true);
        mBottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    mBottomSheetBehavior.setPeekHeight(0);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                // React to dragging events
            }
        });

        //mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

        //setup comment buttons
//        Button writeCommentButton = (Button) v.findViewById(R.id.writeCommentButton);
//        writeCommentButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                writeCommentClick();
//            }
//        });

        Button viewCommentButton = (Button) v.findViewById(R.id.viewCommentButton);
        viewCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewCommentClick();
            }
        });

        final Button attendButton = (Button) v.findViewById(R.id.attend);

        // if the user is already attending an event, change the button text to "Joined"
        if (me.getAttendingEvents().contains(passedEventID)) {

            attendButton.setBackgroundColor(getResources().getColor(R.color.colorDividerLight));
            attendButton.setTextColor(getResources().getColor(R.color.colorDivider));
            attendButton.setTypeface(null, Typeface.ITALIC);
            attendButton.setText("Joined");
        }

        // listener for attendButton
        attendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attendClick(attendButton);
            }
        });

        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        /*((ImageView) getActivity().findViewById(R.id.upArrow)).setImageBitmap(
                Bitmap.createScaledBitmap((BitmapFactory.decodeResource(getResources(),
                                R.drawable.up_arrow_big)),
                        30,
                        30,
                        true));*/
        // set scrollView
        scrollView = (InteractiveScrollViewHorizontal) getActivity().findViewById(R.id.joinedScrollView);

        scrollView.setOnBottomReachedListener(
                new InteractiveScrollViewHorizontal.OnBottomReachedListener() {
                    @Override
                    public void onBottomReached() {
                        // do something
                        loadMore();
                    }
                }
        );
    }

    public void collapse() {
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    // GATES AND JUSTIN IGNORE CODE BETWEEN HERE...

    private synchronized void loadMore() {
        page++;
        generateJoined();
    }

    private void generateJoined() {
        //(new UserNamesTask(fb)).execute();
        final HashMap<String, String> idToName = new HashMap<String, String>();
        int startingPoint = page * SECTION_SIZE;
        if (startingPoint >= currEvent.getAttendees().size()) {
            return;
        }

        int numToLoad = Math.min(SECTION_SIZE, currEvent.getAttendees().size() - startingPoint);

        for (int i = page * SECTION_SIZE; ; i++) {

            if (numToLoad == 0) {
                Log.d("sizzle", "currEvent.getAttendees() is " + currEvent.getAttendees().toString());
                (new SetButtonTask(idToName)).execute();
                break;
            }

            final String id = currEvent.getAttendees().get(i);

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
                        140,
                        140));
            }

            constructUsersLayout(idToBitmap, userList);
            scrollView.ready();
        }
    }

    private synchronized void constructUsersLayout (ConcurrentHashMap<String, Bitmap> idToBitmap,
                                                    HashMap<String, String> idToName) {

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
                            b.setColorFilter(new
                                    PorterDuffColorFilter(getResources().getColor(R.color.colorDividerLight),
                                    PorterDuff.Mode.MULTIPLY));
                        }

                        // handle "click"
                        if (event.getAction() == MotionEvent.ACTION_UP) {
                            Log.d("myTag", "imageButton pressed");
                            if (id.equals(Project_18.me.getID())) {
                                MainAct.viewPager.setCurrentItem(MainAct.PROF_POS);
                            }
                            else {
                                ((MainAct) getActivity()).goToOtherProfile(id);
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
                LinearLayout buttonLayout = new LinearLayout(getActivity().getApplicationContext());

                // make button look good and add to buttonLayout
                makePretty(b, name, buttonLayout);

                ((LinearLayout) getActivity().findViewById(R.id.joinedLayout)).addView(buttonLayout);
            }
        });
    }

    private void makePretty(ImageButton b, String userName, LinearLayout buttonLayout) {
        //b.setBackground(getResources().getDrawable(R.drawable.button_pressed));

        b.setBackgroundColor(getResources().getColor(R.color.white));

        LinearLayout.LayoutParams bLP = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        bLP.gravity = Gravity.CENTER_HORIZONTAL;
        b.setPadding(0,0,0,0);
        b.setLayoutParams(bLP);

        TextView tv = new TextView(getActivity().getApplicationContext());
        tv.setText(userName.split(" ")[0]);

        makePretty(tv);

        LinearLayout.LayoutParams tLP =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
        tLP.gravity = Gravity.CENTER_HORIZONTAL;
        tv.setLayoutParams(tLP);

        buttonLayout.setOrientation(LinearLayout.VERTICAL);
        buttonLayout.setGravity(Gravity.CENTER_HORIZONTAL);
        buttonLayout.setPadding(0,0,20,0);

        buttonLayout.addView(b);
        buttonLayout.addView(tv);
    }

    private void makePretty(TextView tv) {
        tv.setTextColor(getResources().getColor(R.color.colorTextPrimary));
        tv.setGravity(Gravity.CENTER_HORIZONTAL);
    }

    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    // ... AND HERE

    public void hideEventInfo() {
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    public void toggleState() {

        if (mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
        else {
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            //getActivity().findViewById(R.id.upArrow).setRotation(0);
        }
    }

    /* toggle frag state */
    public void toggleState(View v) {
        toggleState();
    }

    //setter method for main act to pass in eventID
    public void setEventID(String passedEventID) {
        this.passedEventID = passedEventID;
    }

    // Pulls event info and delegates to showInfo to display the correct info
    private void grabEventInfo(final String event_id) {
        fb.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        // get the info for the event
                        currEvent = new Event(dataSnapshot.child("Events").
                                child(event_id).getValue(
                                new GenericTypeIndicator<HashMap<String, Object>>() {
                                }));

                        // get the info for the user
                        currUser = new User((HashMap<String, Object>) dataSnapshot.child("Users").
                                child(currEvent.getCreator_id()).getValue());

                        System.out.println(((Project_18) getActivity().getApplication()).getMe().getName());

                        // display event
                        showInfo(currEvent, currUser);

                        mBottomSheetBehavior.setPeekHeight(
                                //getActivity().findViewById(R.id.arrowButtonLayout).getHeight() +
                                getActivity().findViewById(R.id.measurement).getHeight());

                        // set text for number Joined
                        ((TextView) getActivity().findViewById(R.id.numJoinedText)).
                                setText("Joined (" + currEvent.getAttendees().size() + ")");

                        // generateJoined scrollview
                        generateJoined();

                        // remove this listener
                        fb.child("Events").child(event_id).removeEventListener(this);
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {
                    }
                });
    }

    // Called from grabEventInfo, programatically updates the textviews to display the correct info
    // Justin TODO Update the textviews in the layout to show the correct info
    private void showInfo(Event e, User u) {

        //Initialize Local Variables
        TextView eventDate = (TextView) this.getActivity().findViewById(R.id.eventDate);
        TextView eventName = (TextView) this.getActivity().findViewById(R.id.eventName);
        TextView eventDescription = (TextView) this.getActivity().findViewById(R.id.eventDesc);
        TextView eventLength = (TextView) this.getActivity().findViewById(R.id.eventLength);
        TextView eventCreator = (TextView) this.getActivity().findViewById(R.id.eventCreator);
        TextView eventStart = (TextView) this.getActivity().findViewById(R.id.starts);
        TextView eventTimes = (TextView) this.getActivity().findViewById(R.id.times);
        TextView eventDesc = (TextView) this.getActivity().findViewById(R.id.desc);
        //TextView eventTime = (TextView) this.getActivity().findViewById(R.id.eventTimeTo);
        ImageView eventCreatorPic = (ImageView) this.getActivity().findViewById(R.id.creatorPic);
        long startHour = 0;
        long startMinute = 0;
        //End Initialization

        //ImageView eventIcon = (ImageView) this.getActivity().findViewById(R.id.eventIcon);
        int findType = e.getType();

        // setting the event info text fields
        eventName.setText(e.getName());
        eventDescription.setVisibility(View.VISIBLE);
        eventDesc.setVisibility(View.VISIBLE);
        if(e.getDescription().length() == 0){
            eventDescription.setVisibility(View.GONE);
            eventDesc.setVisibility(View.GONE);
        } else {
            eventDescription.setText(e.getDescription());
        }
        eventCreator.setText(u.getName());

        // setting the event creator image
        getFBPhoto(u.getID());

        // show time fields
        //Initialize time
        long startTime = e.getStartTime();
        long endTime = e.getEndTime();
        Date start = new Date(startTime);
        Date end = new Date(endTime);

        Date now = new Date();

        //Set start time
        String startText = buildStartDay(start) + " at " + buildStartTime(start);
        eventDate.setText(startText);

        if(now.getTime() > start.getTime()){
            eventStart.setText("Started");
            long hourdiff = (start.getHours() - now.getHours() + 1);
            long mindiff = start.getMinutes() - now.getMinutes();
            if(mindiff < 0){
                hourdiff--;
                mindiff += 60;
            }
            eventTimes.setText("Ends in: " + hourdiff + " h " + mindiff + "m");
        }

        //Set event length
        String durationText = buildDurationTime(startTime, endTime);
        eventLength.setText(durationText);

        /*
        //Set how long until start time or if started/completed yet
        Calendar curr = Calendar.getInstance();
        long currTime = curr.getTimeInMillis();
        long timeTill = startTime - currTime;
        long timeTillHour = timeTill/(1000 * 60 * 60);
        long timeTillMinute = timeTill/(1000 * 60) - timeTillHour*60;

        System.out.println("Time till: " + timeTill);
        if (timeTill <= 0) {
            eventTime.setText("STARTED!");
            long timeAfterStart = endTime - currTime;
            long timeAfterHour = timeAfterStart/(1000 * 60 * 60);
            long timeAfterMinute = timeAfterStart/(1000 * 60) - timeAfterHour*60;
            if (timeAfterMinute > 60) {
                eventTime.setText("Completed");
            }
            if (timeAfterMinute < 0) {
                eventTime.setText("Finished " + timeAfterMinute + " minutes ago");
            }
        }
        else {
            eventTime.setText(timeTillHour + " hours and " + timeTillMinute + " minutes left until start of event");
        }
        */

        v.setVisibility(View.VISIBLE);
    }

    public void getFBPhoto(final String id) {

        new Thread(new Runnable() {
            public void run() {
                try {
                    URL imageURL = new URL("https://graph.facebook.com/" +
                            id + "/picture?width=" + Project_18.pictureSize);
                    Log.d("EventInfoFrag", "https://graph.facebook.com/" +
                            id + "/picture?width=" + Project_18.pictureSize);
                    picBitmap = BitmapFactory.decodeStream(imageURL.openConnection().getInputStream());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                final ImageView iv = (ImageView) v.findViewById(R.id.creatorPic);

                iv.post(new Runnable() {
                    public void run() {

                        // cache image
                        ((Project_18) getActivity().getApplication()).
                                addBitmapToMemoryCache(id, Bitmap.createBitmap(picBitmap));

                        //picBitmap = Project_18.BITMAP_RESIZER(picBitmap, 250, 250);
                        iv.setImageBitmap(picBitmap);
                        iv.setVisibility(View.VISIBLE);

                        // unhide-layout
                        (v.findViewById(R.id.creatorPic)).setVisibility(View.VISIBLE);
                    }

                });

                // handle touch/click
                iv.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        // set filter when pressed
                        if (event.getAction() == MotionEvent.ACTION_DOWN) {
                            iv.setColorFilter(new
                                    PorterDuffColorFilter(getResources().getColor(R.color.colorDividerLight),
                                    PorterDuff.Mode.MULTIPLY));
                        }

                        // handle "click"
                        if (event.getAction() == MotionEvent.ACTION_UP) {
                            Log.d("myTag", "imageButton pressed");
                            if (id.equals(Project_18.me.getID())) {
                                MainAct.viewPager.setCurrentItem(MainAct.PROF_POS);
                            } else {
                                ((MainAct) getActivity()).goToOtherProfile(id);
                            }
                        }

                        // remove filter on release/cancel
                        if (event.getAction() == MotionEvent.ACTION_UP ||
                                event.getAction() == MotionEvent.ACTION_CANCEL) {
                            iv.clearColorFilter();
                        }
                        return true;
                    }
                });

            }
        }).start();
    }

    public void writeCommentClick() {
        //open comment write window
        WriteCommentFrag writeFrag = new WriteCommentFrag();
        writeFrag.setEventID(this.passedEventID);
        FragmentTransaction trans = this.getActivity().getSupportFragmentManager().beginTransaction();
        trans.add(R.id.show_writeComment, writeFrag).addToBackStack("WriteCommentFrag").commit();
    }

//    public void pushComment(Comment comment) {
//        fb = ((Project_18) this.getActivity().getApplication()).getFB();
//        String event_ID = comment.getEvent_ID();
//        fb.child("CommentDatabase").child(event_ID).setValue(comment);
//    }

    public String buildStartDay(Date start) {
        String finalString;
        String dayString;
        // get today's day in MM/dd format
        Calendar c = Calendar.getInstance();
        SimpleDateFormat today = new SimpleDateFormat("MM/dd", Locale.US);

        // convert start time to MM/dd format
        SimpleDateFormat startDay = new SimpleDateFormat("MM/dd", Locale.US);
        dayString = startDay.format(start);

        // compare today's date
        if (dayString.equals(today.format(c.getTime()))) {
            finalString = "Today";
        }
        else {
            startDay = new SimpleDateFormat("MMM dd", Locale.US);
            finalString = startDay.format(start);
        }
        return finalString;
    }

    public String buildStartTime(Date start) {
        SimpleDateFormat startTime = new SimpleDateFormat("h:mm a", Locale.US);
        return startTime.format(start);
    }

    public String buildDurationTime(long startTime, long endTime) {
        String finalString = "";
        long length = endTime - startTime;
        long eventHour = length/(1000 * 60 * 60);
        long eventMin = length/(1000 * 60) - eventHour*60;

        if (eventHour > 0) {
            finalString = finalString + eventHour;
            if (eventHour == 1) {
                finalString = finalString + " hr";
            }
            else {
                finalString = finalString + " hrs";
            }
            Log.d("EventInfoFrag", "eventMin: " + eventMin);
            if (eventMin > 0) {
                finalString = finalString + " and " + eventMin;
            }
        }
        if (eventMin > 0) {
            if (eventMin == 1) {
                finalString = finalString + " min";
            }
            else {
                finalString = finalString + " mins";
            }
        }

        return finalString;
    }


    public void viewCommentClick() {
        //open comment view window
        ViewCommentFrag viewFrag = new ViewCommentFrag();
        viewFrag.setEventID(this.passedEventID);
        FragmentTransaction trans = this.getActivity().getSupportFragmentManager().beginTransaction();
        trans.add(R.id.show_writeComment, viewFrag).addToBackStack("ViewCommentFrag").commit();
    }

    public void attendClick(Button b) {
        if(b.getText() == "Joined"){
            // get the info for the user
            me.unattendEvent(currEvent.getEvent_id(), fb);

            b.setBackgroundColor(getResources().getColor(R.color.colorAccent));
            b.setTextColor(getResources().getColor(R.color.colorTextPrimary));
            b.setTypeface(null, Typeface.BOLD);
            b.setText("Join");

        } else {
            me.attendEvent(currEvent.getEvent_id(), fb);
            b.setBackgroundColor(getResources().getColor(R.color.colorDividerLight));
            b.setTextColor(getResources().getColor(R.color.colorDivider));
            b.setTypeface(null, Typeface.ITALIC);
            b.setText("Joined");

        }
    }

    // pull and set event creator picture
    private void setCreatorPicture() {

    }

    public String getPassedEventID() {
        return passedEventID;
    }


    @Override
    public void onResume() {
        super.onResume();
        Firebase.setAndroidContext(getContext());
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }
}