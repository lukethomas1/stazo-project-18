package com.stazo.project_18;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.GenericTypeIndicator;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Fragment version of MapAct, also uses MapView instead of MapFragment from Google API
 */
public class MapFrag extends Fragment {

    public static final LatLng REVELLE = new LatLng(32.874447, -117.240914);

    private Firebase fb;
    private GoogleMap map;
    private MapHandler mapHandler;
    private MapView mapView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.map_overview, container, false);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        // Initialize Firebase
        Firebase.setAndroidContext(this.getActivity());
        fb = ((Project_18) this.getActivity().getApplication()).getFB();
        mapHandler = new MapHandler();

        // Initialize the map_overview
        mapView = (MapView) v.findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(mapHandler);

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

//    public void goToCreateEvent(View view) {
//        startActivity(new Intent(this.getActivity(), CreateEventAct.class));
//    }

    private void goToEventInfo(Marker marker) {
        // Get event's database id
        String event_id = mapHandler.getEventIDFromMarker(marker);

        // Delegate Activity switching to encapsulating activity
        ((MainAct)this.getActivity()).goToEventInfo(event_id);
    }

    public void filterRelevantEvents(String search) {
        Log.d("MyTag", "yo wee filteringgg");
        ArrayList<String> relevantEventIds =
                ((Project_18) getActivity().getApplication()).findRelevantEventIds(search);
        mapHandler.displayRelevantEvents(relevantEventIds);
    }

    /**
     * Map Handler
     */

    private class MapHandler extends FragmentActivity implements OnMapReadyCallback,
            ActivityCompat.OnRequestPermissionsResultCallback, GoogleMap.InfoWindowAdapter
    {
        // Hashmap from marker id to event id (for displaying event info)
        private HashMap<String, String> idLookupHM = new HashMap<>();

        // Hashmap from event id to marker (for hiding markers);
        private HashMap<String, Marker> markerLookupHM = new HashMap<>();

        private View infoWindow;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                final boolean enabled = true;
            } else {
                // Show rationale and request permission.
            }

            System.out.println( "Entered MapHandler's onCreate method." );
            System.err.println( "Entered MapHandler's onCreate method." );
        }

        @Override
        public View getInfoWindow(Marker marker) {


            return null; // Call getInfoContents
        }

        @Override
        public View getInfoContents(Marker marker) {
            render(marker, infoWindow);

            return infoWindow;
        }

        private void render(Marker marker, View inflatedLayout) {

            // Set title and event description
            TextView titleTV = (TextView) inflatedLayout.findViewById(R.id.eventTitleTV);
            TextView descTV = (TextView) inflatedLayout.findViewById(R.id.eventDescTV);

            titleTV.setText(marker.getTitle());
            descTV.setText(marker.getSnippet());
        }

        public String getEventIDFromMarker(Marker marker) {
            return idLookupHM.get(marker.getId());
        }

        public void onMapReady(GoogleMap googleMap) {
            // Initialize global variable
            map = googleMap;
            map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    //goToEventInfo(marker);

                    return false; // Perform default behavior: displaying InfoWindow
                }
            });

            // Inflate custom info window layout
            LayoutInflater inflater = LayoutInflater.from(getContext());

            infoWindow = inflater.inflate(R.layout.custom_info_window, null);

            // Initialize custom info window
            map.setInfoWindowAdapter(this);
            map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                @Override
                public void onInfoWindowClick(Marker marker) {
                    goToEventInfo(marker);
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

                            ((Project_18) getActivity().getApplication()).clearPulledEvents();
                            // For every event in fb.child("Events"), create event and displayEvent
                            for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                                // get the info, storage?
                                Event e = new Event(eventSnapshot.getValue(
                                        new GenericTypeIndicator<HashMap<String, Object>>() {
                                        }));

                                // add the event to the local ArrayList
                                ((Project_18) getActivity().getApplication()).addPulledEvent(e);

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

        // Hide irrelevant events, show relevant events
        public void displayRelevantEvents(ArrayList<String> relevantEventIds) {
            //Iterator it = markerLookupHM.entrySet().iterator();

            // iterate through map of event_ids to markers
            for (String key: markerLookupHM.keySet()) {
                //HashMap.Entry pair = (HashMap.Entry)it.next();
                //System.out.println(pair.getKey() + " = " + pair.getValue());
                // if the event_id of a marker is not contained in relevantEventIds
                if (relevantEventIds.contains(key)) {
                    // hide the marker
                    Log.d("MyTag", "Relevant: " + key.toString());
                    markerLookupHM.get(key).setVisible(true);
                }
                // if it is relevant, make it visible
                else {
                    Log.d("MyTag", "Irrelevant: " + key.toString());
                    markerLookupHM.get(key).setVisible(false);
                }
                //it.remove(); // avoids a ConcurrentModificationException
            }
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

            // Put the marker in a HashMap to hide markers later
            markerLookupHM.put(e.getEvent_id(), marker);
        }
    }
}