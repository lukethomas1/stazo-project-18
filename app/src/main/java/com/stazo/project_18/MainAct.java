package com.stazo.project_18;

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
import android.widget.SeekBar;
import android.view.WindowManager;

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

    // Search stuff
    private SearchView searchView = null;
    private SearchView.OnQueryTextListener queryTextListener;


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
                                getResources().getColor(R.color.offwhite),
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
                getResources().getColor(R.color.offwhite),
                PorterDuff.Mode.SRC_IN);
        tabLayout.getTabAt(2).getIcon().setColorFilter(
                getResources().getColor(R.color.offwhite),
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
        //getSupportActionBar().setTitle(getString(R.string.app_name));
        //getSupportActionBar().setSubtitle("By: Stazo");
        //getSupportActionBar().setLogo(R.mipmap.ic_launcher);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        //menu button actions
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                // FOR MENU ITEMS NOT IN THE 3 DOTS
                switch (item.getItemId()) {
                    case R.id.action_profile:
                        goToProfile();
                        Log.d("myTag", "you hit action profile");
                        return true;
                }

                /*// FOR THE 3 DOTS
                int i;

                // Get the index of the type in the Event.types array
                for(i = 0; i < Event.types.length; i++) {
                    // Create Integer object to add to ArrayList
                    Integer category = new Integer(i);

                    // If it is an existing type
                    if (item.getTitle().equals(Event.types[i])) {

                        // Toggle Check
                        item.setChecked(!item.isChecked());

                        // If it is checked, add it
                        if (item.isChecked()) {

                            // If all is currently checked...
                            if (toolbar.getMenu().findItem(R.id.all).isChecked()) {

                                // uncheck all
                                toolbar.getMenu().findItem(R.id.all).setChecked(false);

                                // clear filteredCategories
                                Project_18.filteredCategories.clear();
                            }

                            // Add this category
                            Project_18.filteredCategories.add(category);
                        }

                        // If it isn't checked anymore, remove it
                        else {
                            Project_18.filteredCategories.remove(category);
                        }

                        // We're done checking
                        Log.d("myTag", Project_18.filteredCategories.toString());

                        // Update filtering
                        ((MapFrag) adapter.getItem(0)).filterRelevantEvents();
                        ((ListAct) adapter.getItem(1)).displayFilteredEventList();
                        return true;
                    }
                }

                // If we iterated fully, then all was clicked
                if (i == Event.types.length) {

                    // If all wasn't checked, check all. NOTE: You cannot uncheck all
                    if (!item.isChecked()) {

                        // uncheck all the other categories
                        toolbar.getMenu().findItem(R.id.food).setChecked(false);
                        toolbar.getMenu().findItem(R.id.sports).setChecked(false);
                        toolbar.getMenu().findItem(R.id.performance).setChecked(false);
                        toolbar.getMenu().findItem(R.id.academic).setChecked(false);
                        toolbar.getMenu().findItem(R.id.social).setChecked(false);
                        toolbar.getMenu().findItem(R.id.gaming).setChecked(false);
                        toolbar.getMenu().findItem(R.id.other).setChecked(false);
                        Project_18.filteredCategories.clear();
                        for (int index = 0; index < Event.types.length; index++) {
                            Project_18.filteredCategories.add(new Integer(index));
                        }
                        item.setChecked(true);

                        // Update filtering
                        ((MapFrag) adapter.getItem(0)).filterRelevantEvents();
                        ((ListAct) adapter.getItem(1)).displayFilteredEventList();
                    }
                }

                Log.d("myTag", Project_18.filteredCategories.toString());*/
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
            searchView.setSearchableInfo(searchManager.getSearchableInfo(this.getComponentName()));
            queryTextListener = new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextChange(String newText) {
                    // do search here
                    Log.d("MyTag", newText);
                    Log.d("MyTag", "letsgo");
                    ((Project_18) getApplication()).setRelevantText(newText);
                    // filter MapFrag
                    ((MapFrag) adapter.getItem(0)).filterRelevantEvents();
                    ((ListAct) adapter.getItem(1)).displayFilteredEventList();
                    return true;
                }

                @Override
                public boolean onQueryTextSubmit(String query) {
                    // do search here
                    Log.d("MyTag", query);
                    Log.d("MyTag", "yooo");

                    // hide keyboard
                    searchView.clearFocus();
                    return true;
                }
            };
            searchView.setOnQueryTextListener(queryTextListener);
        }
        super.onCreateOptionsMenu(menu);
        return true;
    }

//    public boolean onOptionsItemSelected(MenuItem item) {

 //   }

    private void setDrawer() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.main);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

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

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.main);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void goToCreateEvent(View view) {
        startActivity(new Intent(this, CreateEventAct.class));
        //goToProfile(view);
//        CreateEventFrag createEventFrag = new CreateEventFrag();
//        android.support.v4.app.FragmentTransaction transaction =
//                this.getSupportFragmentManager().beginTransaction();
//        transaction.add(R.id.show_createEvent, createEventFrag).addToBackStack("CreateEventFrag").commit();
    }

    public void goToProfile(View view) {
        goToProfile();
    }

    public void goToProfile() {
        Intent i =  new Intent(this, Profile.class);
        i.putExtra("userID", ((Project_18)getApplication()).getMe().getID());
        startActivity(i);
    }

    public void goToEventInfo(String event_id) {
//        Intent intent = new Intent(this, EventInfoAct.class);
//        intent.putExtra("event_id", event_id);
//        startActivity(intent);

        // UNCOMMENT IF YOU WANT EVENT INFO TURNED INTO FRAG
        EventInfoFrag eventInfoFrag = new EventInfoFrag();
        eventInfoFrag.setEventID(event_id);
        FragmentTransaction transaction =
                this.getSupportFragmentManager().beginTransaction();
//        transaction.replace
//                (R.id.show_eventInfo, eventInfoFrag, "EventInfoFrag");
        transaction.add(R.id.show_eventInfo, eventInfoFrag).addToBackStack("EventInfoFrag").commit();
//        this.getWindow().setDimAmount((float) 0.8);
//        WindowManager.LayoutParams lp = this.getWindow().getAttributes();
//        lp.dimAmount=0.0f;
//        this.getWindow().setAttributes(lp);
//        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
//        transaction
//                .add(R.id.show_eventInfo, eventInfoFrag)
//                .addToBackStack("EventInfoFrag")
//                .commit();

    }

    private void setupViewPager(ViewPager viewPager) {
        adapter = new ViewPagerAdapter(getSupportFragmentManager());

        //-----> REPLACE FRAGMENTS HERE <---------------
        adapter.addFragment(new MapFrag(), ""); //map
        adapter.addFragment(new ListAct(), ""); //explore
        adapter.addFragment(new TestFrag1(), ""); //profile

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
}
