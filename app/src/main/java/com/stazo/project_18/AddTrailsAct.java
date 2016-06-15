package com.stazo.project_18;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

public class AddTrailsAct extends AppCompatActivity {

    private SearchView searchView = null;
    private SearchView.OnQueryTextListener queryTextListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_trails);

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

    }


}
