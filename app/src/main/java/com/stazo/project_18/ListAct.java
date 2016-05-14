package com.stazo.project_18;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.GenericTypeIndicator;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ListAct extends android.support.v4.app.Fragment {

    private Firebase fb;
    ArrayList<Event> eventList = new ArrayList<Event>();
    private TextView loadingText;

    ExpandableListAdapter listAdapter;
    ExpandableListView expListView;
    List<String> listDataHeader;
    HashMap<String, List<String>> listDataChild;
    List<String> listIds;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
                    public void onCancelled(FirebaseError firebaseError) { }
                });
        return v;
    }

    // Creates ListViews for each event in arraylist and adds them to the activity
    private void displayEventList() {
        //Grabs the ListView
        expListView = (ExpandableListView)getActivity().findViewById(R.id.eventList);
        listDataHeader = new ArrayList<>();
        listDataChild = new HashMap<>();
        listIds = new ArrayList<>();

        for(int i = 0; i < eventList.size(); i++) {
            Event evt = eventList.get(i);

            listIds.add(evt.getEvent_id());
            listDataHeader.add(evt.getName());

            List<String> evtDesc = new ArrayList<>();
            evtDesc.add(evt.getDescription());

            listDataChild.put(listDataHeader.get(i), evtDesc);
        }

        listAdapter = new ExpandableListAdapter(getActivity(), listDataHeader, listDataChild,
                listIds);

        expListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
                                        int childPosition, long id) {
                String eventId = listAdapter.getEventId(groupPosition);

                ((MainAct)getActivity()).goToEventInfo(eventId);

                return true;
            }
        });

        expListView.setAdapter(listAdapter);
    }


    public void displayFilteredEventList(String search) {

        listDataHeader = new ArrayList<>();
        listDataChild = new HashMap<>();
        listIds = new ArrayList<>();

        filterEventList(search);

        for(int i = 0; i < eventList.size(); i++) {
            Event evt = eventList.get(i);

            listIds.add(evt.getEvent_id());
            listDataHeader.add(evt.getName());

            List<String> evtDesc = new ArrayList<>();
            evtDesc.add(evt.getDescription());

            listDataChild.put(listDataHeader.get(i), evtDesc);
        }

        listAdapter = new ExpandableListAdapter(getActivity(), listDataHeader, listDataChild,
                listIds);

        expListView.setAdapter(listAdapter);
    }
    private void filterEventList(String search) {
        this.eventList = ((Project_18) getActivity().getApplication()).findRelevantEvents(search);
    }
}
