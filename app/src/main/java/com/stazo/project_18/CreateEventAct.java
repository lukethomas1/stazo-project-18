package com.stazo.project_18;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Spinner;
import android.widget.TextView;

import com.firebase.client.Firebase;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Brian Chan
 */
public class CreateEventAct extends AppCompatActivity {

    private Event event = new Event();
    private List<String> typeList;
    private final int normColor = Color.BLACK;
    private final int errorColor = Color.RED;

    //The events itself
    AutoCompleteTextView nameView;
    AutoCompleteTextView descView;
    AutoCompleteTextView dateView;
    AutoCompleteTextView startTimeView;
    AutoCompleteTextView endTimeView;
    Spinner typeSpinner;
    TextView nameText, descText, pickText, dateText, startText, endText;

    //User inputted values
    String name;
    String desc;
    String date;
    String startTime;
    String endTime;
    String type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_event);

        // comment out later, needed for testing
        Firebase.setAndroidContext(this);

        nameText = (TextView) findViewById(R.id.NameText);
        descText = (TextView) findViewById(R.id.DescText);
        pickText = (TextView) findViewById(R.id.PickText);
        dateText = (TextView) findViewById(R.id.DateText);
        startText = (TextView) findViewById(R.id.StartText);
        endText = (TextView) findViewById(R.id.EndText);

        nameText.setTextColor(normColor);
        descText.setTextColor(normColor);
        pickText.setTextColor(normColor);
        dateText.setTextColor(normColor);
        startText.setTextColor(normColor);
        endText.setTextColor(normColor);

        //Sets up the Spinner for selecting an Event Type
        typeSpinner = (Spinner) findViewById(R.id.EventType);
        //Add values here to populate the spinner
        typeList = new ArrayList<>();
        typeList.add("Change Me!");
        typeList.add("Party");
        typeList.add("Sports");
        typeList.add("Food");
        typeList.add("Fundraiser");
        typeList.add("Other");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, typeList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(adapter);

        //Actions for spinner selection
        typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView,
                                       int position, long id) {
                pickText.setTextColor(normColor);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {}
        });
    }

    public void makeEvent(View view) {
        boolean valid = true; //If the event is valid

        setUpInput();

        if (checkInput()) {
            //Eliminate non digits
            startTime = startTime.replaceAll("[^\\d.]", "");
            endTime = endTime.replaceAll("[^\\d.]", "");
            date = date.replaceAll("[^\\d.]", "");

            int startTimeInt = Integer.parseInt(startTime);
            int endTimeInt = Integer.parseInt(endTime);
            int dateInt = Integer.parseInt(date);

            event = new Event(name, desc, "creator id", 0, dateInt, startTimeInt, endTimeInt);

            // Location is cafe v by default, will add location selection later
            event.setLocation(new LatLng(32.886030, -117.242590));

            // Push the event to firebase
            event.pushToFirebase(((Project_18) getApplication()).getFB());

            // Return to the Map screen now that we've finished
            goToMapAct();
        }
    }

    // Navigate to the map activity
    private void goToMapAct() {
        startActivity(new Intent(this, MapAct.class));
    }

    // Sets up text fields
    private void setUpInput() {
        //Sets up the views
        nameView = (AutoCompleteTextView) findViewById(R.id.EventName);
        descView = (AutoCompleteTextView) findViewById(R.id.EventDesc);
        dateView = (AutoCompleteTextView) findViewById(R.id.EventDate);
        startTimeView = (AutoCompleteTextView) findViewById(R.id.StartTime);
        endTimeView = (AutoCompleteTextView) findViewById(R.id.EndTime);

        //Grabs user input
        name = nameView.getText().toString();
        desc = descView.getText().toString();
        date = dateView.getText().toString();
        startTime = startTimeView.getText().toString();
        endTime = endTimeView.getText().toString();
        type = typeSpinner.getSelectedItem().toString();
    }

    // Checks if text fields are set properly
    private boolean checkInput() {
        boolean valid = true;

        //Error checking
        if (name.isEmpty()) {
            nameText.setTextColor(errorColor);
            valid = false;
        }
        else {
            nameText.setTextColor(normColor);
        }

        if (desc.isEmpty()) {
            descText.setTextColor(errorColor);
            valid = false;
        }
        else {
            descText.setTextColor(normColor);
        }

        if (date.isEmpty()) {
            dateText.setTextColor(errorColor);
            valid = false;
        }
        else {
            dateText.setTextColor(normColor);
        }

        if (startTime.isEmpty()) {
            startText.setText("Enter a Start Time!");
            startText.setTextColor(errorColor);
            valid = false;
        }
        else {
            startText.setText("Start Time");
            startText.setTextColor(normColor);
        }

        if (endTime.isEmpty()) {
            endText.setText("Enter an End Time!");
            endText.setTextColor(errorColor);
            valid = false;
        }
        else {
            startText.setText("End Time");
            endText.setTextColor(normColor);
        }

        if (type.equals(typeList.get(0))) {
            pickText.setTextColor(errorColor);
            valid = false;
        }

        return valid;
    }
}
