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
    public static final int TYPE_INVITE_EVENT = 4;
    public static final int TYPE_WELCOME = 5;


    private String notifID;
    private boolean viewed = false;
    private int type;
    private String pictureId;

    public Notification2() {}

    // FOR GENERATING A NEW NOTIF
    public Notification2(int type, String pictureId) {
        generateNotifID();
        this.type = type;
        this.pictureId = pictureId;
    }

    // FOR PULLING AN EXISTING NOTIF
    public Notification2(int type, String notifID, String pictureId) {
        this.type = type;
        this.notifID = notifID;
        this.pictureId = pictureId;
    }

    public abstract void onNotificationClicked(Context context);
    public abstract String generateMessage();
    public abstract SnapToBase hasConflict(DataSnapshot userNotifs);
    public abstract Notification2 handleConflict(SnapToBase stb);

    public void pushToFirebase(final Firebase fb, final ArrayList<String> usersWhoCare) {
        final Notification2 thisNotif = this;

        for (final String id: usersWhoCare) {
            fb.child("NotifDatabase").child(id).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot userNotifs) {
                    SnapToBase stb = hasConflict(userNotifs);
                    if (stb != null) {
                        Notification2 resolved = handleConflict(stb);
                        if (resolved != null) {
                            fb.child("NotifDatabase").child(id).push().setValue(resolved);
                        }
                    }
                    else {
                        fb.child("NotifDatabase").child(id).push().setValue(thisNotif);
                    }
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {

                }
            });
        }
    }

    public void generateNotifID() {
        notifID = "";
        Random rand = new Random();
        for (int i = 0; i < 10; i++) {
            String id = "" + (char) (65 + rand.nextInt(26));
            notifID += id;
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


    public String getPictureId() {
        return pictureId;
    }

    public void setPictureId(String pictureId) {
        this.pictureId = pictureId;
    }
}
