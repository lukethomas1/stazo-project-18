package com.stazo.project_18;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;


import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.GenericTypeIndicator;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;

public class MapAct extends AppCompatActivity {

    private Firebase fb;
    private GoogleMap map;
    private MapHandler mapHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        }); */

        // Initialize Firebase
        Firebase.setAndroidContext(this);

        fb = ((Project_18) getApplication()).getFB();

        // Initialize the map
        MapFragment mapFrag =
                (MapFragment) getFragmentManager().findFragmentById(R.id.map);

        mapFrag.getMapAsync(new MapHandler());

    }

    // Display all the events, should probably be called in onCreate
    private void displayAllEvents() {

        // Listener for pulling the events
        fb.child("Events").addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        // For every event in fb.child("Events"), create event and displayEvent
                        for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {

                            // get the info, storage?
                            HashMap<String, Object> event = eventSnapshot.getValue(
                                    new GenericTypeIndicator<HashMap<String, Object>>() {
                                    });
                            Event e = new Event(
                                    (String) event.get("name"),
                                    (String) event.get("description"),
                                    (String) event.get("creator_id"),
                                    ((Integer) event.get("type")).intValue(),
                                    ((Integer) event.get("time")).longValue());

                            // display event
                            displayEvent(e);
                        }

                        // remove this listener
                        fb.child("Events").removeEventListener(this);
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {
                    }
                });
    }

    // Ansel and Matt TODO Should add marker for event
    private void displayEvent(Event e) {
        // Add marker for single event
        MarkerOptions marker = new MarkerOptions();

        marker.title(e.getName());
    }

    private class MapHandler extends FragmentActivity implements OnMapReadyCallback
    {
        public void onMapReady(GoogleMap googleMap) {
            map = googleMap;
            LatLng sydney = new LatLng(-34, 151);
            map.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
            map.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        }
    }

}
