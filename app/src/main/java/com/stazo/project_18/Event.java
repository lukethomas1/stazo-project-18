package com.stazo.project_18;

import android.support.v7.app.AppCompatActivity;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.GenericTypeIndicator;
import com.firebase.client.ValueEventListener;

import java.util.HashMap;

/**
 * Created by isaacwang on 4/25/16.
 */
public class Event {
    private String name;
    private String description;
    private String creator_id;
    private String event_id = "yoo";
    private int type;
    private int popularity = 0;
    private long time;
    private boolean isConstructed = false; // has the event been constructed?

    // default constructor
    public Event(){}

    // constructor with Date
    public Event(String name, String description, String creator_id,
                 int type, long time) {
        this.name = name;
        this.description = description;
        this.creator_id = creator_id;
        this.type = type;
        this.time = time;
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
                        name = (String) event.get("name");
                        description = (String) event.get("description");
                        creator_id = (String) event.get("creator_id");
                        type = ((Integer) event.get("type")).intValue();
                        time = ((Integer) event.get("time")).longValue();
                        isConstructed = true;
                        System.out.println(time);
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {
                    }


                });
    }

    //Getters and setters
    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCreator_id(String creator_id) {
        this.creator_id = creator_id;
    }

    public void setEvent_id(String event_id) {
        this.event_id = event_id;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setPopularity(int popularity) {
        this.popularity = popularity;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public void setIsConstructed(boolean isConstructed) {
        this.isConstructed = isConstructed;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getCreator_id() {
        return creator_id;
    }

    public String getEvent_id() {
        return event_id;
    }

    public int getType() {
        return type;
    }

    public int getPopularity() {
        return popularity;
    }

    public long getTime() {
        return time;
    }

    public boolean isConstructed() {
        return isConstructed;
    }
}
