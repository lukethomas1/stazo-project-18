package com.stazo.project_18;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by Ansel on 4/28/16.
 */
public class LocSelectAct extends FragmentActivity
        implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener
{
    private GoogleMap map;
    private Intent callingIntent;
    private Event eventToInit;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_loc_selector);

        callingIntent = getIntent();
        //eventToInit = callingIntent.getSerializableExtra("eventToInit");

        // Initialize the map_overview
        MapFragment mapFrag =
                (MapFragment) getFragmentManager().findFragmentById(R.id.mapLocSelector);

        mapFrag.getMapAsync(this);
    }

    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setOnMapLongClickListener(this);
    }

    // Add a marker where a long click occurs
    public void onMapLongClick(LatLng point) {
            // Add a new marker on the click location
            MarkerOptions markerOpts = new MarkerOptions();

            markerOpts.draggable(true);
            markerOpts.position(point);
    }


}