package com.stazo.project_18;

import android.util.Log;

import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;

/**
 * Created by isaacwang on 6/27/16.
 */

// TESTING PURPOSES ONLY
public class NotificationHandler {

    private ArrayList<Notification> notifs = new ArrayList<>();

    public NotificationHandler() {}

    public void generateNotifications() {
        Notification notif0 = new Notification("<num_users> commented on <event_name>",
                false, "yooGPHLVQTAYM", 0); // RICK ORD LEGACY
        Notification notif1 = new Notification("<user_i_am_following> is hosting <event_name> " +
                "at <location> in <hours>", false, "yooKPGCHIFIGR", 1); //SMASH
        Notification notif2 = new Notification("<user_who_followed_me> is now following you.",
                false, "1196215920412322", 2); //ISAAC
        ArrayList<String> usersWhoCare = new ArrayList<>();

        usersWhoCare.add("1184188798300386"); // BRIAN
        notif0.pushToFirebase(Project_18.getFB(), usersWhoCare);

        usersWhoCare.add("1177156832304841"); // ANSEL and BRIAN
        notif1.pushToFirebase(Project_18.getFB(), usersWhoCare);

        usersWhoCare.clear();
        usersWhoCare.add("1138117392898486"); // MATTHEW
        usersWhoCare.add("1131880253542315"); // LUKE
        usersWhoCare.add("1184188798300386"); // BRIAN AGAIN
        notif2.pushToFirebase(Project_18.getFB(), usersWhoCare);
    }

    // PULL AND PRINT BRIAN'S NOTIFICATIONS
    public void pullNotifications() {
        Project_18.getFB().child("NotifDatabase").
                child("1184188798300386").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot notifSnap: dataSnapshot.getChildren()) {
                    Notification notif = notifSnap.getValue(Notification.class);
                    notifs.add(notif);
                }

                Log.d("Notifications", "Notifications are: " + notifs.toString());
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }
}
