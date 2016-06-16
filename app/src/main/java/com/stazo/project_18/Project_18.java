package com.stazo.project_18;

import android.app.Application;
import android.util.Log;

import com.firebase.client.Firebase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * Created by isaacwang on 4/25/16.
 */
public class Project_18 extends Application {
    private static final String fb = "https://stazo-project-18.firebaseio.com/";
    public static User me;
    public static ArrayList<Event> pulledEvents = new ArrayList<Event>(); // list of all the events pulled
    public static ArrayList<Integer> filteredCategories = new ArrayList<>();
    public static int GMTOffset = ((new GregorianCalendar()).getTimeZone()).getRawOffset();
    public static long relevantTime = System.currentTimeMillis();
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
        relevantTime = System.currentTimeMillis() + (progress * 60*60*1000);
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
                            e.isInTime(relevantTime)) {
                        // add to start
                        relatedEventIds.add(0, e.getEvent_id());
                    }
                    break;
                case 1:

                    // if it is a relevant category, and at a relevant time
                    if (filteredCategories.contains(new Integer(e.getType())) &&
                            e.isInTime(relevantTime)) {
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

    // filter based on search and categories, use time if !noTime
    public ArrayList<Event> findRelevantEvents (boolean noTime) {
        ArrayList<Event> relatedEvents = new ArrayList<Event>();
        for (Event e: pulledEvents) {
            int relevance = e.findRelevance(relevantText);
            switch (relevance) {
                case 2:
                    // if it is a relevant category, and at a relevant time
                    if (filteredCategories.contains(new Integer(e.getType())) &&
                            (noTime || e.isInTime(relevantTime))) {
                        // add to start
                        relatedEvents.add(0, e);
                    }
                    break;
                case 1:
                    // if it is a relevant category, and at a relevant time
                    if (filteredCategories.contains(new Integer(e.getType())) &&
                            (noTime || e.isInTime(relevantTime))) {
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

    // TRAIL STUFF

    // addTrail for category
    public void addTrail(Integer type) {
        me.addTrail(getFB(), type);
        me.pushToFirebase(getFB());
    }

    // addTrail for user
    public void addTrail(String userid) {
        me.addTrail(getFB(), userid);
        me.pushToFirebase(getFB());
    }
}
