package com.stazo.project_18;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.GenericTypeIndicator;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.PropertyPermission;

public class ListAct extends android.support.v4.app.Fragment {

    private static final int NUM_HOT = 2;
    private static final int NUM_HAPPENING_NOW = 4;
    private static final int NUM_LATER = 4;
    private static final long HAPPENING_NOW_WINDOW = (60 * 60 * 1000); // Milliseconds in an hour

    private Firebase fb;
    private ArrayList<Event> eventList = new ArrayList<Event>();
    private ArrayList<Event> litEventsList = new ArrayList<>();
    private ArrayList<Event> happeningNowList = new ArrayList<>();
    private ArrayList<Event> laterList = new ArrayList<>();


    private TextView loadingText;

    //ExpandableListAdapter listAdapter;
    //ExpandableListView expListView;
    private LinearLayout litLayout, happeningNowLayout, laterLayout;

    private int pageHappeningNow = 0;
    private int pageLater = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.list_act_concept, container, false);

        fb = ((Project_18) this.getActivity().getApplication()).getFB();

        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // hide activity
        getActivity().findViewById(R.id.listActLayout).setVisibility(View.INVISIBLE);

        // listeners for show more
        getActivity().findViewById(R.id.showMoreHappeningNowButton).
                setOnTouchListener(new ShowMoreOnTouchListener("showMoreHappeningNow",
                        (Button) getActivity().findViewById(R.id.showMoreHappeningNowButton)));

        getActivity().findViewById(R.id.showMoreLaterButton).
                setOnTouchListener(new ShowMoreOnTouchListener("showMoreLater",
                        (Button) getActivity().findViewById(R.id.showMoreLaterButton)));

        // set layouts
        litLayout = (LinearLayout) getActivity().findViewById(R.id.litLayout);
        happeningNowLayout = (LinearLayout) getActivity().findViewById(R.id.happeningNowLayout);
        laterLayout = (LinearLayout) getActivity().findViewById(R.id.laterLayout);

        pageHappeningNow = 0;
        pageLater = 0;

        eventList.clear();
        litEventsList.clear();
        happeningNowList.clear();
        laterList.clear();

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

                            eventList.add(e);
                        }

                        // Display all the events the were pulled from Firebase
                        // TODO do not call if list already populated
                        categorizeEvents();
                        displayAllEvents();

                        // remove this listener
                        fb.child("Events").removeEventListener(this);

                        // show activity
                        getActivity().findViewById(R.id.listActLayout).setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {
                    }
                });
    }

    // Put all events into categories
    private void categorizeEvents() {

        // sort events by popularity
        Collections.sort(eventList, new popularityCompare());


        // POPULAR EVENTS SECTION
        for (int i = 0; i < NUM_HOT; i++) {
            Event event = eventList.get(i);
            litEventsList.add(event);
        }
        // Remove hot events from eventList
        for (Event event : litEventsList) {
            eventList.remove(event);
        }

        // HAPPENING TODAY EVENTS SECTION
        for (Event event : eventList) {
            if ((new Date(event.getStartTime())).getDay() == (new Date()).getDay()) {
                happeningNowList.add(event);
            }
        }
        // Remove current events from eventList
        for (Event event : happeningNowList) {
            eventList.remove(event);
        }

        // LATER THIS WEEK EVENTS SECTION
        // The remaining are happening later this week
        laterList = new ArrayList<>(eventList);

        adjustShowMoreButtons();
    }

    public void adjustShowMoreButtons() {

        if (happeningNowList.size() > (pageHappeningNow + 1) * NUM_HAPPENING_NOW) {
            getActivity().findViewById(R.id.showMoreHappeningNowButton).setVisibility(View.VISIBLE);
        } else {
            getActivity().findViewById(R.id.showMoreHappeningNowButton).setVisibility(View.GONE);
        }

        if (laterList.size() > (pageLater + 1) * NUM_LATER) {
            getActivity().findViewById(R.id.showMoreLaterButton).setVisibility(View.VISIBLE);
        } else {
            getActivity().findViewById(R.id.showMoreLaterButton).setVisibility(View.GONE);
        }
    }

    public void showMoreHappeningNow() {
        if ((pageHappeningNow + 1) * NUM_HAPPENING_NOW < happeningNowList.size()) {
            pageHappeningNow++;
            displayHappeningNowEvents();
            adjustShowMoreButtons();
        }
    }

    public void showMoreLater() {
        if ((pageLater + 1) * NUM_LATER < laterList.size()) {
            pageLater++;
            displayLaterEvents();
            adjustShowMoreButtons();
        }

    }

    public void displayAllEvents() {
        displayLitEvents();
        displayHappeningNowEvents();
        displayLaterEvents();
    }

    public void displayLitEvents() {

        // empty case
        if (litEventsList.isEmpty()) {
            getActivity().findViewById(R.id.emptyLitTextContainer).setVisibility(View.VISIBLE);
        }
        // non empty case
        else {
            // Remove hot events from eventList
            for (Event event : litEventsList) {

                LinearLayout container = new LinearLayout(getActivity());
                EventButtonOnTouchListener listener = new EventButtonOnTouchListener(event, container);

                ((Project_18) getActivity().getApplication()).makeEventButton
                        (getActivity(), event, container, listener, true);

                litLayout.addView(container);
            }
        }
    }

    public void displayHappeningNowEvents() {

        int startPoint = pageHappeningNow * NUM_HAPPENING_NOW;
        int numLoaded = 0;

        // empty case
        if (happeningNowList.isEmpty()) {
            getActivity().findViewById(R.id.emptyHappeningNowTextContainer).setVisibility(View.VISIBLE);
        }
        // non empty case
        else {

            // Display events
            for (int i = startPoint; i < happeningNowList.size(); i++) {

                if (numLoaded == NUM_HAPPENING_NOW) {
                    break;
                }
                Event event = happeningNowList.get(i);

                LinearLayout container = new LinearLayout(getActivity());
                EventButtonOnTouchListener listener = new EventButtonOnTouchListener(event, container);

                ((Project_18) getActivity().getApplication()).makeEventButton
                        (getActivity(), event, container, listener, true);

                happeningNowLayout.addView(container);

                numLoaded++;
            }
        }
    }

    public void displayLaterEvents() {

        int startPoint = pageLater * NUM_LATER;
        int numLoaded = 0;

        // empty case
        if (laterList.isEmpty()) {
            getActivity().findViewById(R.id.emptyLaterTextContainer).setVisibility(View.VISIBLE);
        }

        // non empty case
        else {

            // Display events
            for (int i = startPoint; i < laterList.size(); i++) {

                if (numLoaded == NUM_LATER) {
                    break;
                }
                Event event = laterList.get(i);

                LinearLayout container = new LinearLayout(getActivity());
                EventButtonOnTouchListener listener = new EventButtonOnTouchListener(event, container);

                ((Project_18) getActivity().getApplication()).makeEventButton
                        (getActivity(), event, container, listener, true);

                laterLayout.addView(container);

                numLoaded++;
            }
        }
    }

    public class EventButtonOnTouchListener implements View.OnTouchListener {

        private Event e;
        private LinearLayout container;

        public EventButtonOnTouchListener(Event e, LinearLayout container) {
            this.e = e;
            this.container = container;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {

            if (event.getAction() == MotionEvent.ACTION_UP) {
                ((MainAct) getActivity()).goToEventInfo(e.getEvent_id(), true);
                container.setBackground(getResources().getDrawable(R.drawable.border_event_button));
            }
            if (event.getAction() == MotionEvent.ACTION_CANCEL) {
                container.setBackground(getResources().getDrawable(R.drawable.border_event_button));
            }
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                container.setBackground(getResources().
                        getDrawable(R.drawable.border_event_button_pressed));
            }
            return true;
        }

        public void setE(Event e) {
            this.e = e;
        }
    }

    public class ShowMoreOnTouchListener implements View.OnTouchListener {

        private String buttonName;
        private Button button;

        public ShowMoreOnTouchListener(String buttonName, Button button) {
            this.buttonName = buttonName;
            this.button = button;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {

            if (event.getAction() == MotionEvent.ACTION_UP) {
                showMore();
                button.setBackgroundColor(getResources().getColor(R.color.white));
            }
            if (event.getAction() == MotionEvent.ACTION_CANCEL) {
                button.setBackgroundColor(getResources().getColor(R.color.white));
            }
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                button.setBackgroundColor(getResources().getColor(R.color.colorDividerLight));
            }

            return true;
        }
        public void showMore() {
            if (buttonName.equals("showMoreHappeningNow")) {
                showMoreHappeningNow();
            }
            if (buttonName.equals("showMoreLater")) {
                showMoreLater();
            }
        }
    }

    private class popularityCompare implements Comparator<Event> {
        @Override
        public int compare(Event e1, Event e2) {
            if (e1.getAttendees().size() < (e2.getAttendees().size())) {
                return 1;
            }
            if (e1.getAttendees().size() > (e2.getAttendees().size())) {
                return -1;
            }
            return 0;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Firebase.setAndroidContext(getContext());
    }

}
