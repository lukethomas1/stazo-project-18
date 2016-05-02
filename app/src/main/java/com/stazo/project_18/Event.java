package com.stazo.project_18;

import android.os.Parcel;
import android.os.Parcelable;

import com.firebase.client.Firebase;
import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;

/**
 * Created by isaacwang on 4/25/16.
 */
public class Event implements Parcelable {
    private String name;
    private String description;
    private String creator_id;
    private String event_id = "yoo";
    private int type;
    private int popularity = 0;
    private long date, startTime, endTime;
    private LatLng location;

    // Extracts the Event from a Parcel for CreateEventAct -> LocSelectAct
    public static final Parcelable.Creator<Event> CREATOR
            = new Parcelable.Creator<Event>() {

        public Event createFromParcel(Parcel in) {
            return new Event(in);
        }

        public Event[] newArray(int size) {
            return new Event[size];
        }
    };

    // default constructor
    public Event(){}

    // Parsing Parcel Constructor
    public Event(Parcel in) {
        setName(in.readString());
        setDescription(in.readString());
        setCreator_id(in.readString());
        setEvent_id(in.readString());
        setType(in.readInt());
        setPopularity(in.readInt());
        setDate(in.readLong());
        setStartTime(in.readLong());
        setEndTime(in.readLong());
    }

    // constructor without location
    public Event(String name, String description, String creator_id,
                 int type, long date, long startTime, long endTime) {
        this.name = name;
        this.description = description;
        this.creator_id = creator_id;
        this.type = type;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
    }
    // constructor with location
    public Event(String name, String description, String creator_id,
                 int type, long date, long startTime, long endTime, LatLng location) {
        this.name = name;
        this.description = description;
        this.creator_id = creator_id;
        this.type = type;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.location = location;
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

    public void setLocation(LatLng newLoc) {
        this.location = newLoc;
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

    public void setDate(long date) {
        this.date = date;
    }

    public void setStartTime(long time) {
        this.startTime = time;
    }

    public void setEndTime(long time) {
        this.endTime = time;
    }

    public String getName() {
        return name;
    }

    public LatLng getLocation() {
        return location;
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

    public long getDate() {
        return date;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    /**
     * Packages the Event in a Parcel for the CreateEventAct -> LocSelectAct Intent.
     * @param out The Parcel to package the Event in.
     * @param flags Special state flags for the Parcel (unused).
     */
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(getName());
        out.writeString(getDescription());
        out.writeString(getCreator_id());
        out.writeString(getEvent_id());
        out.writeInt(getType());
        out.writeInt(getPopularity());
        out.writeLong(getDate());
        out.writeLong(getStartTime());
        out.writeLong(getEndTime());
    }

    public int describeContents() {
        return 0;
    }
}