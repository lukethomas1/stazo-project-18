package com.stazo.project_18;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.GenericTypeIndicator;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

/**
 * Fragment version of MapAct, also uses MapView instead of MapFragment from Google API
 */
public class MapFrag extends Fragment {

    //public static final LatLng REVELLE = new LatLng(32.874447, -117.240914);
    public static final LatLng REVELLE = new LatLng(32.881759, -117.236824);
    private Firebase fb;
    private GoogleMap map;
    private MapHandler mapHandler;
    private MapView mapView;
    private GoogleApiClient mGoogleApiClient;

    // When we've focused on an event
    private static final float ZOOM_IN = 16;

    // Initial Camera Position
    private float zoom = 14.5f;
    private float tilt = 0;
    private float bearing = 0;
    // Time seekbar
    private SeekBar seekbar;
    private TextView timeTextView;

    private float testTime;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.map_overview, container, false);

        // Initialize Firebase
        Firebase.setAndroidContext(this.getActivity());
        fb = ((Project_18) this.getActivity().getApplication()).getFB();
        mapHandler = new MapHandler();

        // Initialize the map_overview
        mapView = (MapView) v.findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(mapHandler);

        testTime = System.nanoTime();

        return v;
    }

    // Seekbar handling
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        seekbar = (SeekBar) getView().findViewById(R.id.timeSeekBar);
        timeTextView = (TextView) getView().findViewById(R.id.timeTextView);
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                ((Project_18) getActivity().getApplication()).setRelevantTime(progress);

                // set the text time
                long rTime = ((Project_18) getActivity().getApplication()).getRelevantTime();
                String period = "AM";
                String time = "";
                int DSTOffset = ((new GregorianCalendar()).getTimeZone()).getDSTSavings();
                long hours = TimeUnit.MILLISECONDS.toHours(rTime + DSTOffset +
                        Project_18.GMTOffset) % 24;
                if (progress == 0) {
                    timeTextView.setText("Now");
                } else {
                    if (hours >= 12 && hours < 24) {
                        period = "PM";
                    }
                    if (hours > 12) {
                        hours -= 12;
                    }
                    if (hours <= 0) {
                        hours += 12;
                    }
                    time = (String.format("%2d:%02d",
                            hours,
                            (TimeUnit.MILLISECONDS.toMinutes(rTime) % 60)
                    ));
                    time += period;
                    timeTextView.setText(time);
                }

                // filter out the irrelevant events
                //filterRelevantEvents();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                timeTextView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                timeTextView.setVisibility(View.INVISIBLE);
            }
        });

        // init to zero
        seekbar.setProgress(0);
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


    private void goToEventInfo(Marker marker) {
        // Get event's database id
        String event_id = mapHandler.getEventIDFromMarker(marker);

        // Delegate Activity switching to encapsulating activity
        ((MainAct)this.getActivity()).goToEventInfo(event_id, false);
    }

    public void simulateOnClick(String event_id) {
        mapHandler.simulateOnClick(event_id);
    }

    /**
     * Map Handler
     */
    private class MapHandler extends FragmentActivity implements OnMapReadyCallback,
            ActivityCompat.OnRequestPermissionsResultCallback, GoogleMap.InfoWindowAdapter,
            GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener
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
        }

        @Override
        public View getInfoWindow(Marker marker) {
            render(marker, infoWindow);

            return infoWindow;
        }

        @Override
        public View getInfoContents(Marker marker) {
            return null;
        }

        public void simulateOnClick(String eventId) {
            if (markerLookupHM.containsKey(eventId))
                simulateOnClick(markerLookupHM.get(eventId));
        }

        public void simulateOnClick(Marker marker) {
            marker.showInfoWindow();
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), ZOOM_IN), 250, null);
        }

        private void render(Marker marker, View inflatedLayout) {
            // Set title and event description
            TextView titleTV = (TextView) inflatedLayout.findViewById(R.id.eventTitleTV);

            titleTV.setText(marker.getTitle());
        }

        public String getEventIDFromMarker(Marker marker) {
            return idLookupHM.get(marker.getId());
        }

        public void onMapReady(GoogleMap googleMap) {

            Log.d("TimeTest", "Map ready at " + (System.nanoTime() - testTime));

            // Initialize global variable
            map = googleMap;
            map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    goToEventInfo(marker);
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
            map.setOnInfoWindowCloseListener(new GoogleMap.OnInfoWindowCloseListener() {
                @Override
                public void onInfoWindowClose(Marker marker) {
                    // Hides the eventInfoFrag when the infoWindow disappears

                    if (getActivity() != null) {
                        ((MainAct) getActivity()).hideInfo();
                    }
                }
            });

            displayAllEvents();


            CameraPosition camPos = new CameraPosition(REVELLE, zoom, tilt, bearing);

            map.moveCamera(CameraUpdateFactory.newCameraPosition(camPos));

            // bring the bar to the front
            seekbar.bringToFront();
            timeTextView.bringToFront();


            // Initialize API client
            mGoogleApiClient = new GoogleApiClient
                    .Builder(getActivity().getApplicationContext())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .addApi(Places.GEO_DATA_API)
                    .addApi(Places.PLACE_DETECTION_API)
                    .build();

            // Permission checking: if no permissions, return
            if (ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(),
                    android.Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(),
                    android.Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {

                return;
            }

            // Button to auto-locate current position
            map.setMyLocationEnabled(true);
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

                                //((Project_18) getActivity().getApplication()).clearPulledEvents();
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

                                Log.d("TimeTest", "Non-cached events loaded at " + (System.nanoTime() - testTime));

                                // if we have an eventInfo open, go to that
                                if (MainAct.eventInfoFrag != null) {
                                    simulateOnClick(MainAct.eventInfoFrag.getPassedEventID());
                                }

                                // remove this listener
                                fb.child("Events").removeEventListener(this);


                                //NotificationHandler nh = new NotificationHandler();
                                //nh.generateNotifications(getActivity());
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
            //markerOpts.snippet(e.getDescription());
            markerOpts.position(e.getLocation());

            // Figure out icon size
            int size;

            if (e.getPopularity() < Project_18.POP_THRESH1) {
                size = Project_18.MARK_SIZE_1;
            }
            else if (e.getPopularity() < Project_18.POP_THRESH2) {
                size = Project_18.MARK_SIZE_2;
            }
            else {
                size = Project_18.MARK_SIZE_3;
            }

            Bitmap markerBitmap;

            // TODO separate drawable for happening soon and not happening today

            if (e.happeningSoon()) {
                markerBitmap = Project_18.BITMAP_RESIZER(BitmapFactory.decodeResource(getActivity().getResources(),
                                R.drawable.flaticon_marker),
                        size,
                        size);
            }
            else if (!e.happeningLaterToday()) {
                markerBitmap = Project_18.BITMAP_RESIZER(BitmapFactory.decodeResource(getActivity().getResources(),
                                R.drawable.flaticon_marker),
                        size,
                        size);
            }
            else {
                markerBitmap = Project_18.BITMAP_RESIZER(BitmapFactory.decodeResource(getActivity().getResources(),
                                R.drawable.flaticon_marker),
                        size,
                        size);
            }

            markerOpts.icon(BitmapDescriptorFactory.
                    fromBitmap(markerBitmap));

            // Add the marker to the map

            Marker marker = map.addMarker(markerOpts);

            // Put the marker in a HashMap to look up IDs later
            idLookupHM.put(marker.getId(), e.getEvent_id());

            // Put the marker in a HashMap to hide markers later
            markerLookupHM.put(e.getEvent_id(), marker);
        }

        // Methods for ConnectionsCallback
        public void onConnected(@Nullable Bundle bundle) {

        }


        public void onConnectionSuspended(int i) {

        }

        // Method for OnConnectionFailedListener
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
            //CONNECTION FAILED??
            //OH NO
        }
    }
}