package com.stazo.project_18;

import android.app.ActionBar;
import android.app.Fragment;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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

public class ListAct extends AppCompatActivity {

    private Firebase fb;
    ArrayList<Event> eventList = new ArrayList<Event>();
    private TextView loadingText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fb = ((Project_18) getApplication()).getFB();

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
                        loadingText = (TextView)findViewById(R.id.loadingText);

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
    }

    // Creates buttons for each event in arraylist and adds them to the activity
    private void displayEventList() {
        LinearLayout listLayout = (LinearLayout)findViewById(R.id.nestedLL);
        LinearLayout.LayoutParams listParams = new LinearLayout.LayoutParams
                (ViewGroup.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT);

        for(Event evt : eventList) {
            Button evtButton = new Button(this);
            evtButton.setText(evt.getName());
            listLayout.addView(evtButton, listParams);
        }
    }
}
