package com.stazo.project_18;

import android.util.Log;

import com.firebase.client.Firebase;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.GregorianCalendar;

/**
 * Created by isaacwang on 5/18/16.
 */

/* PURELY FOR TESTING PURPOSES, an easy way to clear/generate events */
public class EventHandler {
    public static ArrayList<LatLng> locations = new ArrayList<LatLng>();

    // offset from the epoch
    private static int GMTOffset = ((new GregorianCalendar()).getTimeZone()).getRawOffset();


    public EventHandler() {
        locations.clear();
        locations.add(new LatLng(32.890194, -117.241424));
        locations.add(new LatLng(32.884310, -117.241561));
        locations.add(new LatLng(32.879037, -117.237329));
        locations.add(new LatLng(32.874614, -117.241620));
        locations.add(new LatLng(32.874950, -117.232011));
        locations.add(new LatLng(32.881387, -117.231324));
        locations.add(new LatLng(32.886526, -117.236872));
    }

    public void clearEvents() {
        (new Firebase("https://stazo-project-18.firebaseio.com/")).child("Events").setValue(null);
        (new Firebase("https://stazo-project-18.firebaseio.com/")).child("Users").
                child("1196215920412322").child("myEvents").setValue(null);
    }

    public void generateEvents() {
        for (int i = 0; i < Event.types.length; i++) {
            Event e = new Event("Event " + i, "Description " + i, "1196215920412322", i, locations.get(i),
            System.currentTimeMillis(),     // all events start "now"
                    System.currentTimeMillis() + (i+2) * 1000 * 60 * 60); // and end in 2,3,4... hours
            //Log.d("myTag", "" + e.getEvent_id());

            // Popularity generation
            e.setPopularity((int) (Math.random() * 30));

            e.pushToFirebase(new Firebase("https://stazo-project-18.firebaseio.com/"));
        }
    }
}
