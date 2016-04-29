package com.stazo.project_18;

import android.os.Bundle;
import android.os.SystemClock;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
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

public class EventInfoAct extends AppCompatActivity {

    Firebase fb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_info);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Event tester = new Event("Smash Tournament",
                "No Plebs Allowed.", "Dank Memes", 3, 2034, 2034);
        showInfo(tester);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        fb = ((Project_18) getApplication()).getFB();

        // Set textviews to have correct info
        grabEventInfo(getIntent().getStringExtra("event_id"));
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
        //Initializing local variables to display relevant information
        TextView eventName = (TextView) findViewById(R.id.eventName);
        eventName.setText(e.getName());
        TextView eventDescription = (TextView) findViewById(R.id.eventDesc);
        eventDescription.setText(e.getDescription());
        TextView eventCreator = (TextView) findViewById(R.id.eventCreator);
        eventCreator.setText("Created by: " + e.getCreator_id());
        TextView eventTime = (TextView) findViewById(R.id.eventClock);
        //Converstion to turn a long (ex. 2014) into (8:14 PM)
        long hours = e.getStartTime()/100;
        long minutes = (e.getStartTime() - (hours*100));
        String timePeriod = "AM";
        if(hours > 12){
            timePeriod = "PM";
            hours = hours - 12;
        }
        eventTime.setText(hours + ":" + minutes + " " + timePeriod);
        //A bit of math to find the time till event.
        Calendar currTime = Calendar.getInstance();
        TextView eventTimeTo = (TextView) findViewById(R.id.eventTimeTo);
        if(timePeriod.equalsIgnoreCase("PM")){
            eventTimeTo.setText(((hours + 12) - currTime.HOUR) + " h " + (minutes - currTime.MINUTE) + " m");
        } else {
            eventTimeTo.setText((hours - currTime.HOUR) + " h " + (minutes - currTime.MINUTE) + " m");
        }
    }

}