package com.stazo.project_18;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.PermissionRequest;
import android.widget.Toast;

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

import java.security.acl.Permission;
import java.util.HashMap;

public class MapAct extends AppCompatActivity {

    public static final LatLng REVELLE = new LatLng(32.874447, -117.240914);

    private Firebase fb;
    private GoogleMap map;
    private MapHandler mapHandler;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_overview);

        //toolbar stuff
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //Toast.makeText(getApplicationContext(),""+(getSupportActionBar()==null),Toast.LENGTH_LONG).show();
        getSupportActionBar().setTitle("toolbar title");
        getSupportActionBar().setSubtitle("subtitle");

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {

                switch (menuItem.getItemId()){
                    case R.id.game:
                        Toast.makeText(getApplicationContext(),"clicked game icon!", Toast.LENGTH_SHORT).show();
                        return true;
                }

                return false;
            }
        });

        // Initialize Firebase
        Firebase.setAndroidContext(this);

        fb = ((Project_18) getApplication()).getFB();

        // Initialize the map_overview
        MapFragment mapFrag =
                (MapFragment) getFragmentManager().findFragmentById(R.id.map);

        mapHandler = new MapHandler();

        mapFrag.getMapAsync(mapHandler);
        /*placingEvent = new Event();

        placingEvent.setName("Roaring Revelle");
        placingEvent.setDescription("This event takes place off campus.\n" +
                                    "It is a Gatsby themed party, so come well-dressed!");*/
        //displayAllEvents();
    }

    // Menu icons are inflated just as they were with actionbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_toolbar, menu);
        return true;
    }

    public void goToCreateEvent(View view) {
        startActivity(new Intent(this, CreateEventAct.class));
    }

    private void goToEventInfo(Marker marker) {
        Intent intent = new Intent(this, EventInfoAct.class);

        // Get event's database id
        String event_id = mapHandler.idLookupHM.get(marker.getId());

        // Store the event ID as an extra
        intent.putExtra("event_id", event_id);

        startActivity(intent);
    }

    private class MapHandler extends FragmentActivity implements OnMapReadyCallback,
            ActivityCompat.OnRequestPermissionsResultCallback
    {
        private HashMap<String, String> idLookupHM = new HashMap<>();

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                final boolean enabled = true;
            } else {
                // Show rationale and request permission.
            }
        }

        public void onMapReady(GoogleMap googleMap) {
            // Initialize global variable
            map = googleMap;
            map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    goToEventInfo(marker);

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

        // Display all the events
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
                                Event e = new Event(eventSnapshot.getValue(
                                        new GenericTypeIndicator<HashMap<String, Object>>() {
                                        }));

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

        // Displays a single event
        private void displayEvent(Event e) {
            // Set the marker's parameters
            MarkerOptions markerOpts = new MarkerOptions();

            markerOpts.title(e.getName());
            markerOpts.snippet(e.getDescription());
            markerOpts.position(e.getLocation());

            // Add the marker to the map
            Marker marker = map.addMarker(markerOpts);

            // Put the marker in a HashMap to look up IDs later
            idLookupHM.put(marker.getId(), e.getEvent_id());
        }
    }
}