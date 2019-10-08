package com.example.onlineneighborhood;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FilterEvents extends AppCompatActivity implements View.OnClickListener {

    private static TextView timeSet, dateSet;
    private Button filterApply;
    private ImageView chooseTime, dateButton;
    private Spinner typeSpinner;
    static Date calenderDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter_events);
        timeSet = findViewById(R.id.timeView);
        filterApply = findViewById(R.id.filterApplyButton);
        chooseTime = findViewById(R.id.TimeFilter);
        typeSpinner = findViewById(R.id.filterTypeSpin);
        dateButton = findViewById(R.id.datePicker);
        dateSet = findViewById(R.id.dateView);
        dateSet.setText("DEFAULT");
        timeSet.setText("DEFAULT");
        dateSet.setVisibility(View.INVISIBLE);
        timeSet.setVisibility(View.INVISIBLE);
        chooseTime.setOnClickListener(this);
        filterApply.setOnClickListener(this);
        dateButton.setOnClickListener(this);



        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout((int)(width * 0.85), (int)(height * 0.6));

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.gravity = Gravity.CENTER;
        params.x = 0;
        params.y = -20;

        getWindow().setAttributes(params);
    }

    @Override
    public void onClick(View view) {
        if (view == filterApply) {
            if(timeSet.getText().toString().equals("DEFAULT")||dateSet.getText().toString().equals("DEFAULT")){
                Toast.makeText(this, "enter all fields", Toast.LENGTH_LONG).show();
            }else{
                try {
                    applyFilters();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }

        if (view == chooseTime) {
            showTimePickerDialog(view);
            timeSet.setVisibility(View.VISIBLE);
        }

        if(view == dateButton){
            showDatePickerDialog(view);
            dateSet.setVisibility(View.VISIBLE);
        }
    }

    public void applyFilters() throws ParseException {
        String date = dateSet.getText().toString().trim();
        String typeFilter = typeSpinner.getSelectedItem().toString();
        String time = timeSet.getText().toString().trim();


        Intent i = getIntent();
        String intentSuburb = i.getStringExtra("SUBURB");



        Intent intent = new Intent(this, BottomNavigationActivity.class);
        intent.putExtra("SUBURB", intentSuburb);
        intent.putExtra("DATE",  date);
        intent.putExtra("TYPE", typeFilter);
        intent.putExtra("TIME", time);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    public static class TimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {
        private String time;

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
            // Do something with the time chosen by the user
            time = hourOfDay + ":" + minute;
            timeSet.setVisibility(View.VISIBLE);
            timeSet.setText(time);
        }
    }



    public void showTimePickerDialog(View v) {
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getSupportFragmentManager(), "timePicker");
    }


    public void showDatePickerDialog(View v) {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }


    // NEED TO REFERENCE THIS

    public static class DatePickerFragment extends DialogFragment implements
            DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            // Do something with the date chosen by the user
            dateSet.setText(day + "/" + (month + 1) + "/" + year);
        }
    }



}
