package com.stazo.project_18;

import android.support.v7.app.AppCompatActivity;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.GenericTypeIndicator;
import com.firebase.client.ValueEventListener;

import java.lang.reflect.Type;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by isaacwang on 4/25/16.
 */
public class Event {
    private String name;
    private String description;
    private String creator_id;
    private String event_id;
    private int type;
    private int popularity = 0;
    private Date date;
    private boolean isConstructed = false; // has the event been constructed?

    // default constructor
    public Event(){ this.isConstructed = true; }

    // constructor with Date
    public Event(String name, String description, String creator_id,
                 int type, Date date) {
        this.name = name;
        this.description = description;
        this.creator_id = creator_id;
        this.type = type;
        this.date = date;
        this.isConstructed = true;
    }

    // constructor with Date broken up
    public Event(String name, String description, String creator_id, int type,
                 int year, int month, int day, int hour, int minute) {
        this.name = name;
        this.description = description;
        this.creator_id = creator_id;
        this.type = type;
        this.date = new Date(year, month, day, hour, minute);
        this.isConstructed = true;
    }

    /**
     * Pushes the event to Firebase
     * @param fb The Firebase ref
     */
    public void pushToFirebase(Firebase fb) {
        fb.child("Events").child(event_id).setValue(this);
    }

    /**
     * Constructor for grabbing an already existing event on Firebase
     * Event myEvent = newEvent(fb, String event_id);
     */
    public Event(Firebase fb, String event_id) {

        // One-time listener for pulling information
        fb.child("Events").child(event_id).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        HashMap<String, Object> event = dataSnapshot.getValue(
                                new GenericTypeIndicator<HashMap<String, Object>>() {
                        });
                        HashMap<String, Object> dateMap = (HashMap<String, Object>) event.get("date");
                        name = (String) event.get("name");
                        description = (String) event.get("description");
                        creator_id = (String) event.get("creator_id");
                        type = ((Integer) event.get("type")).intValue();
                        date = new Date(
                                ((Integer) dateMap.get("year")).intValue(),
                                ((Integer) dateMap.get("month")).intValue(),
                                ((Integer) dateMap.get("day")).intValue(),
                                ((Integer) dateMap.get("hour")).intValue(),
                                ((Integer) dateMap.get("minute")).intValue()
                        );
                        isConstructed = true;
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {
                    }
                });

        // Wait until the event isConstructed
        while (!isConstructed){
            try {
                Thread.sleep(1000);                 //1000 milliseconds is one second.
            } catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
    }

}
