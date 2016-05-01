package com.stazo.project_18;

import android.content.Intent;
import android.os.Bundle;
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
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;

public class MapAct extends AppCompatActivity {

    public static final LatLng REVELLE = new LatLng(32.874447, -117.240914);

    private Firebase fb;
    private GoogleMap map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_overview);
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

        // Initialize the map_overview
        MapFragment mapFrag =
                (MapFragment) getFragmentManager().findFragmentById(R.id.map);

        mapFrag.getMapAsync(new MapHandler());

        /*placingEvent = new Event();

        placingEvent.setName("Roaring Revelle");
        placingEvent.setDescription("This event takes place off campus.\n" +
                                    "It is a Gatsby themed party, so come well-dressed!");*/
        //displayAllEvents();
    }


    protected void goToCreateEvent(View view) {
        startActivity(new Intent(this, CreateEventAct.class));
    }

    private void goToEventInfo() {
        startActivity(new Intent(this, EventInfoAct.class));
    }

    private class MapHandler extends FragmentActivity implements OnMapReadyCallback
    {
        public void onMapReady(GoogleMap googleMap) {
            // Initialize global variable
            map = googleMap;

            map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    goToEventInfo();

                    return true; // Do not perform default behavior: displaying InfoWindow
                }
            });

            displayAllEvents();

            // Initial Camera Position
            float zoom = 15;
            float tilt = 0;
            float bearing = 0;

            CameraPosition camPos = new CameraPosition(REVELLE, zoom, tilt, bearing);

            map.moveCamera(CameraUpdateFactory.newCameraPosition(camPos));
        }

        // Display all the events, should probably be called in onCreate
        private void displayAllEvents() {
            // Clear existing markers on the map
            map.clear();

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
                                HashMap<String, Object> locMap =
                                        ((HashMap<String, Object>) event.get("location"));
                                double lat = (double) locMap.get("latitude");
                                LatLng loc = new LatLng((double) (locMap.get("latitude")),
                                        (double) (locMap.get("longitude")));
                                Event e = new Event(
                                        (String) event.get("name"),
                                        (String) event.get("description"),
                                        (String) event.get("creator_id"),
                                        ((Integer) event.get("type")).intValue(),
                                        ((Integer) event.get("date")).longValue(),
                                        ((Integer) event.get("startTime")).longValue(),
                                        ((Integer) event.get("endTime")).longValue(),
                                        loc);

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

        private void displayEvent(Event e) {
            // Set the marker's parameters
            MarkerOptions markerOpts = new MarkerOptions();

            markerOpts.title(e.getName());
            markerOpts.snippet(e.getDescription());
            markerOpts.position(e.getLocation());

            // Add the marker to the map
            Marker marker = map.addMarker(markerOpts);
        }
    }
}