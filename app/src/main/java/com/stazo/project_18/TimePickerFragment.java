package com.stazo.project_18;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.widget.EditText;
import android.widget.TimePicker;

import java.util.Calendar;

/**
 * Created by Brian Chan on 5/5/2016.
 */
@SuppressLint("ValidFragment")
public class TimePickerFragment extends DialogFragment
        implements TimePickerDialog.OnTimeSetListener {
    private String time;
    EditText text;

    public TimePickerFragment(EditText text, String time) {
        this.time = time;
        this.text = text;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current time as the default values for the picker
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        // Create a new instance of TimePickerDialog and return it
        return new TimePickerDialog(getActivity(), this, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        String hour = "" + hourOfDay;
        String min = "" + minute;

        if (hourOfDay < 12) {
            if (hourOfDay == 0) {
                hour = "12";
            }
            if (minute == 0) {
                min = "00";
            }
            time = hour + ":" + min + "AM ";
        }
        else {
            if (hourOfDay == 12) {
                hour = "12";
            }
            else {
                hour = "" + (hourOfDay - 12);
            }
            if (minute == 0) {
                min = "00";
            }
            time = hour + ":" + min + "PM ";
        }

        text.setText(time);
    }
}