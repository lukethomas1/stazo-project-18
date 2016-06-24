package com.stazo.project_18;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


/**
 * A simple {@link Fragment} subclass.
 * to handle interaction events.
 */
public class SearchFrag extends Fragment {
    private View v;
    private Toolbar toolbar;
    private LinearLayout queryButtonLayout;
    private ArrayList<Event> allEvents;
    private ArrayList<Event> matchEvents;

    public SearchFrag() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_search, container, false);

        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        queryButtonLayout = (LinearLayout) getActivity().findViewById(R.id.queryButtonLayout);

        allEvents = ((Project_18) getActivity().getApplication()).getPulledEvents();
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        getActivity().getMenuInflater().inflate(R.menu.toolbar_search_menu, menu);

        toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.app_name));

    }

    public void updateResults(String query) {

        queryButtonLayout.removeAllViews();
        matchEvents = new ArrayList<Event>();
        ArrayList<Event> levelOne = new ArrayList<Event>();
        ArrayList<Event> levelTwo = new ArrayList<Event>();
        ArrayList<Event> levelThree = new ArrayList<Event>();

        if (query.equals(new String(""))) {
            return;
        }

        for (Event e: allEvents) {
            switch (e.findRelevance(query)) {
                case 3:
                    levelThree.add(e);
                    break;
                case 2:
                    levelTwo.add(e);
                    break;
                case 1:
                    levelOne.add(e);
                    break;
                default:
                    break;
            }
        }
        Collections.sort(levelThree, new popularityCompare());
        Collections.sort(levelTwo, new popularityCompare());
        Collections.sort(levelOne, new popularityCompare());

        matchEvents.addAll(levelThree);
        matchEvents.addAll(levelTwo);
        matchEvents.addAll(levelOne);

        for (final Event e: matchEvents) {
            Button eventButton = new Button(getContext());
            eventButton.setText(e.getName());
            makePretty(eventButton);
            eventButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    goToEventInfo(e.getEvent_id());
                }
            });
            queryButtonLayout.addView(eventButton);
        }

        if (matchEvents.isEmpty()) {
            TextView emptyText = new TextView(getActivity());
            emptyText.setText("No matches found");
            queryButtonLayout.addView(emptyText);
            makePretty(emptyText);
        }
    }

    private class popularityCompare implements Comparator<Event> {
        @Override
        public int compare(Event e1, Event e2) {
            if (e1.getPopularity() < (e2.getPopularity())) {
                return 1;
            }
            if (e1.getPopularity() > (e2.getPopularity())) {
                return -1;
            }
            return 0;
        }
    }

    private void makePretty(Button button){

        RelativeLayout.LayoutParams lp = new
                RelativeLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        button.setTextSize(12);
        //button.setTypeface(Typeface.MONOSPACE);
        button.setAllCaps(false);
        button.setGravity(Gravity.CENTER_VERTICAL);
        button.setPadding(40, 0, 0, 0);
        button.setLayoutParams(lp);
        button.setBackgroundColor(getResources().getColor(R.color.white));
    }

    private void makePretty(TextView tv) {
        /*ViewGroup.LayoutParams params = tv.getLayoutParams();
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        tv.setLayoutParams(params);*/

        tv.setGravity(Gravity.CENTER);
        tv.setTextSize(16);
        tv.setBackgroundColor(getResources().getColor(R.color.white));
        tv.setTextColor(getResources().getColor(R.color.colorTextPrimary));
        tv.setHeight(150);
    }

    private void goToEventInfo(String event_id) {
        ((MainAct) getActivity()).goToEventInfo(event_id);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
