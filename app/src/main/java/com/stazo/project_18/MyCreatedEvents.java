package com.stazo.project_18;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;

public class MyCreatedEvents extends AppCompatActivity {

    private Firebase fb;
    private Firebase userRef;
    private Firebase myEventsRef;
    ArrayList<Event> myEvents = new ArrayList<Event>();
    private TextView noEventText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_created_events);

        fb = ((Project_18) this.getApplication()).getFB();
        userRef = fb.child("Users");
        myEventsRef = fb.child("myEvents");




        fb.child("Users").addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                }
        );
    }

}
