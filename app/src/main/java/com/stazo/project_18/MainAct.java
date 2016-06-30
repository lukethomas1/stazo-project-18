package com.stazo.project_18;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.AvoidXfermode;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.login.LoginManager;
import com.firebase.client.Firebase;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
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

    private static final int MAP_POS = 0;
    private static final int LIST_POS = 1;
    private static final int PROF_POS = 3;
    private static final int NOT_POS = 2;


    private TabLayout tabLayout;
    public static ViewPager viewPager;
    public static Toolbar toolbar;
    private ViewPagerAdapter adapter;
    private FragmentTransaction transaction;
    private SearchFrag searchFrag;
    private SharedPreferences sharedPreferences;
    public static final String MyPREFERENCES = "MyPrefs" ;
    public static EventInfoFrag eventInfoFrag;
    private static ProfileFrag otherProfileFrag;
    private static ProfileFrag newOtherProfileFrag;
    private static boolean pendingProfile = false;

    private static ArrayList<Fragment> tabFragments = new ArrayList<Fragment>();

    // Search stuff
    private SearchView searchView = null;
    private SearchView.OnQueryTextListener queryTextListener;

    // For notifications
    private boolean notify = false;

    private AppCompatActivity act = this;

    private boolean searched; // did the user just submit a query?

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        //toolbar stuff first because it needs to be on top?
        //or maybe it's done in .xml
        //moved toolbar to separate method
        setToolbar();

        //initialize all to be in filtered
        for (int index = 0; index < Event.types.length; index++) {
            Project_18.filteredCategories.add(new Integer(index));
        }

        setDrawer();

        //tab stuff    http://www.androidhive.info/2015/09/android-material-design-working-with-tabs/

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager); // <- add fragments in setup method

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        tabLayout.getTabAt(MAP_POS).setIcon(R.drawable.ic_actionbar_map2);
        tabLayout.getTabAt(LIST_POS).setIcon(R.drawable.ic_actionbar_browse2);
        tabLayout.getTabAt(PROF_POS).setIcon(R.drawable.ic_actionbar_head);
        tabLayout.getTabAt(NOT_POS).setIcon(R.drawable.ic_actionbar_notif);

        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int pageNumber) {
                hideInfo();

                if (otherProfileFrag != null) {
                    getSupportFragmentManager().beginTransaction().remove(otherProfileFrag).commit();
                    otherProfileStateChange(false);
                }

                for (int i = 0; i <= 3; i++) {
                    if (i == pageNumber) {
                        tabLayout.getTabAt(i).getIcon().setColorFilter(
                                getResources().getColor(R.color.colorPrimary),
                                PorterDuff.Mode.SRC_IN);
                    } else {
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
                getResources().getColor(R.color.colorPrimary),
                PorterDuff.Mode.SRC_IN);
        tabLayout.getTabAt(LIST_POS).getIcon().setColorFilter(
                getResources().getColor(R.color.colorDivider),
                PorterDuff.Mode.SRC_IN);
        tabLayout.getTabAt(PROF_POS).getIcon().setColorFilter(
                getResources().getColor(R.color.colorDivider),
                PorterDuff.Mode.SRC_IN);
        tabLayout.getTabAt(NOT_POS).getIcon().setColorFilter(
                getResources().getColor(R.color.colorDivider),
                PorterDuff.Mode.SRC_IN);

        // Default to "All" categories
        //Project_18.filteredCategories.add(-1);

        // Are we going straight to browse?
        if (getIntent().hasExtra("toBrowse")) {
            viewPager.setCurrentItem(LIST_POS);
        }

        if (getIntent().hasExtra("toProfile")) {
            viewPager.setCurrentItem(PROF_POS);
        }
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
                        if (otherProfileFrag != null) {
                            getSupportFragmentManager().beginTransaction().remove(otherProfileFrag).commit();
                            otherProfileStateChange(false);
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

                    /*((Project_18) getApplication()).setRelevantText(newText);
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
//        profileFrag.setUser_ID(((Project_18)this.getApplication()).getMe().getID());
//        FragmentTransaction transaction = this.getSupportFragmentManager().beginTransaction();
//        transaction.add(R.id.show_viewProfile, profileFrag).addToBackStack("CreateEventFrag").commit();
//    }

    public void goToOtherProfile(String userId) {
        if (searchFrag != null) {
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
        transaction.add(R.id.show_otherProfile, newOtherProfileFrag).addToBackStack("ProfileFrag").commit();
    }

    public void updateOtherProfileFrag() {
        otherProfileStateChange(true);
        if (otherProfileFrag != null) {
            getSupportFragmentManager().beginTransaction().remove(otherProfileFrag).commit();
        }
        otherProfileFrag = newOtherProfileFrag;
        pendingProfile = false;
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
        input.setText(Project_18.me.getBio());

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
                        Project_18.me.setBio(((Project_18)
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


    public void goToEventInfo(String event_id) {
        if (searchFrag != null) {
            getSupportFragmentManager().beginTransaction().remove(searchFrag).commit();
        }
        if (eventInfoFrag != null) {
            getSupportFragmentManager().beginTransaction().remove(eventInfoFrag).commit();
        }

        if (otherProfileFrag != null) {
            otherProfileStateChange(false);
            //getSupportFragmentManager().beginTransaction().remove(otherProfileFrag).commit();
            getSupportFragmentManager().beginTransaction().hide(otherProfileFrag).commit();
        }

        simulateClick(event_id);

        searchView.setIconified(true);

        searchView.clearFocus();

        eventInfoFrag = new EventInfoFrag();
        eventInfoFrag.setEventID(event_id);
        this.getSupportFragmentManager().beginTransaction().
                add(R.id.show_eventInfo, eventInfoFrag).addToBackStack("EventInfoFrag").commit();
        //transaction.add(eventInfoFrag, "EventInfoFrag").commit();

    }
    public void simulateClick(String event_id) {
        ((MapFrag) tabFragments.get(MAP_POS)).simulateOnClick(event_id);
    }

    private void setupViewPager(ViewPager viewPager) {
        adapter = new ViewPagerAdapter(getSupportFragmentManager());

        //-----> REPLACE FRAGMENTS HERE <---------------
        MapFrag mapFrag = new MapFrag();
        ListAct listAct = new ListAct();
        ProfileFrag profileFrag= new ProfileFrag();
        NotificationFrag notFrag = new NotificationFrag();

        // save references to fragments
        tabFragments.add(mapFrag);
        tabFragments.add(listAct);
        tabFragments.add(profileFrag);
        tabFragments.add(notFrag);

        //preemptive set user_id and isMe
        profileFrag.setInfo(Project_18.me.getID(), true);

        adapter.addFragment(mapFrag, "");       // MAP_POS = 0
        adapter.addFragment(listAct, "");       // LIST_POS = 1
        adapter.addFragment(notFrag, "");       // NOT_POS = 2
        adapter.addFragment(profileFrag, "");   // PROF_POS = 3

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
        builder.setMessage("Are you sure? Like really sure?");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                Log.i("MainAct", "Logout Confirmed: Logging Out");

                clearSharedPreferences();
                fbLogout();
                goToInitialAct();

                dialog.dismiss();
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.i("MainAct", "Logout Cancelled");

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
        Log.i("MainAct", "Accesstoken should be something: " + AccessToken.getCurrentAccessToken());
        if (AccessToken.getCurrentAccessToken() != null) {
            LoginManager.getInstance().logOut();
            Log.i("MainAct", "Accesstoken should be null: " + AccessToken.getCurrentAccessToken());
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
        Intent facebookIntent = getOpenFacebookIntent(this, Project_18.me.getID());
        startActivity(facebookIntent);
    }

    @Override
    public void onBackPressed() {
        otherProfileStateChange(false);
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
        Firebase.setAndroidContext(this);
    }

    public void followUser(View v) {
        otherProfileFrag.followUser();
    }
}
