package com.stazo.project_18;

import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;

import com.firebase.client.Firebase;
import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;

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
    private int reports = 0;
    private Date startDate;
    private Date endDate;
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
        setStartDate(new Date(in.readInt(),
                in.readInt(),
                in.readInt(),
                in.readInt(),
                in.readInt()));
        setEndDate(new Date(in.readInt(),
                in.readInt(),
                in.readInt(),
                in.readInt(),
                in.readInt()));
    }

    // constructor without location
    public Event(String name, String description, String creator_id,
                 int type, Date startDate, Date endDate) {
        this.name = name;
        this.description = description;
        this.creator_id = creator_id;
        this.type = type;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // constructor with location
    public Event(String name, String description, String creator_id, int type,
                 Date startDate, Date endDate, LatLng location) {
        this.name = name;
        this.description = description;
        this.creator_id = creator_id;
        this.type = type;
        this.startDate = startDate;
        this.endDate = endDate;
        this.location = location;
    }

    // constructor with HashMap
    public Event(HashMap<String, Object> eventMap) {
        this.name = (String) eventMap.get("name");
        this.description = (String) eventMap.get("description");
        this.creator_id = (String) eventMap.get("creator_id");
        this.type = ((Integer) eventMap.get("type")).intValue();
        //this.startDate = makeDate((HashMap<String, Object>) eventMap.get("startDate"));
        //this.endDate = makeDate((HashMap<String, Object>) eventMap.get("endDate"));
        this.startDate = new Date((Long) eventMap.get("startDate"));
        this.endDate = new Date((Long) eventMap.get("endDate"));
        HashMap<String, Object> locMap = ((HashMap<String,Object>) eventMap.get("location"));
        this.location = new LatLng((double) locMap.get("latitude"),
                (double) locMap.get("longitude"));
    }

    // makes a Date object out of a hashmap
    public Date makeDate(HashMap<String,Object> dateMap) {
        return new Date(
                ((Integer) dateMap.get("year")).intValue(),
                ((Integer) dateMap.get("month")).intValue(),
                ((Integer) dateMap.get("date")).intValue(),
                ((Integer) dateMap.get("hrs")).intValue(),
                ((Integer) dateMap.get("min")).intValue());
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

    public void setReports(int r) { this.reports = r; }

    public void setStartDate(Date date) {
        this.startDate = date;
    }

    public void setEndDate(Date date) { this.endDate = date;}

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

    public int getReports() { return reports; }

    public Date getStartDate() {
        return startDate;
    }

    public int getStartYear() { return startDate.getYear();}

    public int getStartMonth() { return startDate.getMonth();}

    public int getStartDay() { return startDate.getDay();}

    public int getStartHour() { return startDate.getHours();}

    public int getStartMinute() { return startDate.getMinutes();}

    public Date getEndDate() {
        return endDate;
    }

    public int getEndYear() { return endDate.getYear();}

    public int getEndMonth() { return endDate.getMonth();}

    public int getEndDay() { return endDate.getDay();}

    public int getEndHour() { return endDate.getHours();}

    public int getEndMinute() { return endDate.getMinutes();}

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
        out.writeInt(getReports());

        out.writeInt(getStartYear());
        out.writeInt(getStartMonth());
        out.writeInt(getStartDay());
        out.writeInt(getStartHour());
        out.writeInt(getStartMinute());

        out.writeInt(getEndYear());
        out.writeInt(getEndMonth());
        out.writeInt(getEndDay());
        out.writeInt(getEndHour());
        out.writeInt(getEndMinute());

    }

    public int describeContents() {
        return 0;
    }
}