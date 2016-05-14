package com.stazo.project_18;

import android.app.ActionBar;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.GenericTypeIndicator;
import com.firebase.client.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;

public class ListAct extends android.support.v4.app.Fragment {

    private Firebase fb;
    ArrayList<Event> eventList = new ArrayList<Event>();
    private TextView loadingText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View v = inflater.inflate(R.layout.activity_list, container, false);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        fb = ((Project_18) this.getActivity().getApplication()).getFB();

        // Pull the events from firebase
        fb.child("Events").addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        // For every event in fb.child("Events"), create event and displayEvent
                        for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {

                            // get the info, storage?
                            Event e = new Event(eventSnapshot.getValue(
                                    new GenericTypeIndicator<HashMap<String, Object>>() {
                                    }));

                            // Add event to arraylist
                            eventList.add(e);

                        }

                        // Get the text in the activity
                        loadingText = (TextView)getActivity().findViewById(R.id.loadingText);

                        // Set loading text to "No events" if there were no events
                        if(eventList.isEmpty()) {
                            loadingText.setText("No Events");
                        }

                        // Otherwise there were events, and hide the TextView
                        else {
                            loadingText.setVisibility(View.GONE);
                        }

                        // Display all the events the were pulled from Firebase
                        displayEventList();

                        // remove this listener
                        fb.child("Events").removeEventListener(this);
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {
                    }
                });
        return v;
    }

    // Creates buttons for each event in arraylist and adds them to the activity
    private void displayEventList() {
        LinearLayout listLayout = (LinearLayout)getActivity().findViewById(R.id.nestedLL);

        LinearLayout.LayoutParams listParams = new LinearLayout.LayoutParams
                (ViewGroup.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT);

        for(Event evt : eventList) {
            Button evtButton = new Button(this.getActivity());
            evtButton.setText(evt.getName());

            evtButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    buttonPressed(v);
                }
            });

            listLayout.addView(evtButton, listParams);
        }
    }

    public void displayFilteredEventList(String search) {
        LinearLayout listLayout = (LinearLayout)getActivity().findViewById(R.id.nestedLL);

        // clear all the views
        listLayout.removeAllViews();

        LinearLayout.LayoutParams listParams = new LinearLayout.LayoutParams
                (ViewGroup.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT);

        filterEventList(search);

        for(Event evt : eventList) {
            Button evtButton = new Button(this.getActivity());
            evtButton.setText(evt.getName());

            evtButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    buttonPressed(v);
                }
            });

            listLayout.addView(evtButton, listParams);
        }
    }
    private void filterEventList(String search) {
            this.eventList = ((Project_18) getActivity().getApplication()).findRelevantEvents(search);
    }
    /*public void unfilterEvents() {
        // reset the pulledEvents
        this.eventList = ((Project_18) getActivity().getApplication()).getPulledEvents();

        // clean layout
        ((LinearLayout)getActivity().findViewById(R.id.nestedLL)).removeAllViews();

        // redisplay Event
        displayEventList();
    }*/

    public void buttonPressed(View view) {
        // Cast view to button
        Button button = (Button) view;
        String id = "default id for list";

        // Find which event it is
        for(Event evt : eventList) {
            if(button.getText() == evt.getName()) {
                // Get event id
                id = evt.getEvent_id();
            }
        }
        // Start EventInfo activity with event id attached
        ((MainAct)this.getActivity()).goToEventInfo(id);
    }
}
