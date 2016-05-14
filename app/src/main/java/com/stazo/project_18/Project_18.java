package com.stazo.project_18;

import android.app.Application;
import android.provider.CalendarContract;

import com.firebase.client.Firebase;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by isaacwang on 4/25/16.
 */
public class Project_18 extends Application {
    private static final String fb = "https://stazo-project-18.firebaseio.com/";
    public static User me;
    public static ArrayList<Event> pulledEvents = new ArrayList<Event>(); // list of all the events pulled

    public Firebase getFB() { return new Firebase(fb);}
    public User getMe() { return me; }
    public void setMe(User user) { me = user; }

    // stores a pulled event locally (pulledEvents)
    public void addPulledEvent(Event e) {pulledEvents.add(e);}

    // returns list of event_ids in order of relevance
    public ArrayList<String> findRelevantEvents (String search) {
        ArrayList<String> relatedEvents = new ArrayList<String>();
        for (Event e: pulledEvents) {
            int relevance = e.findRelevance(search);
            switch (relevance) {
                case 2:
                    // add to start
                    relatedEvents.add(0, e.getEvent_id());
                    break;
                case 1:
                    // add to end
                    relatedEvents.add(e.getEvent_id());
                    break;
                default:
                    break;
            }
        }
        return relatedEvents;
    }
}
