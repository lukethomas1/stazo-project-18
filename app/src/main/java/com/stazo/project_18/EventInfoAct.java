package com.stazo.project_18;

import android.content.Intent;
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
import com.google.android.gms.maps.model.LatLng;

import java.util.Calendar;
import java.util.Date;
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

        /*Event tester = new Event("FBGM",
                "The goal of this event is to disregard women and acquire capital." +
                        "We will be offering free bro-tanks and snapbacks.",
                "Wiz Khalifa", 3, "42042000", "6969", "2034", "2054");*/
        //showInfo(tester);


        fb = ((Project_18) getApplication()).getFB();

        // Get the Intent that led to this Activity
        Intent callingIntent = getIntent();

        // Get the event_id to display
        String event_id = callingIntent.getStringExtra("event_id");

        // Display event info
        grabEventInfo(event_id);
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

                        System.out.println(((Project_18) getApplication()).getMe().getName());

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
        TextView eventDate = (TextView) findViewById(R.id.eventDate);
        TextView eventName = (TextView) findViewById(R.id.eventName);
        TextView eventDescription = (TextView) findViewById(R.id.eventDesc);
        TextView eventLength = (TextView) findViewById(R.id.eventLength);
        TextView eventCreator = (TextView) findViewById(R.id.eventCreator);
        TextView eventTime = (TextView) findViewById(R.id.eventClock);
        long startHour = 0;
        long startMinute = 0;
        //End Initialization

        ImageView eventIcon = (ImageView) findViewById(R.id.eventIcon);
        int findType = e.getType();
        Drawable d = getResources().getDrawable(R.drawable.gameicon);

        // determining the icon
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
        // setting the icon
        eventIcon.setImageDrawable(d);

        // setting the event info text fields
        eventName.setText(e.getName());
        eventDescription.setText(e.getDescription());
        int eventHour = e.getEndHour() - e.getStartHour();
        int eventMinute = e.getEndMinute() - e.getStartMinute();
        if(eventMinute < 0){
            eventHour--;
            eventMinute += 60;
        }

        eventDate.setText(e.getStartMonth() + "/" + e.getStartDay() + "/" + e.getStartYear() + " - " +
                e.getEndMonth() + "/" + e.getEndDay() + "/" + e.getEndYear());

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
        eventCreator.setText("Created by: " + u.getName());

        //Conversion to turn a long (ex. 2014) into (8:14 PM)
        long hours = e.getStartHour();
        long minutes = e.getStartMinute();
        String timePeriod = "AM";
        if(hours > 12){
            timePeriod = "PM";
            hours = hours - 12;
        }
        eventTime.setText(hours + ":" + minutes + " " + timePeriod);
        //A bit of math to find the time till event.
        Calendar currTime = Calendar.getInstance();
        TextView eventTimeTo = (TextView) findViewById(R.id.eventTimeTo);
        if (currTime.get(Calendar.MINUTE) > minutes) {
            minutes = minutes + 60;
            hours--;
        }
        if((hours - currTime.get(Calendar.HOUR) < 0) || ((minutes - currTime.get(Calendar.MINUTE)) < 0)){
            eventTimeTo.setText("Started!");
        } else {
            if (timePeriod.equalsIgnoreCase("PM")) {
                eventTimeTo.setText((hours - currTime.get(Calendar.HOUR)) + " h " + (minutes - currTime.get(Calendar.MINUTE)) + " m");
            } else {
                eventTimeTo.setText((hours - currTime.HOUR) + " h " + (minutes - currTime.MINUTE) + " m");
            }
        }
    }
    /*@Override
    public void onBackPressed(){

    }*/
}