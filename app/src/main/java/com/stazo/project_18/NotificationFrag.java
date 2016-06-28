package com.stazo.project_18;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.client.Firebase;

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_notification, container, false);

        // Initialize the Firebase reference
        fb = ((Project_18) this.getActivity().getApplication()).getFB();

        currentUser = ((Project_18) this.getActivity().getApplication()).getMe();

        return v;
    }

    private ArrayList<Notification> loadNotifications() {
        // List of follower IDs
        ArrayList<Notification> notifications = new ArrayList<>();

        //notifications = ( (ArrayList<Notification>) fb.child("Notifications").child(currentUser.getID()));



        return notifications;
    }
}
