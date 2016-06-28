package com.stazo.project_18;

/**
 * Created by ericzhang on 5/14/16.
 */
import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresPermission;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.GenericTypeIndicator;
import com.firebase.client.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

public class EventInfoFrag extends Fragment implements GestureDetector.OnGestureListener {

    private Firebase fb;

    public String passedEventID;
    private User currUser;
    private Event currEvent;
    private View v;
    private View bottomSheet;
    private BottomSheetBehavior mBottomSheetBehavior;
    private User me;

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
        TextView eventTime = (TextView) this.getActivity().findViewById(R.id.eventTimeTo);
        long startHour = 0;
        long startMinute = 0;
        //End Initialization

        //ImageView eventIcon = (ImageView) this.getActivity().findViewById(R.id.eventIcon);
        int findType = e.getType();

        // setting the event info text fields
        eventName.setText(e.getName());
        eventDescription.setText(e.getDescription());
        eventCreator.setText("Created by: " + u.getName());

        // show time fields
        //Initialize time
        long startTime = e.getStartTime();
        long endTime = e.getEndTime();
        Date start = new Date(startTime);
        Date end = new Date(endTime);

        //Set start time

        SimpleDateFormat startDayFormat = new SimpleDateFormat("MM/dd", Locale.US);
        SimpleDateFormat startTimeFormat = new SimpleDateFormat("HH:mm", Locale.US);

        String startText = "Starting on " + startDayFormat.format(start) +
                " at " + startTimeFormat.format(start);
        eventDate.setText(startText);

        //Set event length
        long length = endTime - startTime;
        long eventHour = length/(1000 * 60 * 60);
        long eventMinute = length/(1000 * 60) - eventHour*60;

        if(eventHour > 0){
            if(eventHour == 1){
                eventLength.setText("Duration: " + eventHour + " hour and ");
            } else {
                eventLength.setText("Duration: " + eventHour + " hours and ");
            }
        }
        if(eventMinute == 1){
            eventLength.setText(eventLength.getText() + "" + eventMinute + " minute");
        }
        else {
            eventLength.setText(eventLength.getText() + "" + eventMinute + " minutes");
        }

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
        v.setVisibility(View.VISIBLE);
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
            b.setText("Join");

        } else {
            me.attendEvent(currEvent.getEvent_id(), fb);
            b.setBackgroundColor(getResources().getColor(R.color.colorDividerLight));
            b.setText("Joined");

        }
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