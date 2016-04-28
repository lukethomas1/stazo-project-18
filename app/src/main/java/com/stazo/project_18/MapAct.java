package com.stazo.project_18;

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
import com.firebase.client.MutableData;
import com.firebase.client.ValueEventListener;

import java.util.HashMap;

public class MapAct extends AppCompatActivity {

    Firebase fb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        fb = ((Project_18) getApplication()).getFB();

    }

    // Display all the events, should probably be called in onCreate
    private void displayAllEvents() {

        // Listener for pulling the events
        fb.child("Events").addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        // For every event in fb.child("Events"), create event and displayEvent
                        for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {

                            // get the info, storage?
                            HashMap<String, Object> event = eventSnapshot.getValue(
                                    new GenericTypeIndicator<HashMap<String, Object>>() {
                                    });
                            Event e = new Event(
                                    (String) event.get("name"),
                                    (String) event.get("description"),
                                    (String) event.get("creator_id"),
                                    ((Integer) event.get("type")).intValue(),
                                    ((Integer) event.get("time")).longValue());

                            // display event
                            displayEvent(e);
                        }

                        // remove this listener
                        fb.child("Events").removeEventListener(this);


                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {
                    }
                });
    }

    // Ansel and Matt TODO Should add marker for event
    private void displayEvent(Event e) {
        // Add marker for single event
    }

}
