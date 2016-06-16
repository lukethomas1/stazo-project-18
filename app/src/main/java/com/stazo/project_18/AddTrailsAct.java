package com.stazo.project_18;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;
import java.util.HashMap;

public class AddTrailsAct extends AppCompatActivity {

    private SearchView searchView = null;
    private SearchView.OnQueryTextListener queryTextListener;
    private HashMap<String, String> relevantUsers = new HashMap<String, String>();
    private HashMap<String, View> buttonMap = new HashMap<String, View>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_trails);

        // set relevantUsers
        relevantUsers = ((Project_18) getApplication()).getMe().getFriends();
        generateButtons();

        // search view things
        SearchManager searchManager = (SearchManager) this.getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) findViewById(R.id.searchView);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(this.getComponentName()));
        queryTextListener = new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
                updateUserSection(newText);
                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {

                // hide keyboard
                searchView.clearFocus();
                return true;
            }
        };
        searchView.setOnQueryTextListener(queryTextListener);
    }

    public void addCategoryTrail(View v) {
        Integer trailNum = Integer.parseInt((String) v.getTag());
        switch (trailNum) {
            case 1:
                ((Project_18) getApplication()).addTrail(trailNum - 1);
            case 2:
                ((Project_18) getApplication()).addTrail(trailNum - 1);
            case 3:
                ((Project_18) getApplication()).addTrail(trailNum - 1);
            case 4:
                ((Project_18) getApplication()).addTrail(trailNum - 1);
            case 5:
                ((Project_18) getApplication()).addTrail(trailNum - 1);
            case 6:
                ((Project_18) getApplication()).addTrail(trailNum - 1);
            case 7:
                ((Project_18) getApplication()).addTrail(trailNum - 1);
        }
    }

    public void goToProfile(View v) {
        Intent i = new Intent(this, Profile.class);
        i.putExtra("userID", ((Project_18) getApplication()).getMe().getID());
        startActivity(i);
    }

    public void updateUserSection(String text){
        LinearLayout usersLayout = (LinearLayout) findViewById(R.id.usersLayout);
        for(String key: relevantUsers.keySet()) {
            if (!(key.toLowerCase().contains(text.toLowerCase()))) {
                // hide users that don't exist if they aren't already removed
                if (buttonMap.get(key).getParent() != null) {
                    usersLayout.removeView(buttonMap.get(key));
                }
            }
            else {
                // if a user was previously removed, unremove them
                if (buttonMap.get(key).getParent() == null) {
                    usersLayout.addView(buttonMap.get(key));
                }
            }
        }
    }

    private void generateButtons() {
        LinearLayout usersLayout = (LinearLayout) findViewById(R.id.usersLayout);
        for (final String name: relevantUsers.keySet()) {
            Button b = new Button(getApplicationContext());
            b.setText(name);
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // add the trail
                    ((Project_18) getApplication()).addTrail(relevantUsers.get(name));
                }
            });
            buttonMap.put(name, b);
            usersLayout.addView(b);
        }
    }


}
