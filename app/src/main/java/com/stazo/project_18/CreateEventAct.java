package com.stazo.project_18;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.firebase.client.Firebase;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Author: Brian Chan
 * Date: 4/25/2016
 * Description: This class parses the user input into an Event object to create a new event for
 * stazo-project-18.
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
    //Fragments for setting the dates
    DatePickerFragment startDateFrag, endDateFrag;
    //Fragments for setting the times
    TimePickerFragment startTimeFrag, endTimeFrag;
    //Parsed user inputted values, upload these to Firebase
    String name, desc, startDate, endDate, startTime, endTime, type;

    /**
     * Called whenever this layout is created. This should set up the layout so that it is ready
     * to receive user input and to create a new event.
     * @param savedInstanceState Reference to a Bundle object that this activity can use to restore
     *                           itself in the future if needed.
     */
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
                startDateFrag = new DatePickerFragment(startDateView);
                startDateFrag.show(getSupportFragmentManager(), "datePicker");

                startDateView.setError(null);
            }
        });

        endDateView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                endDateFrag = new DatePickerFragment(endDateView);
                endDateFrag.show(getSupportFragmentManager(), "datePicker");

                endDateView.setError(null);
            }
        });

        startTimeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTimeFrag = new TimePickerFragment(startTimeView);
                startTimeFrag.show(getSupportFragmentManager(), "timePicker");

                startTimeView.setError(null);
            }
        });

        endTimeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                endTimeFrag = new TimePickerFragment(endTimeView);
                endTimeFrag.show(getSupportFragmentManager(), "timePicker");

                endTimeView.setError(null);
            }
        });
    }

    /**
     * Changes all of the TextViews to the default color black.
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
     * Grabs all of the EditText Views so they can be referenced in the future.
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
     * Parses the user input into strings.
     */
    private void setUpInput() {
        //Grabs user input
        name = nameView.getText().toString();
        desc = descView.getText().toString();
        type = typeSpinner.getSelectedItem().toString();
        startDate = startDateView.getText().toString();
        endDate = endDateView.getText().toString();
        startTime = startTimeView.getText().toString();
        endTime = endTimeView.getText().toString();
    }

    /**
     * Checks if the user entered valid input. setUpInput MUST be called before checkInput is called
     * @return True/False depending on if we have valid input
     */
    private boolean checkInput() {
        boolean valid = true;
        String blankView = "This field cannot be left blank";
        String dateAfter = "The end date has to be after the start date";
        String timeAfter = "The end time has to be after the start time";

        //Checks that these fields are not left empty
        if (name.isEmpty()) {
            nameView.setError(blankView);
            valid = false;
        }

        if (desc.isEmpty()) {
            descView.setError(blankView);
            valid = false;
        }

        if (type.equals(typeList.get(0))) {
            pickText.setTextColor(errorColor);
            valid = false;
        }

        //Checks if the user inputted a date at all
        if (startDateView.getText().toString().matches("")) {
            startDateView.setError(blankView);
            valid = false;
        }
        else {
            startDateView.setError(null);
        }

        if (endDateView.getText().toString().matches("")) {
            endDateView.setError(blankView);
            valid = false;
        }
        else {
            endDateView.setError(null);
        }

        //Checks if the user entered a time at all
        if (startTimeView.getText().toString().matches("")) {
            startTimeView.setError(blankView);
            valid = false;
        }
        else {
            startTimeView.setError(null);
        }

        if (endTimeView.getText().toString().matches("")) {
            endTimeView.setError(blankView);
            valid = false;
        }
        else {
            endTimeView.setError(null);
        }

        //Checks if date/time that was entered is valid
        if (!startDate.isEmpty() && !endDate.isEmpty()) {
            //Checks if the end month or year is behind the start month or year
            if (startDateFrag.getMonth() > endDateFrag.getMonth() ||
                    startDateFrag.getYear() > endDateFrag.getYear()) {
                endDateView.setError(dateAfter);
                valid = false;
            //Checks that start day isn't after the end day if they're in the same month
            } else if (startDateFrag.getMonth() == endDateFrag.getMonth() &&
                    startDateFrag.getDay() > endDateFrag.getDay()) {
                endDateView.setError(dateAfter);
                valid = false;
            }

            //Check for if start time and end time are on the same day
            if (!startTime.isEmpty() && !endTime.isEmpty() &&
                    startDateFrag.getDay() == endDateFrag.getDay() &&
                    startDateFrag.getMonth() == endDateFrag.getMonth() &&
                    startDateFrag.getYear() == endDateFrag.getYear()) {
                //Check that start hour isn't after end hour if on the same day
                if (startTimeFrag.getHourInt() > endTimeFrag.getHourInt()) {
                    endTimeView.setError(timeAfter);
                    valid = false;
                //Check that start minute isn't after end minute in the same hour and day
                } else if (startTimeFrag.getHourInt() == endTimeFrag.getHourInt() &&
                        startTimeFrag.getMinInt() > endTimeFrag.getMinInt()) {
                    endTimeView.setError(timeAfter);
                    valid = false;
                }
            }
        }

        //Return validity of user input
        return valid;
    }

    /**
     * Called when the Create Event button is pressed.
     * @param view The view we are currently in
     */
    public void makeEvent(View view) {
        //Sets up user input into respective strings
        setUpInput();

        if (checkInput()) {
            event = new Event(name, desc, ((Project_18) getApplication()).getMe().getID(), 0,
                    new Date(
                    startDateFrag.getYear(),
                    startDateFrag.getMonth(),
                    startDateFrag.getDay(),
                    startTimeFrag.getHourInt(),
                    startTimeFrag.getMinInt()),
                    new Date(
                    endDateFrag.getYear(),
                    endDateFrag.getMonth(),
                    endDateFrag.getDay(),
                    endTimeFrag.getHourInt(),
                    endTimeFrag.getMinInt())
            );

            goToLocSelectAct();
        }
    }

    /**
     * Navigate to the map activity.
     */
    private void goToLocSelectAct() {
        startActivity(new Intent(this, LocSelectAct.class).putExtra("eventToInit", event));
    }
}