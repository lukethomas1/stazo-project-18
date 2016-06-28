package com.stazo.project_18;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;

/**
 * Created by luket on 6/20/2016.
 */
public class Notification {

    // TYPE 0: <num_users> commented on <event_name>    onClickID = eventId
    // TYPE 1: <user_i_am_following> is hosting <event_name> at <location> in <hours>   onClickID = eventId
    // TYPE 2: <user_who_followed_me> is now following you.     onClickID = userId

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

    public Notification(String text, boolean viewed, String ID, int type) {
        message = text;
        this.viewed = viewed;
        onClickID = ID;
        this.type = type;
    }

    public void pushToFirebase(Firebase fb, final ArrayList<String> usersWhoCare) {
        final Notification notif = this;

        // loop through NotifDatabase users and push notif to those who care
        fb.child("NotifDatabase").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot userNotifs: dataSnapshot.getChildren()) {
                    if (usersWhoCare.contains(userNotifs.getValue())) {
                        userNotifs.getRef().push().setValue(notif);
                    }
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
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
