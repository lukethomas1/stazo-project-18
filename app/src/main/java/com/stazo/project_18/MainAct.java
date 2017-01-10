package com.stazo.project_18;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.view.WindowManager;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.login.LoginManager;
import com.firebase.client.Firebase;

import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.firebase.client.GenericTypeIndicator;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by ericzhang on 5/5/16.
 */

/**
 * Main activity with the tab switching through ViewPager.
 * Add fragments in the setupViewPager method and follow the TestFrag1 and 2 for example
 * Fragments are different than FragmentActivities btw! Must use onCreateView and return a view for
 * it to be displayed (as opposed to onCreate and setContentView) as it lives inside the main activity
 */
public class MainAct extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{

    public static final int MAP_POS = 0;
    public static final int LIST_POS = 1;
    public static final int NOT_POS = 2;


    private TabLayout tabLayout;
    public static ViewPager viewPager;
    public static Toolbar toolbar;
    private ViewPagerAdapter adapter;
    private FragmentTransaction transaction;
    private SearchFrag searchFrag;
    private SharedPreferences sharedPreferences;
    public static final String MyPREFERENCES = "MyPrefs" ;
    public static EventInfoFrag eventInfoFrag;

    private static ArrayList<Fragment> tabFragments = new ArrayList<Fragment>();

    // Search stuff
    private SearchView searchView = null;
    private SearchView.OnQueryTextListener queryTextListener;

    // For notifications
    private boolean notify = false;

    private AppCompatActivity act = this;

    private boolean searched; // did the user just submit a query?
    private boolean notifTabHighlight = false; // should the notifTab be highlighted? (new notif)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        //toolbar stuff first because it needs to be on top?
        //or maybe it's done in .xml
        //moved toolbar to separate method
        setToolbar();

        setStatusBarColor();

        //initialize all to be in filtered
        for (int index = 0; index < Event.types.length; index++) {
            Converg.filteredCategories.add(new Integer(index));
        }

        setDrawer();

