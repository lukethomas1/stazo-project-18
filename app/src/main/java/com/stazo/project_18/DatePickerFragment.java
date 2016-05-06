package com.stazo.project_18;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;

import java.util.Calendar;

/**
 * Created by Brian Chan on 5/5/2016.
 */
@SuppressLint("ValidFragment")
public class DatePickerFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {
    private String date; //The date the user inputted
    EditText dateText; //The text we want to update once the user inputs a date

    /**
     * Constructor for a DatePicker fragment.
     * @param text The EditText we want to update
     * @param date The date the user inputted
     */
    public DatePickerFragment(EditText text, String date) {
        dateText = text;
        this.date = date;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current startDate as the default startDate in the picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog and return it
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        date = (month+1)+"/"+day+"/"+year;

        dateText.setText(date);
    }
}