package com.stazo.project_18;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import java.util.ArrayList;

public class ListAct extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        // This is where the events will be loaded into the arraylist from firebase

        ArrayList<Event> eventList = new ArrayList<Event>();
        Event tester = new Event("FBGM",
                "The goal of this event is to disregard women and acquire riches." +
                        "We will be offering free bro-tanks and snapbacks.",
                "Wiz Khalifa", 3, 15, 2034, 2034);
        Event tester1 = new Event("TEST2",
                "The goal of this event actively shame the expression of free speech" +
                        "We will be offering free bro-tanks and snapbacks.",
                "Wiz Khalifa", 3, 15, 2034, 2034);
        Event tester2 = new Event("TEST3",
                "The goal of this event actively shame the expression of free speech" +
                        "We will be offering free bro-tanks and snapbacks.",
                "Wiz Khalifa", 3, 15, 2034, 2034);
        Event tester3 = new Event("TEST4",
                "The goal of this event actively shame the expression of free speech" +
                        "We will be offering free bro-tanks and snapbacks.",
                "Wiz Khalifa", 3, 15, 2034, 2034);
        Event tester4 = new Event("TEST5",
                "The goal of this event actively shame the expression of free speech" +
                        "We will be offering free bro-tanks and snapbacks.",
                "Wiz Khalifa", 3, 15, 2034, 2034);

        eventList.add(tester);
        eventList.add(tester1);
        eventList.add(tester2);
        eventList.add(tester3);
        eventList.add(tester4);

        // This is where the events will be displayed

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
