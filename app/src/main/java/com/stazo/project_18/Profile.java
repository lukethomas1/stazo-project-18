package com.stazo.project_18;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.GenericTypeIndicator;
import com.firebase.client.ValueEventListener;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class Profile extends AppCompatActivity {

    private Firebase fb;
    private User user;
    private Context context = this;
    private Event currentEvent;
    private Integer currentCategoryTrail;
    private String currentUserTrail;
    private static int eventsTextSize = 12;
    private ArrayList<Event> myEvents =  new ArrayList<Event>();
    private ArrayList<Event> attendingEvents = new ArrayList<Event>();
    private Toolbar toolbar;
    private ArrayList<Integer> categoryTrails = new ArrayList<Integer>();
    private ArrayList<String> userTrails = new ArrayList<String>();
    private Bitmap profPicBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);
        setToolbar();

        // set firebase reference
        fb = ((Project_18) getApplication()).getFB();

        // grab user and fill screen with correct info
        grabInfo();
        //((TextView) findViewById(R.id.nameTextView)).setText("");
        //startActivity(new Intent(this, ListActConcept.class));
    }

    private void setToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //getSupportActionBar().setNavigationIcon(R.mipmap.ic_launcher);
        //getSupportActionBar().setTitle(getString(R.string.app_name));
        //getSupportActionBar().setSubtitle("By: Stazo");
        //getSupportActionBar().setLogo(R.mipmap.ic_launcher);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                switch (item.getItemId()) {

                    case R.id.action_profile:
                        goToProfile();
                        Log.d("myTag", "you hit action profile");
                        return true;
                    default:
                        return true;
                }
            }
        });

        //back button action
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        super.onCreateOptionsMenu(menu);
        return true;
    }

    private void grabInfo() {
        fb.child("Users").child(getIntent().getStringExtra("userID")).
                addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // set user and grab data
                        user = new User((HashMap<String, Object>) dataSnapshot.getValue());
                        ((TextView) findViewById(R.id.nameTextView)).setText(user.getName());

                        // display events
                        grabAndDisplayEvents();

                        // display trails
                        grabAndDisplayTrails();

                        // set profile picture
                        setProfilePicture();

                        // remove event listener
                        fb.child("Users").child(getIntent().getStringExtra("userID")).
                                removeEventListener(this);
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                });
    }

    private void grabAndDisplayEvents() {
        fb.child("Events").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                /* add myEvents */
                for (String event_id : user.getMyEvents()) {
                    myEvents.add(new Event(dataSnapshot.child(event_id).getValue
                            (new GenericTypeIndicator<HashMap<String, Object>>() {
                            })));
                }

                /* add attendingEvents */
                for (String event_id : user.getAttendingEvents()) {
                    attendingEvents.add(new Event(dataSnapshot.child(event_id).getValue
                            (new GenericTypeIndicator<HashMap<String, Object>>() {
                            })));
                }
                /* dynamically add button */
                LinearLayout eventsLayout = (LinearLayout) findViewById(R.id.eventsLayout);
                //myEvents.clear();
                //attendingEvents.clear();

                /* no adventures case */
                if (myEvents.isEmpty() && attendingEvents.isEmpty()) {
                    Button eventButton = new Button(context);
                    eventButton.setText("No adventures, no worries! " +
                            "Just tap to explore.");
                    makePretty(eventButton);
                    eventButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            goToBrowse();
                        }
                    });
                    eventsLayout.addView(eventButton);
                    fb.child("Events").removeEventListener(this);
                    return;
                }

                /* display myEvents */
                for (Event e : myEvents) {
                    currentEvent = e;
                    Button eventButton = new Button(context);
                    eventButton.setText(e.toString() + "Host");
                    makePretty(eventButton);
                    eventButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            goToEventInfo(currentEvent.getEvent_id());
                        }
                    });
                    eventsLayout.addView(eventButton);
                }
                /* display attendingEvents */
                for (Event e : attendingEvents) {
                    currentEvent = e;
                    Button eventButton = new Button(context);
                    eventButton.setText(e.toString() + "Attending");
                    makePretty(eventButton);
                    eventButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            goToEventInfo(currentEvent.getEvent_id());
                        }
                    });
                    eventsLayout.addView(eventButton);
                }
                fb.child("Events").removeEventListener(this);

                // unhide-layout
                (findViewById(R.id.profileLayout)).setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });
    }

    private void makePretty(Button button) {
        RelativeLayout.LayoutParams lp = new
                RelativeLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        button.setTextSize(eventsTextSize);
        //button.setTypeface(Typeface.MONOSPACE);
        button.setAllCaps(false);
        button.setGravity(Gravity.LEFT);
        button.setGravity(Gravity.CENTER_VERTICAL);
        button.setLayoutParams(lp);
        button.setBackgroundColor(getResources().getColor(R.color.white));
        //button.setBackgroundColor(getResources().getColor(R.color.skyBlue));
    }

    private void grabAndDisplayTrails() {
        categoryTrails = user.getCategoryTrails();
        userTrails = user.getUserTrails();
        LinearLayout trailsLayout = (LinearLayout) findViewById(R.id.trailsLayout);

        // draw category trails
        for (Integer type : categoryTrails) {
            currentCategoryTrail = type;
            Button trailButton = new Button(context);
            trailButton.setText(Event.types[type]);
            makePretty(trailButton);
            trailButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    goToBrowse(currentCategoryTrail);
                }
            });
            trailsLayout.addView(trailButton);
        }

        // draw user trails
        for (String userId : userTrails) {
            currentUserTrail = userId;
            final Button trailButton = new Button(context);

            // grab name
            fb.child("Users").child(userId).child("name").
                    addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            trailButton.setText((String) dataSnapshot.getValue());
                            fb.child("Users").child(currentUserTrail).child("name").
                                    removeEventListener(this);
                        }

                        @Override
                        public void onCancelled(FirebaseError firebaseError) {
                        }
                    });
            makePretty(trailButton);
            trailButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    goToBrowse(currentUserTrail);
                }
            });
            trailsLayout.addView(trailButton);
        }
    }

    // pull and set profile picture
    private void setProfilePicture() {
        new Thread(new Runnable() {
            public void run() {

                try {
                    URL imageURL = new URL("https://graph.facebook.com/" + user.getID() + "/picture?type=large");
                    profPicBitmap = Bitmap.createScaledBitmap(
                            BitmapFactory.decodeStream(imageURL.openConnection().getInputStream()),
                            400,
                            400,
                            true);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                final ImageView iv = (ImageView) findViewById(R.id.profilePicture);

                iv.post(new Runnable() {
                    public void run() {
                        iv.setImageBitmap(profPicBitmap);
                    }
                });
            }
        }).start();

        /*ImageView iv = (ImageView) findViewById(R.id.profilePicture);
        //Bitmap bMap = BitmapFactory.decodeResource(getResources(), R.drawable.me_tho_2);
        Bitmap bMapScaled = Bitmap.createScaledBitmap(profPicBitmap, 400, 400, true);
        iv.setImageBitmap(bMapScaled);*/
    }


    private void goToEventInfo(String event_id) {
        // go to detailed event info act
        /*
        EventInfoFrag eventInfoFrag = new EventInfoFrag();
        eventInfoFrag.setEventID(event_id);
        android.support.v4.app.FragmentTransaction transaction =
                this.getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.show_eventInfo, eventInfoFrag).addToBackStack("EventInfoFrag").commit();*/
    }

    public void goToAddTrails(View view) {
        Intent i = new Intent(this, AddTrailsAct.class);
        //i.putExtra("userID", user.getID());
        startActivity(i);
    }

    private void goToBrowse() {
    }

    // goes to Explore with search query as an extra.
    private void goToBrowse(String userTrail) {
        Intent i = new Intent(this, MainAct.class);
        i.putExtra("userTrail", userTrail);
        i.putExtra("toBrowse", true);
        startActivity(i);
        finish();
    }

    // goes to Explore with search query as an extra
    private void goToBrowse(int categoryTrail) {
        Intent i = new Intent(this, MainAct.class);
        i.putExtra("categoryTrail", categoryTrail);
        i.putExtra("toBrowse", true);
        startActivity(i);
        finish();
    }

    public void goToProfile() {

    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, MainAct.class));
        finish();
    }

}
