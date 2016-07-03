package com.stazo.project_18;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class NotificationFrag extends android.support.v4.app.Fragment {

    private View v;
    private Firebase fb;
    private User currentUser;
    private ArrayList<Notification2> notifs = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_notification, container, false);

        // Initialize the Firebase reference
        fb = ((Project_18) this.getActivity().getApplication()).getFB();

        currentUser = ((Project_18) this.getActivity().getApplication()).getMe();

        // Update the notifications
        pullNotifications(currentUser.getID());

        // Show them on the screen
        displayNotifications();

        return v;
    }

    private void displayNotifications() {

    }

    private void goToEventInfo(String event_id) {
        // Delegate Activity switching to encapsulating activity
        ((MainAct)this.getActivity()).goToEventInfo(event_id, true);
    }

    public void pullNotifications(String userToPullFrom) {
        Project_18.getFB().child("NotifDatabase").
                child(userToPullFrom).addListenerForSingleValueEvent(new ValueEventListener() {
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

    // Sets the current notif to "viewed" for user Me
    public void setToViewed(final Notification notif) {
        fb.child("NotifDatabase").child(Project_18.me.getID()).
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
