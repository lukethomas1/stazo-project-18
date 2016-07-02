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

public class NotificationFrag extends android.support.v4.app.Fragment {

    private View v;
    private Firebase fb;
    private User currentUser;
    private ArrayList<Notification> notifs = new ArrayList<>();

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
        LinearLayout LL1 = ((LinearLayout) this.getActivity().findViewById(R.id.LL1));

        for(Notification not : notifs) {
            final Notification finalNot = not;
            Button butt = new Button(this.getActivity());
            butt.setText(not.getMessage());

            butt.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    goToEventInfo(finalNot.getOnClickID());
                    setToViewed(finalNot);
                }
            });

            if(LL1 != null) {
                LL1.addView(butt);
            }
        }
    }

    private void goToEventInfo(String event_id) {
        // Delegate Activity switching to encapsulating activity
        ((MainAct)this.getActivity()).goToEventInfo(event_id, true);
    }

    public void pullNotifications(String userId) {

        fb.child("NotifDatabase").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
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
