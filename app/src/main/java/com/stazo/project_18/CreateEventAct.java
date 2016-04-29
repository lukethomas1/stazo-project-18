package com.stazo.project_18;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Spinner;
import android.widget.TextView;

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
    private final int hintColor = Color.rgb(128, 128, 128);

    //The events itself
    AutoCompleteTextView nameEvent;
    AutoCompleteTextView descEvent;
    AutoCompleteTextView startTimeEvent;
    AutoCompleteTextView endTimeEvent;
    Spinner typeEvent;
    TextView nameText, descText, pickText, startText, endText;

    //User inputted values
    String name;
    String desc;
    String startTime;
    String endTime;
    String type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_event);

        nameText = (TextView) findViewById(R.id.NameText);
        descText = (TextView) findViewById(R.id.DescText);
        pickText = (TextView) findViewById(R.id.PickText);
        startText = (TextView) findViewById(R.id.StartText);
        endText = (TextView) findViewById(R.id.EndText);

        nameText.setTextColor(normColor);
        descText.setTextColor(normColor);
        pickText.setTextColor(normColor);
        startText.setTextColor(normColor);
        endText.setTextColor(normColor);

        //Sets up the Spinner for selecting an Event Type
        typeEvent = (Spinner) findViewById(R.id.EventType);
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
        typeEvent.setAdapter(adapter);

        //Actions for spinner selection
        typeEvent.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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
        //Links the events
        nameEvent = (AutoCompleteTextView) findViewById(R.id.EventName);
        descEvent = (AutoCompleteTextView) findViewById(R.id.EventDesc);
        startTimeEvent = (AutoCompleteTextView) findViewById(R.id.StartTime);
        endTimeEvent = (AutoCompleteTextView) findViewById(R.id.EndTime);

        //Grabs user input
        name = nameEvent.getText().toString();
        desc = descEvent.getText().toString();
        startTime = startTimeEvent.getText().toString();
        endTime = endTimeEvent.getText().toString();
        type = typeEvent.getSelectedItem().toString();

        //Error checking
        if (name.isEmpty()) {
            nameText.setTextColor(errorColor);
        }
        else {
            nameText.setTextColor(normColor);
        }

        if (desc.isEmpty()) {
            descText.setTextColor(errorColor);
        }
        else {
            descText.setTextColor(normColor);
        }

        if (startTime.isEmpty()) {
            startText.setText("Enter a Start Time!");
            startText.setTextColor(errorColor);
        }
        else {
            startText.setText("Start Time");
            startText.setTextColor(normColor);
        }

        if (endTime.isEmpty()) {
            endText.setText("Enter an End Time!");
            endText.setTextColor(errorColor);
        }
        else {
            startText.setText("End Time");
            endText.setTextColor(normColor);
        }

        if (type.equals(typeList.get(0))) {
            pickText.setTextColor(errorColor);
        }
    }
}
