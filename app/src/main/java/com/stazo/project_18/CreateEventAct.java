package com.stazo.project_18;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.support.v4.app.DialogFragment;
import com.firebase.client.Firebase;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Brian Chan
 */
public class CreateEventAct extends AppCompatActivity {
    private Event event = new Event(); //Create a new event object
    private List<String> typeList; //Array of the types of events
    int normColor = Color.BLACK; //Color of default text
    int errorColor = Color.RED; //Color of error text

    //The Text labeling the EditTexts
    TextView nameText, descText, pickText, startDateText, endDateText, startText, endText;
    //The EditText views for each input
    EditText nameView, descView, startDateView, endDateView, startTimeView, endTimeView;
    // The Spinner to pick what type the event is
    Spinner typeSpinner;
    //User inputted values, upload these to Firebase
    String name, desc, startDate, endDate, startTime, endTime, type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_event);

        // comment out later, needed for testing
        Firebase.setAndroidContext(this);

        setUpTextColors();

        grabEditTextViews();

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

        startDateView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(startDateView, startDate);
            }
        });

        endDateView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(endDateView, endDate);
            }
        });

        startTimeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePickerDialog(startTimeView, startTime);
            }
        });

        endTimeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePickerDialog(endTimeView, endTime);
            }
        });
    }

    /**
     * Changes all of the TextViews to the color black.
     */
    private void setUpTextColors() {
        nameText = (TextView) findViewById(R.id.NameText);
        descText = (TextView) findViewById(R.id.DescText);
        pickText = (TextView) findViewById(R.id.PickText);
        startDateText = (TextView) findViewById(R.id.StartDateText);
        endDateText = (TextView) findViewById(R.id.EndDateText);
        startText = (TextView) findViewById(R.id.StartText);
        endText = (TextView) findViewById(R.id.EndText);

        nameText.setTextColor(normColor);
        descText.setTextColor(normColor);
        pickText.setTextColor(normColor);
        startDateText.setTextColor(normColor);
        endDateText.setTextColor(normColor);
        startText.setTextColor(normColor);
        endText.setTextColor(normColor);
    }

    /**
     * Grabs all of the EditText Views for future use.
     */
    private void grabEditTextViews() {
        nameView = (EditText) findViewById(R.id.EventName);
        descView = (EditText) findViewById(R.id.EventDesc);
        startDateView = (EditText) findViewById(R.id.StartDate);
        endDateView = (EditText) findViewById(R.id.EndDate);
        startTimeView = (EditText) findViewById(R.id.StartTime);
        endTimeView = (EditText) findViewById(R.id.EndTime);
    }

    /**
     * Shows a DatePicker fragment and sets up what happens when user enters a date.
     * @param text The EditText view that we want to change when a date is inputted
     * @param date The date string to store the date the user inputted
     */
    public void showDatePickerDialog(EditText text, String date) {
        DatePickerFragment newFragment = new DatePickerFragment(text, date);
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    /**
     * Shows a TimePicker fragment and sets up what happens after the user enters a time
     * @param text The EditText we want to change to whatever time the user inputted
     * @param time The time string to store the time the user inputted
     */
    public void showTimePickerDialog(EditText text, String time) {
        DialogFragment newFragment = new TimePickerFragment(text, time);
        newFragment.show(getSupportFragmentManager(), "timePicker");
    }

    /**
     * Parses the user input.
     */
    private void setUpInput() {
        //Grabs user input
        name = nameView.getText().toString();
        desc = descView.getText().toString();
        type = typeSpinner.getSelectedItem().toString();
    }

    /**
     * Checks if the user entered valid input.
     * @return True/False depending on if we have valid input
     */
    private boolean checkInput() {
        boolean valid = true;

        //Error checking
        if (name.isEmpty()) {
            nameView.setError("This field cannot be blank");
            valid = false;
        }

        if (desc.isEmpty()) {
            descView.setError("This field cannot be blank");
            valid = false;
        }

        if (type.equals(typeList.get(0))) {
            pickText.setTextColor(errorColor);
            valid = false;
        }

        if (valid) {
            return valid;
        }

        return valid;
    }

    /* public void makeEvent(View view) {
        boolean valid = true; //If the event is valid

        setUpInput();

        if (checkInput()) {
            //Eliminate non digits

            event = new Event(name, desc, ((Project_18) getApplication()).getMe().getID(),
                    0, dateInt, startTimeInt, endTimeInt);

            // Location is cafe v by default, will add location selection later
            event.setLocation(new LatLng(32.886030, -117.242590));

            // Push the event to firebase
            //event.pushToFirebase(((Project_18) getApplication()).getFB());

            // Return to the Map screen now that we've finished
            goToLocSelectAct();
        }
    } */

    public void makeEvent(View view) {
        setUpInput();
        checkInput();
    }

    /**
     * Navigate to the map activity.
     */
    private void goToLocSelectAct() {
        startActivity(new Intent(this, LocSelectAct.class).putExtra("eventToInit", event));
    }
}
