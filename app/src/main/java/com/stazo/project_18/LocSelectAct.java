package com.stazo.project_18;

import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;

/**
 * Created by Ansel on 4/28/16.
 */
public class LocSelectAct extends FragmentActivity
        implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener,
        OnConnectionFailedListener
{
    private GoogleMap map;
    private PlaceAutocompleteFragment autocompleteFragment;
    private Event eventToInit;
    private Marker eventMarker;
    private GoogleApiClient mGoogleApiClient;

    // Initial Camera Position
    private float zoom = 15;
    private float tilt = 0;
    private float bearing = 0;


    private Uri imageUri;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_loc_selector);

        // The intent that led to this activity
        Intent callingIntent = getIntent();

        // Get the event to initialize
        eventToInit = (Event) callingIntent.getParcelableExtra("eventToInit");
        String imageString = callingIntent.getStringExtra("mainImageUri");
        if (imageString != null) {
            imageUri = Uri.parse(imageString);
        }
        System.out.println("Start time: " + eventToInit.getStartTime());
        System.out.println("End time: " + eventToInit.getEndTime());

        // Initialize the map_overview
        MapFragment mapFrag =
                (MapFragment) getFragmentManager().findFragmentById(R.id.mapLocSelector);

        mapFrag.getMapAsync(this);

        // init API client
        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();

        //directions
        Toast dir = Toast.makeText(getApplicationContext(),
                "Tap and hold to choose the event's location", Toast.LENGTH_LONG);
        //this centers the text in the toast
        TextView v = (TextView) dir.getView().findViewById(android.R.id.message);
        if( v != null) v.setGravity(Gravity.CENTER);
        dir.show();

        //private method defined below
        setUpSearchFragment();
    }

    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setOnMapLongClickListener(this);


        CameraPosition camPos = new CameraPosition(MapFrag.REVELLE, zoom, tilt, bearing);

        map.moveCamera(CameraUpdateFactory.newCameraPosition(camPos));
        //zoom in and out
        map.getUiSettings().setZoomControlsEnabled(true);
    }

    // Add a marker where a long click occurs
    public void onMapLongClick(LatLng point) {
        // Set the marker's location
        MarkerOptions markerOpts = new MarkerOptions();
        markerOpts.position(point);
        markerOpts.draggable(true);
        // Set the color of the marker
        markerOpts.icon(
                //BitmapDescriptorFactory.defaultMarker(Event.typeColors[eventToInit.getType()]));
                BitmapDescriptorFactory.fromResource(R.drawable.marker_light_blue_3x));
        // Remove the previous marker if there is one on the map
        if (eventMarker != null) {
            eventMarker.remove();
        }

        // Add marker to map
        eventMarker = map.addMarker(markerOpts);

        // Intitialize the event with the Lat/Lng of the event
        eventToInit.setLocation(point);

        // Initialize the event's id
        //eventToInit.setEvent_id(eventMarker.getId());
        //eventToInit.generateID();
    }

    public void goToMap(View view) {
        // Check if user placed a marker for event location
        if (eventToInit.getLocation() == null) {
            Context context = getApplicationContext();

            // Error message
            CharSequence text = "Please select a location";

            // How long to display the toast
            int duration = Toast.LENGTH_SHORT;

            // display the toast
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }

        else {
//            System.out.println("Start: " + eventToInit.getStartDate().getTime());
//            System.out.println("End: " + eventToInit.getEndDate().getTime());
            System.out.println("startTimeToBePushed: " + eventToInit.getStartTime());
            System.out.println("endTimeToBePushed: " + eventToInit.getEndTime());

            // Push the event to the database
            eventToInit.pushToFirebase(((Project_18) getApplication()).getFB());

            //push the image to firebasestorage
            if (imageUri != null) {
                pushMainImage(imageUri);
            }

            // Go to the map screen
            Intent intent = new Intent(this, MainAct.class);
            startActivity(intent);
        }
    }

    public void pushMainImage(Uri imageFile) {

        StorageReference storageRef = ((Project_18) getApplication()).getFBStorage();

        //replace with eventid instead of file name later
        StorageReference mainImageStorage = storageRef.child("MainImagesDatabase/" + eventToInit.getEvent_id() + ".jpg");
        UploadTask uploadTask = mainImageStorage.putFile(imageFile);

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                System.out.println("your upload failed");
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                System.out.println("your upload succeeded");
                System.out.println("download url is: " + taskSnapshot.getDownloadUrl());
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        Firebase.setAndroidContext(this);
    }

    private void setUpSearchFragment(){
        autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {

                //setting location
                LatLng point = place.getLatLng();
                // Set the marker's location
                MarkerOptions markerOpts = new MarkerOptions();
                markerOpts.position(point);
                markerOpts.draggable(true);
                // Set the color of the marker
                markerOpts.icon(
                        //BitmapDescriptorFactory.defaultMarker(Event.typeColors[eventToInit.getType()]));
                        BitmapDescriptorFactory.fromResource(R.drawable.marker_light_blue_3x));
                // Remove the previous marker if there is one on the map
                if (eventMarker != null) {
                    eventMarker.remove();
                }

                // Add marker to map
                eventMarker = map.addMarker(markerOpts);
                
                //moving camera to default
                CameraPosition newPos = new CameraPosition(point, zoom, tilt, bearing);
                map.moveCamera(CameraUpdateFactory.newCameraPosition(newPos));
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                //Log.i(TAG, "An error occurred: " + status);
            }
        });

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        //CONNECTION FAILED??
        //OH NO
    }
}