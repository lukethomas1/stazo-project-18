package com.stazo.project_18;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 * Created by luket on 6/20/2016.
 */
public class Notification {

    // TYPE 0: <num_users> commented on <event_name>    onClickID = eventId
    // TYPE 1: <user_i_am_following> is hosting <event_name> at <location> in <hours>   onClickID = eventId
    // TYPE 2: <user_who_followed_me> is now following you.     onClickID = userId

    public String getNotifID() {
        return notifID;
    }

    public void setNotifID(String notifID) {
        this.notifID = notifID;
    }

    private String notifID;
    private String message;
    private String creatorName;
    private String onClickID;       // User OR Event ID depending on type
    private boolean viewed = false;
    private int type = 0;

    public Notification() {
        message = "";
        creatorName = "";
        onClickID = "";
    }

    // Type 2
    public Notification(String ID, String name, int type) {
        this.viewed = false;
        onClickID = ID;
        this.type = type;
        generateNotifID();

        // TYPE 2: <user_who_followed_me> is now following you.     onClickID = userId
        if(type == 2) {
            message = name + " is now follwing you.";
        }
    }

    // Type 1
    public Notification(String eventID, String eventName, String userName, int type) {
        this.viewed = false;
        onClickID = eventID;
        this.type = type;
        generateNotifID();

        // TYPE 1: <user_name> is hosting <event_name> at <location> in <hours>   onClickID = eventId
        if(type == 1) {
            message = userName + " is hosting " + eventName + ".";
        }
    }

    public Notification(String text, boolean viewed, String ID, int type) {
        message = text;
        this.viewed = viewed;
        onClickID = ID;
        this.type = type;
        generateNotifID();
    }

    public void pushToFirebase(Firebase fb, final ArrayList<String> usersWhoCare) {
        final Notification notif = this;

        for (String id: usersWhoCare) {
            fb.child("NotifDatabase").child(id).push().setValue(notif);
        }
    }

    public void pushToFirebase(Firebase fb, String userWhoCares) {
        final Notification notif = this;

        fb.child("NotifDatabase").child(userWhoCares).push().setValue(notif);
    }

    public void generateNotifID() {
        Random rand = new Random();
        for (int i = 0; i < 10; i++) {
            String id = "" + (char) (65 + rand.nextInt(26));
            notifID = id;
        }
    }



    public String toString() {
        return message;
    }

    // NECESSARY FOR FIREBASE
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

    public String getOnClickID() {
        return onClickID;
    }

    public void setOnClickID(String onClickID) {
        this.onClickID = onClickID;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public boolean isViewed() {
        return viewed;
    }

    public void setViewed(boolean viewed) {
        this.viewed = viewed;
    }
}
