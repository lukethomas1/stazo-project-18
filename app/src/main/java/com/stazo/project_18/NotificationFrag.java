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
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;

public class NotificationFrag extends android.support.v4.app.Fragment {

    private View v;
    private Firebase fb;
    private User currentUser;
    private ArrayList<Notification2> notifs = new ArrayList<>();
    private LinearLayout LL1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_notification, container, false);

        // Initialize the Firebase reference
        fb = ((Project_18) this.getActivity().getApplication()).getFB();

        currentUser = ((Project_18) this.getActivity().getApplication()).getMe();

        // Set the layout
        LL1 = (LinearLayout)v.findViewById(R.id.LL1);

        // Clear notifs to prevent duplicates
        notifs.clear();

        // Update the notifications and then display them
        pullNotifications(currentUser.getID());

        return v;
    }

    private void displayNotifications() {
        for (Notification2 not2 : notifs) {
            // Make a final copy of not2 so that it can be used inside the onclick setter
            final Notification2 not2Copy = not2;

            // Create a new button to add to the view
            Button butt = new Button(getActivity());
            // Set the text of the button
            butt.setText(not2.generateMessage());
            // Set the onclick of the button
            butt.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            not2Copy.onNotificationClicked(getActivity());
                        }
                    }
            );

            LL1.addView(butt);
        }
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
                    } else if (((Long) notifMap.get("type")).intValue() == Notification2.TYPE_INVITE_EVENT) {
                        notifs.add(new NotificationInviteEvent(notifMap));
                    }
                }

                // There are no notifications to show
                if(notifs.isEmpty()) {
                    ((TextView)v.findViewById(R.id.loadingText)).setText("No Notifications");
                }

                // There are notifications to show, show them
                else {
                    // Show them on the screen
                    displayNotifications();
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
