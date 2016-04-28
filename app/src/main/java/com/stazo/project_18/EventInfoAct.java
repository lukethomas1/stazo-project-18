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
import com.firebase.client.ValueEventListener;

import java.util.HashMap;

public class EventInfoAct extends AppCompatActivity {

    Firebase fb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_info);
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
                                ((Integer) event.get("time")).longValue());

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
        // update stuff
    }

}
