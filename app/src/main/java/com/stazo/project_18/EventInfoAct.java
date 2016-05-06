package com.stazo.project_18;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextClock;
import android.widget.TextView;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.GenericTypeIndicator;
import com.firebase.client.ValueEventListener;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class EventInfoAct extends AppCompatActivity {

    Firebase fb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_info);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Event tester = new Event("FBGM",
                "The goal of this event is to disregard women and acquire capital." +
                        "We will be offering free bro-tanks and snapbacks.",
                "Wiz Khalifa", 3, 42042000, 2034, 2054);
        //showInfo(tester);


        fb = ((Project_18) getApplication()).getFB();

        // Set textviews to have correct info
        //commented out for testing
        //grabEventInfo(getIntent().getStringExtra("event_id"));

        grabEventInfo("yoo");
    }

    // Pulls event info and delegates to showInfo to display the correct info
    private void grabEventInfo(final String event_id) {
        fb.child("Events").child(event_id).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        // get the info, storage?
                        HashMap<String, Object> event = dataSnapshot.getValue(
                                new GenericTypeIndicator<HashMap<String, Object>>() {
                                });
                        Event e = new Event(
                                (String) event.get("name"),
                                (String) event.get("description"),
                                (String) event.get("creator_id"),
                                ((Integer) event.get("type")).intValue(),
                                ((Integer) event.get("date")).longValue(),
                                ((Integer) event.get("startTime")).longValue(),
                                ((Integer) event.get("endTime")).longValue());

                        // display event
                        showInfo(e);

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
    private void showInfo(Event e) {
<<<<<<< HEAD
        //Initialize Local Variables
        ImageView eventIcon = (ImageView) findViewById(R.id.eventIcon);
        int findType = e.getType();
        Drawable d = getResources().getDrawable(R.drawable.gameicon);
        TextView eventDate = (TextView) findViewById(R.id.eventDate);
        TextView eventName = (TextView) findViewById(R.id.eventName);
        TextView eventDescription = (TextView) findViewById(R.id.eventDesc);
        TextView eventLength = (TextView) findViewById(R.id.eventLength);
        TextView eventCreator = (TextView) findViewById(R.id.eventCreator);
        TextView eventTime = (TextView) findViewById(R.id.eventClock);
        long startHour = 0;
        long endHour = 0;
        long startMinute = 0;
        long endMinute = 0;
        long eventDay = 0;
        long eventMonth = 0;
        long eventYear = 0;
        long eventMinute = 0;
        long eventHour = 0;
        //End Initialization
=======

        ImageView eventIcon = (ImageView) findViewById(R.id.eventIcon);
        int findType = e.getType();
        Drawable d = getResources().getDrawable(R.drawable.gameicon);

        // determining the icon
>>>>>>> f7f7c2d3ee717f0e0f1fed9f58b6bbdd7bcfb3ec
        switch(findType) {
            case 1:
                d = getResources().getDrawable(R.drawable.sportsicon);
                break;
            case 2:
                d = getResources().getDrawable(R.drawable.foodicon);
                break;
            case 3:
                d = getResources().getDrawable(R.drawable.dollaricon);
                break;
            case 4:
                break;
            case 5:
                break;
            case 6:
                break;
        }

<<<<<<< HEAD

        startHour = e.getStartTime()/100;
        startMinute = (e.getStartTime() - (startHour*100));
        endHour = e.getEndTime()/100;
        endMinute = (e.getEndTime() - (endHour*100));

        eventHour = endHour - startHour;
        eventMinute = endMinute - startMinute;
        if(eventMinute < 0){
            eventHour--;
            eventMinute = eventMinute + 60;
        }

        eventMonth = e.getDate()/1000000;
        eventDay = e.getDate()/10000 - (eventMonth * 100);
        eventYear = e.getDate() - ((eventMonth * 1000000)  + (eventDay * 10000));

        eventDate.setText(eventMonth + "/" + eventDay + "/" + eventYear);

        eventIcon.setImageDrawable(d);
=======
        // setting the icon
        eventIcon.setImageDrawable(d);

        // setting the event info text fields
        TextView eventName = (TextView) findViewById(R.id.eventName);
>>>>>>> f7f7c2d3ee717f0e0f1fed9f58b6bbdd7bcfb3ec
        eventName.setText(e.getName());
        eventDescription.setText(e.getDescription());
        if(eventHour > 0){
            if(eventHour == 1){
                eventLength.setText(eventHour + " hour and ");
            } else {
                eventLength.setText(eventHour + " hours and ");
            }
        }
        if(eventMinute == 1){
            eventLength.setText(eventLength.getText() + "" + eventMinute + " minute");
        } else {
            eventLength.setText(eventLength.getText() + "" + eventMinute + " minutes");
        }
        eventCreator.setText("Created by: " + e.getCreator_id());
<<<<<<< HEAD

        //Converstion to turn a long (ex. 2014) into (8:14 PM)

=======
        TextView eventTime = (TextView) findViewById(R.id.eventClock);

        //Conversion to turn a long (ex. 2014) into (8:14 PM)
        long hours = e.getStartTime()/100;
        long minutes = (e.getStartTime() - (hours*100));
>>>>>>> f7f7c2d3ee717f0e0f1fed9f58b6bbdd7bcfb3ec
        String timePeriod = "AM";
        if(startHour > 12){
            timePeriod = "PM";
            startHour = startHour - 12;
        }
<<<<<<< HEAD
        eventTime.setText(startHour + ":" + startMinute + " " + timePeriod);
=======
        eventTime.setText(hours + ":" + minutes + " " + timePeriod);

>>>>>>> f7f7c2d3ee717f0e0f1fed9f58b6bbdd7bcfb3ec
        //A bit of math to find the time till event.
        Calendar currTime = Calendar.getInstance();
        currTime.getTime();
        TextView eventTimeTo = (TextView) findViewById(R.id.eventTimeTo);
        if(timePeriod.equalsIgnoreCase("PM")){
            eventTimeTo.setText(((startHour + 12) - currTime.HOUR) + " h " + (startMinute - currTime.MINUTE) + " m");
        } else {
            eventTimeTo.setText((startHour - currTime.HOUR) + " h " + (startMinute - currTime.MINUTE) + " m");
        }
        System.out.println(currTime.HOUR + ":" + currTime.MINUTE);
    }
    @Override
    public void onBackPressed(){

    }
}