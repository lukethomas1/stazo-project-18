package com.stazo.project_18;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;
import android.widget.EditText;

import java.util.Calendar;

/**
 * Author: Brian Chan
 * Date: 5/5/2016
 * Description: This class launches a DatePicker in a fragment for CreateEventAct.java for
 * stazo-project-18.
 */
@SuppressLint("ValidFragment")
public class DatePickerFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {
    private String date; //The date the user inputted
    EditText dateText; //The text we want to update once the user inputs a date
    int year, month, day;

    /**
     * Constructor for a DatePicker fragment.
     * @param text The EditText we want to update
     */
    public DatePickerFragment(EditText text) {
        dateText = text;
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
        String monthStr = "" + (month + 1);
        String dayStr = "" + day;

        if (month + 1 < 10) {
            monthStr = "0" + (month + 1);
        }
        if (day < 10) {
            dayStr = "0" + day;
        }
        date = monthStr+"/"+dayStr+"/"+year;

        this.year = year;
        this.month = month + 1;
        this.day = day;

        dateText.setText(date);
    }

    public void setYear(int year) {
        this.year = year;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getYear() {
        return year;
    }

    public int getMonth() {
        return month;
    }

    public int getDay() {
        return day;
    }
}