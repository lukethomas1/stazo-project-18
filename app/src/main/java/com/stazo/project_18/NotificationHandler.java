package com.stazo.project_18;

import android.content.Context;
import android.util.Log;

import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by isaacwang on 6/27/16.
 */

// TESTING PURPOSES ONLY
public class NotificationHandler {

    private ArrayList<Notification2> notifs = new ArrayList<>();

    public NotificationHandler() {}

    public void generateNotifications(Context context) {
        ArrayList<String> userNames = new ArrayList<String>();
        userNames.add("Jason");
        userNames.add("Steve");
        userNames.add("Mark");
        String eventId = "yooGPHLVQTAYM";
        NotificationCommentEvent nce = new NotificationCommentEvent(Notification2.TYPE_COMMENT_EVENT,
                userNames, eventId, "Legacy Speech");
        NotificationFriendHost nfe = new NotificationFriendHost(Notification2.TYPE_FRIEND_HOST,
                "Bob the host", "yooKPGCHIFIGR", "Smash Bros Party", "Today at 7:00pm");
        NotificationNewFollow nnf = new NotificationNewFollow(Notification2.TYPE_NEW_FOLLOW,
                "Melissa the follower", "1177156832304841");
        NotificationJoinedEvent nje = new NotificationJoinedEvent(Notification2.TYPE_JOINED_EVENT,
                "Alice the event joiner", "yooQEFISGNDVK", "TamarackSocial");

        ArrayList<String> usersWhoCare = new ArrayList<>();
        usersWhoCare.add("1184188798300386"); // Brian

        /*nce.pushToFirebase(Project_18.getFB(), usersWhoCare);
        nfe.pushToFirebase(Project_18.getFB(), usersWhoCare);
        nnf.pushToFirebase(Project_18.getFB(), usersWhoCare);
        nje.pushToFirebase(Project_18.getFB(), usersWhoCare);*/

        pullNotifications();
    }

    // PULL AND PRINT BRIAN'S NOTIFICATIONS
    public void pullNotifications() {
        Project_18.getFB().child("NotifDatabase").
                child("1184188798300386").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot notifSnap : dataSnapshot.getChildren()) {
                    HashMap<String, Object> notifMap = (HashMap<String, Object>) notifSnap.getValue();
                    if (((Long) notifMap.get("type")).intValue() == Notification2.TYPE_COMMENT_EVENT) {
                        notifs.add(new NotificationCommentEvent(notifMap));
                    } else if (((Long) notifMap.get("type")).intValue() == Notification2.TYPE_FRIEND_HOST) {
                        notifs.add(new NotificationFriendHost(notifMap));
                    } else if (((Long) notifMap.get("type")).intValue() == Notification2.TYPE_NEW_FOLLOW) {
                        notifs.add(new NotificationNewFollow(notifMap));
                    } else if (((Long) notifMap.get("type")).intValue() == Notification2.TYPE_JOINED_EVENT) {
                        notifs.add(new NotificationJoinedEvent(notifMap));
                    }
                }

                for (Notification2 notif: notifs) {
                    Log.d("notifs", notif.generateMessage());
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    public void setToViewed(final Notification notif) {
        Project_18.getFB().child("NotifDatabase").child("1184188798300386").
                addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot notifSnap : dataSnapshot.getChildren()) {
                            Notification pulledNotif = notifSnap.getValue(Notification.class);
                            if (pulledNotif.getNotifID().equals(notif.getNotifID())) {
                                notifSnap.getRef().child("viewed").setValue(true);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                });
    }
}