        //tab stuff    http://www.androidhive.info/2015/09/android-material-design-working-with-tabs/

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager); // <- add fragments in setup method

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        tabLayout.getTabAt(MAP_POS).setIcon(R.drawable.ic_actionbar_map2);
        tabLayout.getTabAt(LIST_POS).setIcon(R.drawable.ic_actionbar_browse2);
        tabLayout.getTabAt(NOT_POS).setIcon(R.drawable.ic_actionbar_notif);

        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int pageNumber) {
                hideInfo();

                for (int i = 0; i <= 2; i++) {
                    if (i == pageNumber) {
                        if (i == NOT_POS && notifTabHighlight) {
                            notifTabHighlight = false;
                        }
                        tabLayout.getTabAt(i).getIcon().setColorFilter(
                                getResources().getColor(R.color.colorPrimaryLight),
                                PorterDuff.Mode.SRC_IN);
                    } else {
                        if (i == NOT_POS && notifTabHighlight) {
                            continue;
                        }
                        tabLayout.getTabAt(i).getIcon().setColorFilter(
                                getResources().getColor(R.color.colorDivider),
                                PorterDuff.Mode.SRC_IN);
                    }
                }
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {

            }

            @Override
            public void onPageScrollStateChanged(int arg0) {

            }
        });

        tabLayout.getTabAt(MAP_POS).getIcon().setColorFilter(
                getResources().getColor(R.color.colorPrimaryLight),
                PorterDuff.Mode.SRC_IN);
        tabLayout.getTabAt(LIST_POS).getIcon().setColorFilter(
                getResources().getColor(R.color.colorDivider),
                PorterDuff.Mode.SRC_IN);
        tabLayout.getTabAt(NOT_POS).getIcon().setColorFilter(
                getResources().getColor(R.color.colorDivider),
                PorterDuff.Mode.SRC_IN);

        // Default to "All" categories
        //Converg.filteredCategories.add(-1);

        // Are we going straight to browse?
        if (getIntent().hasExtra("toBrowse")) {
            viewPager.setCurrentItem(LIST_POS);
        }
        if (getIntent().hasExtra("toEvent")) {
            simulateClick(getIntent().getStringExtra("toEvent"));
        }

        // Send out NotificationEventToday
        addEventTodayNotifications();

        // Check for unviewed notifications, change notification icon if so
        checkAllNotificationsViewed();
    }

    private void addEventTodayNotifications() {
        for (String eventID: ((Converg) getApplication()).getMe().getAttendingEvents()) {
            ((Converg) getApplication()).getFB().child("Events").
                    child(eventID).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot eventSnapshot) {
                    Event e = new Event(eventSnapshot.getValue(
                            new GenericTypeIndicator<HashMap<String, Object>>() {
                            }));
                    long difference = e.getStartTime() - System.currentTimeMillis();
                    if (difference > 0 &&
                            difference < 10 * 60 * 60 * 1000) {

                        ArrayList<String> notList = new ArrayList<>();
                        notList.add(((Converg) getApplication()).getMe().getID());

                        (new NotificationEventToday(Notification2.TYPE_EVENT_TODAY, e.getTimeString(true),
                                e.getEvent_id(), e.getName(), "0")).
                                pushToFirebase(((Converg) getApplication()).getFB(),
                                        notList);
                    }
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {
                }
            });
        }
    }

    // Pull all notifications for user and see if there are ones that havent been viewed, changed
    // icon of tab if so
    private void checkAllNotificationsViewed() {
        // Check if there are unviewed notifications and change icon if so
        final ArrayList<Notification2> notifs = new ArrayList<>();
        User currentUser = ((Converg) getApplication()).getMe();

        Converg.getFB().child("NotifDatabase").
                child(currentUser.getID()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean allViewed = true;
                for (DataSnapshot notifSnap : dataSnapshot.getChildren()) {
                    HashMap<String, Object> notifMap = (HashMap<String, Object>) notifSnap.getValue();

                    if(!((boolean)notifMap.get("viewed"))) {
                        allViewed = false;
                        break;
                    }
                }

                if(!allViewed) {
                    notifTabHighlight = true;
                    tabLayout.getTabAt(NOT_POS).getIcon().setColorFilter(
                            getResources().getColor(R.color.colorAccentDark),
                            PorterDuff.Mode.SRC_IN);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    public void hideInfo() {
        if (eventInfoFrag != null) {
            eventInfoFrag.hideEventInfo();
        }
    }

    private void setToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //getSupportActionBar().setNavigationIcon(R.mipmap.ic_launcher);
        getSupportActionBar().setTitle(getString(R.string.app_name));
        //getSupportActionBar().setSubtitle("By: Stazo");
        //getSupportActionBar().setLogo(R.mipmap.ic_launcher);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setDisplayShowTitleEnabled(false);

        //menu button actions
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                // FOR MENU ITEMS NOT IN THE 3 DOTS
                switch (item.getItemId()) {

                }
                return true;
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



    // Menu icons are inflated just as they were with actionbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);

        // Search stuff
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchManager searchManager = (SearchManager) this.getSystemService(Context.SEARCH_SERVICE);
        if (searchItem != null) {
            searchView = (SearchView) searchItem.getActionView();
        }

        if (searchView != null) {
            searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {

                    if (hasFocus) {
                        if (searchFrag != null) {
                            getSupportFragmentManager().beginTransaction().remove(searchFrag).commit();
                        }
                        if (eventInfoFrag != null) {
                            getSupportFragmentManager().beginTransaction().remove(eventInfoFrag).commit();
                        }
                        searched = false;
                        searchFrag = new SearchFrag();
                        transaction =
                                act.getSupportFragmentManager().beginTransaction();
                        transaction.add(R.id.show_searchFrag, searchFrag).addToBackStack("SearchFrag").commit();
                    }

                    else {
                        //searchFrag.getActivity().onBackPressed();

                        // if we didn't just search, then close the fragment
                        if (!searched) {
                            getSupportFragmentManager().beginTransaction().remove(searchFrag).commit();
                        }
                        //searchView.setQuery("", true);
                        //searchView.clearFocus();
                    }
                }
            });

            searchView.setSearchableInfo(searchManager.getSearchableInfo(this.getComponentName()));
            queryTextListener = new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextChange(String newText) {

                    /*((Converg) getApplication()).setRelevantText(newText);
                    // filter MapFrag
                    ((MapFrag) adapter.getItem(0)).filterRelevantEvents();
                    ((ListAct) adapter.getItem(1)).displayFilteredEventList();*/
                    searchFrag.updateResults(newText);
                    return true;
                }

                @Override
                public boolean onQueryTextSubmit(String query) {
                    // hide keyboard
                    searched = true;
                    searchView.clearFocus();
                    return true;
                }
            };

            searchView.setOnQueryTextListener(queryTextListener);
        }
        super.onCreateOptionsMenu(menu);
        return true;
    }

    private void setDrawer() {
        //DrawerLayout drawer = (DrawerLayout) findViewById(R.id.main);
        /*ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();*/

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        //DrawerLayout drawer = (DrawerLayout) findViewById(R.id.main);
        /*LinearLayout drawer = (LinearLayout) findViewById(R.id.main);
        drawer.closeDrawer(GravityCompat.START);*/
        return true;
    }

    public void toggleState(View v) {
        if (eventInfoFrag != null)
            eventInfoFrag.toggleState(v);
    }

    public void goToCreateEvent(View view) {
        startActivity(new Intent(this, CreateEventAct.class));
        //goToProfile(view);
//        CreateEventFrag createEventFrag = new CreateEventFrag();
//        android.support.v4.app.FragmentTransaction transaction =
//                this.getSupportFragmentManager().beginTransaction();
//        transaction.add(R.id.show_createEvent, createEventFrag).addToBackStack("CreateEventFrag").commit();
    }

    //shouldn't be needed right now
