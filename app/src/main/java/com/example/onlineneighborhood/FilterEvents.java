package com.example.onlineneighborhood;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.app.Activity;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

public class FilterEvents extends AppCompatActivity implements View.OnClickListener {

    private static TextView timeSet;
    private Button filterApply;
    private ImageView chooseTime;
    private Spinner dateSpinner, typeSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter_events);
        timeSet = findViewById(R.id.filterSetTime);
        filterApply = findViewById(R.id.filterApplyButton);
        chooseTime = findViewById(R.id.TimeFilter);
        dateSpinner = findViewById(R.id.dateFilterSpin);
        typeSpinner = findViewById(R.id.filterTypeSpin);

        chooseTime.setOnClickListener(this);
        filterApply.setOnClickListener(this);



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
            applyFilters();
        }

        if (view == chooseTime) {
            showTimePickerDialog(view);
        }
    }


    public void applyFilters(){
        String dateRange = dateSpinner.getSelectedItem().toString();
        String typeFilter = typeSpinner.getSelectedItem().toString();
        String time = timeSet.getText().toString().trim();

        Intent i = getIntent();
        String intentSuburb = i.getStringExtra("SUBURB");



        Intent intent = new Intent(this, BottomNavigationActivity.class);
        intent.putExtra("SUBURB", intentSuburb);
        intent.putExtra("DATE",  dateRange);
        intent.putExtra("TYPE", typeFilter);
        intent.putExtra("TIME", time);
        finish();
        startActivity(intent);
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


}
