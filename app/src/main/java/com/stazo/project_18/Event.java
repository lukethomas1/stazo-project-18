package com.stazo.project_18;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

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

//    private GregorianCalendar startDate;
//    private GregorianCalendar endDate;
    private long startTime;
    private long endTime;
    private String test;

    private LatLng location;
    private ArrayList<String> attendees = new ArrayList<String>();
    // 7 types, indexes 0-6
    public static String types[] = {"Food", "Sports", "Performance", "Academic", "Social", "Gaming", "Other"};
    // Parallel array to types array, colors of the categories (courtesy of Sherry)
    public static float typeColors[] = {10,     // Red
                                        200,    // Blue
                                        80,     // Green
                                        30,     // Orange
                                        55,     // Yellow
                                        280,    // Purple
                                        230};   // Darker Blue

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
        setReports(in.readInt());
        setStartTime(in.readLong());
        setEndTime(in.readLong());
        setTest(in.readString());
    }

    // constructor without location
    public Event(String name, String description, String creator_id,
                 int type, long startTime, long endTime, String test) {
        this.name = name;
        this.description = description;
        this.creator_id = creator_id;
        this.type = type;
        this.startTime = startTime;
        this.endTime = endTime;
        this.test = test;
    }

    // constructor with location
    public Event(String name, String description, String creator_id, int type, LatLng location,
                 long startTime, long endTime) {
        this.name = name;
        this.description = description;
        this.creator_id = creator_id;
        this.type = type;
        this.location = location;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    // constructor with HashMap
    public Event(HashMap<String, Object> eventMap) {
        if (eventMap == null) {
            return;
        }
        this.name = (String) eventMap.get("name");
        this.description = (String) eventMap.get("description");
        this.creator_id = (String) eventMap.get("creator_id");
        this.type = ((Integer) eventMap.get("type")).intValue();
        this.popularity = ((Integer) eventMap.get("popularity")).intValue();
        this.reports = ((Integer) eventMap.get("reports")).intValue();

//        Date sDate = new Date((Long) eventMap.get("startDate"));
//        Date eDate = new Date((Long) eventMap.get("endDate"));
//        this.startDate = new GregorianCalendar(sDate.getYear(),
//                                               sDate.getMonth(),
//                                               sDate.getDay(),
//                                               sDate.getHours(),
//                                               sDate.getMinutes());
//        this.endDate = new GregorianCalendar(eDate.getYear(),
//                                             eDate.getMonth(),
//                                             eDate.getDay(),
//                                             eDate.getHours(),
//                                             eDate.getMinutes());

        HashMap<String, Object> locMap = ((HashMap<String,Object>) eventMap.get("location"));
        this.location = new LatLng((double) locMap.get("latitude"),
                (double) locMap.get("longitude"));
        this.event_id = (String) eventMap.get("event_id");
        this.startTime = ((Long) eventMap.get("startTime")).longValue();
        this.endTime = ((Long) eventMap.get("endTime")).longValue();

        //pull attendees with iterable
        this.attendees = new ArrayList<>();
        HashMap<Object, Object> attendeesIterable = (HashMap<Object, Object>) eventMap.get("attendees");
        if (attendeesIterable != null) {
            for(Object value : attendeesIterable.values()) {
                this.attendees.add((String) value);
            }
        }
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

    public ArrayList<String> getAttendees() {
        return attendees;
    }

    public void setAttendees(ArrayList<String> attendees) {
        this.attendees = attendees;
    }

    /**
     * Pushes the event to Firebase
     * @param fb The Firebase ref
     */
    public void pushToFirebase(Firebase fb, String creator_name, ArrayList<String> followers) {
        // generate a unique id
        generateID();

        // add self to attendee
        //attendees.add(creator_id);

        // Add event to the Events dictionary under event_id
        fb.child("Events").child(event_id).setValue(this);

        // Add event_id to the creator's list of events (myEvents)
        fb.child("Users").child(creator_id).child("myEvents").push().setValue(event_id);

        (new NotificationFriendHost(Notification2.TYPE_FRIEND_HOST, creator_name,
                event_id,
                name,
                getTimeString(true),
                creator_id)).pushToFirebase(fb, followers);

        //new ReportEventTask().execute("yo");

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

//    public void setStartDate(GregorianCalendar date) {
//        this.startDate = date;
//    }
//
//    public void setEndDate(GregorianCalendar date) { this.endDate = date;}

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

//    public GregorianCalendar getStartDate() {
//        return startDate;
//    }
//
//    public int getStartYear() { return startDate.getTime().getYear();}
//
//    public int getStartMonth() { return startDate.getTime().getMonth();}
//
//    public int getStartDay() { return startDate.getTime().getDay();}
//
//    public int getStartHour() { return startDate.getTime().getHours();}
//
//    public int getStartMinute() { return startDate.getTime().getMinutes();}
//
//    public GregorianCalendar getEndDate() {
//        return endDate;
//    }
//
//    public int getEndYear() { return endDate.getTime().getYear();}
//
//    public int getEndMonth() { return endDate.getTime().getMonth();}
//
//    public int getEndDay() { return endDate.getTime().getDay();}
//
//    public int getEndHour() { return endDate.getTime().getHours();}
//
//    public int getEndMinute() { return endDate.getTime().getMinutes();}

    public long getStartTime() { return this.startTime; }

    public long getEndTime() { return this.endTime; }

    public void setStartTime(long startTime) { this.startTime = startTime; }

    public void setEndTime(long endTime) { this.endTime = endTime; }

    public void setTest(String test) { this.test = test;}
    public String getTest() {return this.test;}

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

//        out.writeInt(getStartYear());
//        out.writeInt(getStartMonth());
//        out.writeInt(getStartDay());
//        out.writeInt(getStartHour());
//        out.writeInt(getStartMinute());

//        out.writeInt(getEndYear());
//        out.writeInt(getEndMonth());
//        out.writeInt(getEndDay());
//        out.writeInt(getEndHour());
//        out.writeInt(getEndMinute());

        System.out.println("out parse: " + getStartTime());
        System.out.println("out parse: " + getTest());
        System.out.println("out parse: " + getName());
        out.writeLong(getStartTime());
        out.writeLong(getEndTime());
        out.writeString(getTest());
    }

    public int describeContents() {
        return 0;
    }

    public void generateID() {
        Random rand = new Random();
        for (int i = 0; i < 10; i++) {
            //add 10 random chars onto yoo
            String add = "" + (char) (65 + rand.nextInt(26));

            event_id = event_id.concat("" + add);
        }
        Log.d("myTag", "GENERATING: " + this.event_id);
    }

    // determines how relevant this event is to a query (3,2,1,0)
    // 3 = name match at start, 2 = name match, 1 = description match, 0 = no match
    public int findRelevance(String search) {
        if (name.toLowerCase().contains(search.toLowerCase())) {
            // if the match is at the start, it is a level 3 match
            if (name.toLowerCase().indexOf(search.toLowerCase()) == 0) {
                return 3;
            }
            return 2;
        }
        if (description.toLowerCase().contains(search.toLowerCase())) {
            return 1;
        }
        return 0;
    }

    public boolean happeningLaterToday() {
        return new SimpleDateFormat("EE").format(startTime).
                equals(new SimpleDateFormat("EE", Locale.US).format(new Date())) &&
                endTime > System.currentTimeMillis();
    }
    public boolean happeningSoon() {
        return (startTime - System.currentTimeMillis()) < 60 * 60 * 1000 * 2 && endTime > System.currentTimeMillis();
    }

    public String getTimeString(boolean midSentence) {
        Date start = new Date(startTime);

        // convert start time to MM/dd format
        String startDay = new SimpleDateFormat("EE").format(start);

        // compare today's date
        if (startDay.equals(new SimpleDateFormat("EE", Locale.US).format(new Date()))) {
            if (midSentence) {
                startDay = "today";
            }
            else {
                startDay = "Today";
            }
        }

        SimpleDateFormat startTimeFormat = new SimpleDateFormat("h:mm a", Locale.US);

        return startDay + " at " + startTimeFormat.format(start);
    }

    public String toString() {
        String string = name;
        return string;
    }

    public String getAddress(Context context) {
        Geocoder geoCoder = new Geocoder(context);
        List<Address> matches = null;
        try {
            matches = geoCoder.getFromLocation(location.latitude, location.longitude, 1);
        } catch (IOException e) {

        } finally {
            if (matches == null) {
                return null;
            } else {
                return (matches.isEmpty() ? null : matches.get(0)).getAddressLine(0);
            }
        }
    }


    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Event)) {
            return false;
        }
        return event_id.equals(((Event) other).getEvent_id());
    }


    /*class ReportEventTask extends AsyncTask<String, Void, String> {
        private Exception exception;

        protected String doInBackground(String... urls) {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("https://dry-wave-59635.herokuapp.com/");

            try {
                // Add your data
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
                nameValuePairs.add(new BasicNameValuePair("event_id", event_id));
                nameValuePairs.add(new BasicNameValuePair("user_id", creator_id));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                HttpResponse response = httpclient.execute(httppost);
                return("nicuru");
            } catch (ClientProtocolException e) {
                // TODO Auto-generated catch block
                return("clientProtocalException");
            } catch (IOException e) {
                // TODO Auto-generated catch block
                return("ioexception");
            }
        }
        protected void onPostExecute(String yeet) {

        }
    }*/
}