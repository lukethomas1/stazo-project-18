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
    public static ArrayList<String> us = new ArrayList<String>();
    public static ArrayList<String> eventNames = new ArrayList<String>();
    public static ArrayList<String> eventDescriptions = new ArrayList<String>();

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
        us.add("10209766334938822");
        us.add("1070949549640758");
        us.add("1076100269116381");
        us.add("1131880253542315");
        us.add("1138117392898486");
        us.add("1177156832304841");
        us.add("1184188798300386");
        us.add("1196215920412322");
        eventNames.add("Super Smash Bros in OVL");
        eventNames.add("Sup y'all come out to [N]Motion!!!");
        eventNames.add("Theta Tau Chipotle Fundraiser @ Library Walk");
        eventNames.add("Idk bored lol come chill");
        eventNames.add("Social in Tamarack Floor 7");
        eventNames.add("Stargazing trip! All welcome!!!");
        eventNames.add("Rick Ord Legacy Speech");
        eventDescriptions.add("Controllers and pizza provided");
        eventDescriptions.add("NEWCOMERS WELCOME!! 4NO1 teaching it's gonna be lit!");
        eventDescriptions.add("Come out for burritos/bowls!!");
        eventDescriptions.add("srsly pls");
        eventDescriptions.add("soda and other drinks provided");
        eventDescriptions.add("gonna look at some stars");
        eventDescriptions.add("Legacy Speech by professor Ord @ PC");
    }

    public void clearEvents() {
        (new Firebase("https://stazo-project-18.firebaseio.com/")).child("Events").setValue(null);

        for (String id: us) {
            (new Firebase("https://stazo-project-18.firebaseio.com/")).child("Users").
                    child(id).child("myEvents").setValue(null);
            (new Firebase("https://stazo-project-18.firebaseio.com/")).child("Users").
                    child(id).child("attendingEvents").setValue(null);
        }
    }

    public void generateEvents() {
        for (int i = 0; i < 7; i++) {
            Event e = new Event(eventNames.get(i), eventDescriptions.get(i), us.get(i+1), 0, locations.get(i),
            System.currentTimeMillis()+ ((i+1) * 6 - 3) * (1000 * 60 * 60),     // events start in 3, 9, 15 hours
                    System.currentTimeMillis() + ((i+1) * 6) * (1000 * 60 * 60)); //  end in 6, 12, 18
            //Log.d("myTag", "" + e.getEvent_id());

            // Popularity generation
            e.setPopularity((int) (Math.random() * 60));

            e.pushToFirebase(new Firebase("https://stazo-project-18.firebaseio.com/"),
                    "Tester McTest", us);
        }
    }
}