//    public void goToProfile(View view) {
//        ProfileFrag profileFrag = new ProfileFrag();
//        profileFrag.setUser_ID(((Converg)this.getApplication()).getMe().getID());
//        FragmentTransaction transaction = this.getSupportFragmentManager().beginTransaction();
//        transaction.add(R.id.show_viewProfile, profileFrag).addToBackStack("CreateEventFrag").commit();
//    }

    public void goToOtherProfile(String userId) {
        /*if (searchFrag != null) {
            getSupportFragmentManager().beginTransaction().remove(searchFrag).commit();
        }
        if (eventInfoFrag != null) {
            //getSupportFragmentManager().beginTransaction().remove(eventInfoFrag).commit();
            eventInfoFrag.collapse();
        }
        if (pendingProfile) {
            return;
        }
        pendingProfile = true;
        searchView.setIconified(true);
        searchView.clearFocus();
        newOtherProfileFrag = new ProfileFrag();
        newOtherProfileFrag.setInfo(userId, false);
        FragmentTransaction transaction =
                this.getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.show_otherProfile, newOtherProfileFrag).addToBackStack("ProfileFrag").commit();*/
    }

    public void updateOtherProfileFrag() {
        /*otherProfileStateChange(true);
        if (otherProfileFrag != null) {
            getSupportFragmentManager().beginTransaction().remove(otherProfileFrag).commit();
        }
        otherProfileFrag = newOtherProfileFrag;
        pendingProfile = false;*/
    }

    public void goToAddTrails(View v) {
        goToAddTrails();
    }

    public void editBio(View view) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        //alertDialog.setTitle("New Bio (100 char limit)");
        alertDialog.setMessage("New bio (100 char):");

        final LinearLayout container = new LinearLayout(this);
        container.setBackground(getResources().getDrawable(R.drawable.border_welcome_desc));

        LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        container.setLayoutParams(lp2);
        container.setGravity(Gravity.CENTER);

        final EditText input = new EditText(this);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 20, 0, 0);
        input.setLayoutParams(lp);
        input.setGravity(Gravity.CENTER);
        input.setText(Converg.me.getBio());

        // length
        int maxLength = 100;
        InputFilter[] fArray = new InputFilter[1];
        fArray[0] = new InputFilter.LengthFilter(maxLength);
        input.setFilters(fArray);

        input.setSelection(input.getText().length());
        input.setBackground(null);
        //input.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);

        //input.setBackground(getResources().getDrawable(R.drawable.border_welcome_desc));
        container.addView(input);
        alertDialog.setView(container);

        alertDialog.setPositiveButton("Change Bio",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Converg.me.setBio(((Converg)
                                getApplication()).getFB(), input.getText().toString());

                        // reload profile
                        Intent i = new Intent(act, MainAct.class);
                        i.putExtra("toProfile", true);
                        startActivity(i);
                        finish();
                    }
                });

        alertDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        alertDialog.show();
    }

    public void goToAddTrails() {
        startActivity(new Intent(this, AddTrailsAct.class));
    }
    public void goToInviteUsers(View v) {
        InviteUsersAct.event = EventInfoFrag.currEvent;
        startActivity(new Intent(this, InviteUsersAct.class));
    }

    public void goToEventInfo(String event_id, boolean autoOpen) {
        if (searchFrag != null) {
            getSupportFragmentManager().beginTransaction().remove(searchFrag).commit();
        }
        if (eventInfoFrag != null) {
            getSupportFragmentManager().beginTransaction().remove(eventInfoFrag).commit();
        }

        simulateClick(event_id);

        searchView.setIconified(true);

        searchView.clearFocus();

        eventInfoFrag = new EventInfoFrag();
        eventInfoFrag.setEventID(event_id);
        eventInfoFrag.setAutoOpen(autoOpen);
        this.getSupportFragmentManager().beginTransaction().
                add(R.id.show_eventInfo, eventInfoFrag).addToBackStack("EventInfoFrag").commit();
        //transaction.add(eventInfoFrag, "EventInfoFrag").commit();

    }

    public void deleteEvent(View view) {
        eventInfoFrag.deleteEvent();
    }

    public void flagEvent(View view) {
        eventInfoFrag.flagEvent();
    }


    public void simulateClick(String event_id) {
        ((MapFrag) tabFragments.get(MAP_POS)).simulateOnClick(event_id);
    }

    private void setupViewPager(ViewPager viewPager) {
        adapter = new ViewPagerAdapter(getSupportFragmentManager());

        //-----> REPLACE FRAGMENTS HERE <---------------
        MapFrag mapFrag = new MapFrag();
        ListAct listAct = new ListAct();
        NotificationFrag notFrag = new NotificationFrag();

        // save references to fragments
        tabFragments.add(mapFrag);
        tabFragments.add(listAct);
        tabFragments.add(notFrag);

        adapter.addFragment(mapFrag, "");       // MAP_POS = 0
        adapter.addFragment(listAct, "");       // LIST_POS = 1
        adapter.addFragment(notFrag, "");       // NOT_POS = 2

        viewPager.setAdapter(adapter);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    public void logoutUser(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Log Out");
        builder.setMessage("Are you sure you want to log out?");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {

                clearSharedPreferences();
                fbLogout();
                goToInitialAct();

                dialog.dismiss();
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                // Do nothing
                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    private void clearSharedPreferences() {
        sharedPreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("userId", "0");
        editor.putBoolean("isLoggedIn", false);
        editor.apply();
    }

    private void fbLogout() {
        if (AccessToken.getCurrentAccessToken() != null) {
            LoginManager.getInstance().logOut();
            Toast.makeText(getApplicationContext(), "Logged out", Toast.LENGTH_SHORT).show();
        }
    }

    private void goToInitialAct() {
        startActivity(new Intent(this, InitialAct.class));
    }
    public void otherProfileStateChange(boolean entering) {
        if (entering) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
        }
        else {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setTitle(getString(R.string.app_name));
        }
    }

    public static Intent getOpenFacebookIntent(Context context, String userId) {

        try {
            context.getPackageManager().getPackageInfo("com.facebook.katana", 0);

            String facebookScheme = "fb://page/" + userId;

            return new Intent(Intent.ACTION_VIEW, Uri.parse(facebookScheme));
        } catch (Exception e) {
            // Cache and Open a url in browser
            String facebookProfileUri = "https://www.facebook.com/jasonbao";
            return new Intent(Intent.ACTION_VIEW, Uri.parse(facebookProfileUri));
        }
    }

    public void goToFacebookProfile(View v) {
        Intent facebookIntent = getOpenFacebookIntent(this, Converg.me.getID());
        startActivity(facebookIntent);
    }

    @TargetApi(21)
    private void setStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window window = this.getWindow();

            // clear FLAG_TRANSLUCENT_STATUS flag:
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

            // finally change the color
            window.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }
    }

    @Override
    public void onBackPressed() {
        otherProfileStateChange(false);
        if (searchFrag != null) {
            searchView.setIconified(true);
            searchView.clearFocus();
        }

        System.out.println("test");
        if (getSupportFragmentManager().findFragmentByTag("EventInfoFrag") != null) {
            getSupportFragmentManager().popBackStackImmediate();
            System.out.println("dank");
        }
        else if (getSupportFragmentManager().findFragmentByTag("CreateEventFrag") != null) {
            getSupportFragmentManager().popBackStackImmediate();
            System.out.println("dank");
        }
        else {
            super.onBackPressed();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Converg.me == null) {
            startActivity(new Intent(this, InitialAct.class));
            finish();
        } else {
            Firebase.setAndroidContext(this);
        }
    }

    public void followUser(View v) {
        //otherProfileFrag.followUser();
    }
}
