package com.stazo.project_18;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.AvoidXfermode;
import android.graphics.PorterDuff;
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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.view.WindowManager;

import com.firebase.client.Firebase;

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
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private Toolbar toolbar;
    private ViewPagerAdapter adapter;
    private FragmentTransaction transaction;
    private SearchFrag searchFrag;
    private static EventInfoFrag eventInfoFrag;

    private static ArrayList<Fragment> tabFragments = new ArrayList<Fragment>();

    // Search stuff
    private SearchView searchView = null;
    private SearchView.OnQueryTextListener queryTextListener;

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

        tabLayout.getTabAt(0).setIcon(R.drawable.ic_actionbar_map2);
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_actionbar_browse2);
        tabLayout.getTabAt(2).setIcon(R.drawable.ic_actionbar_head);

        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int pageNumber) {
                for (int i = 0; i <= 2; i++) {
                    if (i == pageNumber) {
                        tabLayout.getTabAt(i).getIcon().setColorFilter(
                                getResources().getColor(R.color.colorPrimary),
                                PorterDuff.Mode.SRC_IN);
                    }
                    else {
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

        tabLayout.getTabAt(0).getIcon().setColorFilter(
                getResources().getColor(R.color.colorPrimary),
                PorterDuff.Mode.SRC_IN);
        tabLayout.getTabAt(1).getIcon().setColorFilter(
                getResources().getColor(R.color.colorDivider),
                PorterDuff.Mode.SRC_IN);
        tabLayout.getTabAt(2).getIcon().setColorFilter(
                getResources().getColor(R.color.colorDivider),
                PorterDuff.Mode.SRC_IN);

        // Default to "All" categories
        //Project_18.filteredCategories.add(-1);

        // Are we going straight to browse?
        if (getIntent().hasExtra("toBrowse")) {
            viewPager.setCurrentItem(1);
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
                    /*case R.id.action_profile:
                        goToAddTrails();
                        Log.d("myTag", "you hit action profile");
                        return true;*/
                    /*case R.id.action_map:
                        SearchFrag searchFrag = new SearchFrag();
                        FragmentTransaction transaction =
                                act.getSupportFragmentManager().beginTransaction();
                        transaction.add(R.id.show_searchFrag, searchFrag).addToBackStack("SearchFrag").commit();*/

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
                        searched = false;
                        searchFrag = new SearchFrag();
                        transaction =
                                act.getSupportFragmentManager().beginTransaction();
                        transaction.add(R.id.show_searchFrag, searchFrag).addToBackStack("SearchFrag").commit();

                        if (eventInfoFrag != null) {
                            getSupportFragmentManager().beginTransaction().remove(eventInfoFrag).commit();
                        }
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

//    public void goToProfile() {
//        Intent i =  new Intent(this, Profile.class);
//        i.putExtra("userID", ((Project_18)getApplication()).getMe().getID());
//        startActivity(i);
//    }

    public void goToAddTrails(View v) {
        goToAddTrails();
    }

    public void goToAddTrails() {
        startActivity(new Intent(this, AddTrailsAct.class));
    }
    public void goToEventInfo(String event_id) {

        if (searchFrag != null) {
            getSupportFragmentManager().beginTransaction().remove(searchFrag).commit();
        }
        if (eventInfoFrag != null) {
            getSupportFragmentManager().beginTransaction().remove(eventInfoFrag).commit();
        }
        searchView.clearFocus();

        eventInfoFrag = new EventInfoFrag();
        eventInfoFrag.setEventID(event_id);
        FragmentTransaction transaction =
                this.getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.show_eventInfo, eventInfoFrag).addToBackStack("EventInfoFrag").commit();
    }
    public void simulateClick(String event_id) {
        ((MapFrag) tabFragments.get(0)).simulateOnClick(event_id);
    }

    private void setupViewPager(ViewPager viewPager) {
        adapter = new ViewPagerAdapter(getSupportFragmentManager());

        //-----> REPLACE FRAGMENTS HERE <---------------
        MapFrag mapFrag = new MapFrag();
        ListAct listAct = new ListAct();
        ProfileFrag profileFrag= new ProfileFrag();

        // save references to fragments
        tabFragments.add(mapFrag);
        tabFragments.add(listAct);
        tabFragments.add(profileFrag);

        //preemptive set user_id and isMe
        profileFrag.setUser_ID(((Project_18) this.getApplication()).getMe().getID());
        profileFrag.setIsMe(true);

        adapter.addFragment(mapFrag, ""); //map
        adapter.addFragment(listAct, ""); //explore
        adapter.addFragment(profileFrag, ""); //profile

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

    @Override
    public void onBackPressed() {
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
}
