package com.stazo.project_18;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.GenericTypeIndicator;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class ListAct extends android.support.v4.app.Fragment {

    private static final long HAPPENING_NOW_WINDOW = 3600000; // Milliseconds in an hour

    private Firebase fb;
    ArrayList<Event> eventList = new ArrayList<Event>();
    private TextView loadingText;

    ExpandableListAdapter listAdapter;
    ExpandableListView expListView;
    ArrayList<String> headerList;
    HashMap<String, ArrayList<Event>> headerToEventListHM;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_list, container, false);

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

//                            Iterable<DataSnapshot> attendeesIterable = eventSnapshot.child("attendees").getChildren();
//                            while(attendeesIterable.iterator().hasNext()) {
//                                this.attendees.add(attendeesIterable.iterator().next());
//                            }
                            // Add event to arraylist
                            eventList.add(e);

                        }

                        // Get the text in the activity
                        loadingText = (TextView) getActivity().findViewById(R.id.loadingText);

                        // Set loading text to "No events" if there are no events
                        if (eventList.isEmpty()) {
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

                        // show activity
                        getActivity().findViewById(R.id.eventList).setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {
                    }
                });

        return v;
    }

    // Creates ListViews for each event in arraylist and adds them to the activity
    private void displayEventList() {
        //Grabs the ListView
        expListView = (ExpandableListView) getActivity().findViewById(R.id.eventList);
        headerList = new ArrayList<>();
        headerToEventListHM = new HashMap<>();

        // Add event categories
        headerList.add("Hot Events");
        headerList.add("Happening Now");
        headerList.add("All Events");

        // Add Hot Events
        ArrayList<Event> hotEventsList = new ArrayList<>();

        // Add all events beyond the popularity threshold to the "hot events" list
        for (Event event : eventList) {
            if (event.getPopularity() > Project_18.POP_THRESH2) {
                hotEventsList.add(event);
            }
        }

        headerToEventListHM.put(headerList.get(0), hotEventsList);

        // Find events happening soon
        User thisUser = ((Project_18) getActivity().getApplication()).getMe();

        ArrayList<Event> nowEventsList = new ArrayList<>();

        // Get current time in milliseconds
        long currentTime = System.currentTimeMillis();

        for (Event event : eventList) {
            // Happening now if within an hour from start and hasn't ended yet
            if (event.getStartTime() - currentTime < HAPPENING_NOW_WINDOW
                    && currentTime < event.getEndTime()) {
                nowEventsList.add(event);
            }
        }

        headerToEventListHM.put(headerList.get(1), nowEventsList);

        /* TODO: For local events, base it on a certain radius around the user's current location. */

        ArrayList<Event> allEventsList = new ArrayList<>();

        for (Event event : eventList) {
            allEventsList.add(event);
        }

        headerToEventListHM.put(headerList.get(2), allEventsList);

        /*
        for(int i = 0; i < eventList.size(); i++) {
            Event evt = eventList.get(i);

            listIds.add(evt.getEvent_id());
            headerList.add(evt.getName());

            ArrayList<Event> groupEventsList = new ArrayList<>();
            groupEventsList.add(evt);

            headerToEventListHM.put(headerList.get(i), groupEventsList);
        } */

        listAdapter = new ExpandableListAdapter(getActivity(), headerList, headerToEventListHM);

        // Display event info on child click
        expListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
                                        int childPosition, long id) {
                String eventId = listAdapter.getEventId(groupPosition, childPosition);

                ((MainAct)getActivity()).goToEventInfo(eventId);

                return true;
            }
        });

        // Collapse open list on new expansion
        expListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            int prevGroupPos = -1;

            @Override
            public void onGroupExpand(int groupPosition) {
                // Collapse open list on new expansion
                if (prevGroupPos >= 0 && prevGroupPos != groupPosition) {
                    expListView.collapseGroup(prevGroupPos);
                }

                prevGroupPos = groupPosition;
            }
        });

        expListView.setAdapter(listAdapter);

        // Display hot events by default
        expListView.expandGroup(0);
    }

    /* TODO Filter lists inside of the three main categories (hot, subscribed, and local) */

    public void displayFilteredEventList() {

        headerList = new ArrayList<>();
        headerToEventListHM = new HashMap<>();

        filterEventList();

        for(int i = 0; i < eventList.size(); i++) {
            Event evt = eventList.get(i);

            headerList.add(evt.getName());

            ArrayList<Event> groupEventsList = new ArrayList<>();
            groupEventsList.add(evt);

            headerToEventListHM.put(headerList.get(i), groupEventsList);
        }

        listAdapter = new ExpandableListAdapter(getActivity(), headerList, headerToEventListHM);

        expListView.setAdapter(listAdapter);
    }

    private void filterEventList() {
        // true = don't worry about time
        this.eventList = ((Project_18) getActivity().getApplication()).findRelevantEvents(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        Firebase.setAndroidContext(getContext());
    }

}
