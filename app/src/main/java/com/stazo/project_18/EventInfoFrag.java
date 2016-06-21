package com.stazo.project_18;

/**
 * Created by ericzhang on 5/14/16.
 */
import android.app.Dialog;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.RequiresPermission;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
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

public class EventInfoFrag extends Fragment {

    private Firebase fb;
    private String passedEventID;
    private View v;

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.event_info, container, false);
        v.setVisibility(View.INVISIBLE);
        fb = ((Project_18) this.getActivity().getApplication()).getFB();

        // Get the event_id to display
        //String event_id = this.passedEventID;
        // Display event info
        System.out.println("EVENT ID: " + this.passedEventID);
        grabEventInfo(this.passedEventID);


        //setup comment buttons
        Button writeCommentButton = (Button) v.findViewById(R.id.writeCommentButton);
        writeCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                writeCommentClick();
            }
        });

        Button viewCommentButton = (Button) v.findViewById(R.id.viewCommentButton);
        viewCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewCommentClick();
            }
        });
        return v;
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
                        Event e = new Event(dataSnapshot.child("Events").
                                child(event_id).getValue(
                                new GenericTypeIndicator<HashMap<String, Object>>() {
                                }));

                        // get the info for the user
                        User u = new User((HashMap<String, Object>) dataSnapshot.child("Users").
                                child(e.getCreator_id()).getValue());

                        System.out.println(((Project_18) getActivity().getApplication()).getMe().getName());

                        // display event
                        showInfo(e, u);

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

        ImageView eventIcon = (ImageView) this.getActivity().findViewById(R.id.eventIcon);
        int findType = e.getType();
        Drawable d = getResources().getDrawable(R.drawable.type_icon_game);

        // determining the icon
        switch(findType) {
            case 1:
                d = getResources().getDrawable(R.drawable.ic1);
                break;
            case 2:
                d = getResources().getDrawable(R.drawable.ic2);
                break;
            case 3:
                d = getResources().getDrawable(R.drawable.ic3);
                break;
            case 4:
                d = getResources().getDrawable(R.drawable.ic4);
                break;
            case 5:
                d = getResources().getDrawable(R.drawable.ic5);
                break;
            case 6:
                d = getResources().getDrawable(R.drawable.ic6);
                break;
            case 7:
                d = getResources().getDrawable(R.drawable.ic7);
                break;
        }
        // setting the icon
        eventIcon.setImageDrawable(d);
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

//        if(minutes < 10){
//            eventTime.setText(hours + ":0" + minutes + " " + timePeriod);
//        } else {
//            eventTime.setText(hours + ":" + minutes + " " + timePeriod);
//        }
//        //A bit of math to find the time till event.
//        TextView eventTimeTo = (TextView) this.getActivity().findViewById(R.id.eventTimeTo);
//        if (currTime.get(Calendar.MINUTE) > minutes) {
//            minutes = minutes + 60;
//            hours--;
//        }
//        if((hours - currTime.get(Calendar.HOUR_OF_DAY) < 0) || ((minutes - currTime.get(Calendar.MINUTE)) < 0)){
//            eventTimeTo.setText("Started!");
//            long pastHour = currTime.get(Calendar.HOUR_OF_DAY) - hours - eventHour;
//            long pastMinute = currTime.get(Calendar.MINUTE) - minutes - eventMinute;
//            if(pastMinute < 0){
//                pastMinute += 60;
//                pastHour--;
//            }
//            if(pastHour > 1){
//                eventTimeTo.setText("COMPLETED");
//            } else {
//                if(pastHour == 1){
//                    eventTimeTo.setText("Finished 1 h ago");
//                } else {
//                    eventTimeTo.setText("Finished " + pastMinute + " m ago");
//                }
//            }
//        } else {
//            if (timePeriod.equalsIgnoreCase("PM")) {
//                if((hours - currTime.get(Calendar.HOUR_OF_DAY)) < 1){
//                    eventTimeTo.setTextColor(Color.RED);
//                }
//                eventTimeTo.setText("In: " + (hours - currTime.get(Calendar.HOUR_OF_DAY))
//                        + " h " + (minutes - currTime.get(Calendar.MINUTE)) + " m");
//            } else {
//                if((hours - currTime.get(Calendar.HOUR_OF_DAY)) < 1){
//                    eventTimeTo.setTextColor(Color.RED);
//                }
//                eventTimeTo.setText("In: " + (hours - currTime.HOUR_OF_DAY) + " h "
//                        + (minutes - currTime.MINUTE) + " m");
//            }
//        }
        v.setVisibility(View.VISIBLE);
    }

}