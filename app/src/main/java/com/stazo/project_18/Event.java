package com.stazo.project_18;

import android.support.v7.app.AppCompatActivity;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.GenericTypeIndicator;
import com.firebase.client.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

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
    }

    /**
     * Pushes the event to Firebase
     * @param fb The Firebase ref
     */
    public void pushToFirebase(Firebase fb) {

        // Add event to the Events dictionary under event_id
        fb.child("Events").child(event_id).setValue(this);

        // Add event_id to the creator's list of events (myEvents)
        fb.child("Users").child(creator_id).child("myEvents").push().setValue(event_id);
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

}
