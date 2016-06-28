package com.stazo.project_18;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link NotificationFrag.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link NotificationFrag#newInstance} factory method to
 * create an instance of this fragment.
 */
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

        

        return v;
    }

    private ArrayList<Notification> loadNotifications() {
        // List of follower IDs
        ArrayList<Notification> notifications = new ArrayList<>();

        //notifications = ( (ArrayList<Notification>) fb.child("Notifications").child(currentUser.getID()));



        return notifications;
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
}
