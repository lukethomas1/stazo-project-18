package com.stazo.project_18;

import android.app.Application;
import android.util.Log;

import com.firebase.client.Firebase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

/**
 * Created by isaacwang on 4/25/16.
 */
public class Project_18 extends Application {
    private static final String fb = "https://stazo-project-18.firebaseio.com/";
    public static User me;
    public static ArrayList<Event> pulledEvents = new ArrayList<Event>(); // list of all the events pulled
    public static ArrayList<Integer> filteredCategories = new ArrayList<>();
    //public static long relevantTime = (new Date()).getTime();
    public static long relevantTime = Calendar.getInstance().getTimeInMillis();
    public static String relevantText = new String();
    public Firebase getFB() { return new Firebase(fb);}
    public User getMe() { return me; }
    public void setMe(User user) { me = user; }

    // stores a pulled event locally (pulledEvents)
    public void addPulledEvent(Event e) {
        if (!pulledEvents.contains(e)) {
            pulledEvents.add(e);
        }
    }
    public ArrayList<Event> getPulledEvents (){return pulledEvents;}
    public void clearPulledEvents() {pulledEvents = new ArrayList<Event>();}

    // update the time we're interested in
    public void setRelevantTime(int progress) {
        relevantTime = Calendar.getInstance().getTimeInMillis() + (progress * 60*60*1000);
        Log.d("myTag", "" + TimeUnit.MILLISECONDS.toHours(relevantTime));
    }

    public long getRelevantTime() {return relevantTime;}

    public void setRelevantText(String newText) {
        relevantText = newText;
    }

    // returns list of event_ids in order of relevance
    public ArrayList<String> findRelevantEventIds () {
        ArrayList<String> relatedEventIds = new ArrayList<String>();
        for (Event e: pulledEvents) {
            int relevance = e.findRelevance(relevantText);
            switch (relevance) {
                case 2:
                    // if it is a relevant category, and at a relevant time
                    if (filteredCategories.contains(new Integer(e.getType())) &&
                    e.getStartDate().getTime() < relevantTime &&
                            e.getEndDate().getTime() > relevantTime) {
                        // add to start
                        relatedEventIds.add(0, e.getEvent_id());
                    }
                    break;
                case 1:

                    // if it is a relevant category, and at a relevant time
                    if (filteredCategories.contains(new Integer(e.getType())) &&
                            e.getStartDate().getTime() < relevantTime &&
                            e.getEndDate().getTime() > relevantTime) {
                        // add to end
                        relatedEventIds.add(e.getEvent_id());
                    }
                    break;
                default:
                    break;
            }
        }
        return relatedEventIds;
    }

    // filter based on search and categories, use time of !noTime
    public ArrayList<Event> findRelevantEvents (boolean noTime) {
        ArrayList<Event> relatedEvents = new ArrayList<Event>();
        for (Event e: pulledEvents) {
            int relevance = e.findRelevance(relevantText);
            switch (relevance) {
                case 2:
                    // if it is a relevant category, and at a relevant time
                    if (filteredCategories.contains(new Integer(e.getType())) &&
                            (noTime || e.getStartDate().getTime() < relevantTime &&
                            e.getEndDate().getTime() > relevantTime)) {
                        // add to start
                        relatedEvents.add(0, e);
                    }
                    break;
                case 1:
                    // if it is a relevant category, and at a relevant time
                    if (filteredCategories.contains(new Integer(e.getType())) &&
                            (noTime || e.getStartDate().getTime() < relevantTime &&
                            e.getEndDate().getTime() > relevantTime)) {
                        // add to end
                        relatedEvents.add(e);
                    }
                    break;
                default:
                    break;
            }
        }
        return relatedEvents;
    }
}
