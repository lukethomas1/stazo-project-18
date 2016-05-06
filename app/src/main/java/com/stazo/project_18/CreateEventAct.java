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
import com.google.android.gms.maps.model.LatLng;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Brian Chan
 */
public class CreateEventAct extends AppCompatActivity {

    private Event event = new Event();
    private List<String> typeList;
    int normColor = Color.BLACK;
    int errorColor = Color.RED;

    //The events itself
    EditText nameView;
    EditText descView;
    EditText dateView;
    EditText startTimeView;
    EditText endTimeView;

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

        setUpTextColors();

        dateView = (EditText) findViewById(R.id.Date);
        startTimeView = (EditText) findViewById(R.id.StartTime);
        endTimeView = (EditText) findViewById(R.id.EndTime);

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

        dateView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(dateView, date);
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

    public void showDatePickerDialog(EditText text, String date) {
        DatePickerFragment newFragment = new DatePickerFragment(text, date);
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    public void showTimePickerDialog(EditText text, String time) {
        DialogFragment newFragment = new TimePickerFragment(text, time);
        newFragment.show(getSupportFragmentManager(), "timePicker");
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

    // Navigate to the map activity
    private void goToLocSelectAct() {
        startActivity(new Intent(this, LocSelectAct.class).putExtra("eventToInit", event));
    }

    private void setUpTextColors() {
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
    }

    // Sets up text fields
    private void setUpInput() {
        //Sets up the views
        nameView = (EditText) findViewById(R.id.EventName);
        descView = (EditText) findViewById(R.id.EventDesc);

        //Grabs user input
        name = nameView.getText().toString();
        desc = descView.getText().toString();
        type = typeSpinner.getSelectedItem().toString();
    }

    // Checks if text fields are set properly
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
}
