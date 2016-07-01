package com.stazo.project_18;

import android.content.Context;

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
public abstract class Notification2 {

    // TYPE 0: <num_users> commented on <event_name>    onClickID = eventId
    // TYPE 1: <user_i_am_following> is hosting <event_name> at <location> in <hours>   onClickID = eventId
    // TYPE 2: <user_who_followed_me> is now following you.     onClickID = userId

    public static final int TYPE_COMMENT_EVENT = 0;
    public static final int TYPE_FRIEND_HOST = 1;
    public static final int TYPE_NEW_FOLLOW = 2;
    public static final int TYPE_JOINED_EVENT = 3;

    private String notifID;
    private boolean viewed = false;
    private int type;

    public Notification2() {}

    // Type 2
    public Notification2(int type) {
        generateNotifID();
        this.type = type;
    }

    public abstract void onNotificationClicked(Context context);
    public abstract String generateMessage();

    public void pushToFirebase(Firebase fb, final ArrayList<String> usersWhoCare) {
        final Notification2 notif = this;

        for (String id: usersWhoCare) {
            fb.child("NotifDatabase").child(id).push().setValue(notif);
        }
    }

    public void pushToFirebase(Firebase fb, String userWhoCares) {
        final Notification2 notif = this;

        fb.child("NotifDatabase").child(userWhoCares).push().setValue(notif);
    }

    public void generateNotifID() {
        Random rand = new Random();
        for (int i = 0; i < 10; i++) {
            String id = "" + (char) (65 + rand.nextInt(26));
            notifID = id;
        }
    }

    public String getNotifID() {return notifID;}

    public void setNotifID(String notifID) {this.notifID = notifID;}

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
