package com.example.onlineneighborhood;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.icu.util.Calendar;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static android.provider.CalendarContract.*;

public class createEvent extends AppCompatActivity implements View.OnClickListener {


    //creating initialization variables
    //long and latitude

    //bool check to ensure get location has been requested

    UserInformation host;


    //firebase variables
    private FirebaseAuth firebaseAuth;
    DatabaseReference databaseEvents;

    //location variables
    private final String DEFAULT_LOCAL = "please wait a few seconds while we get your location";
    private String locat = DEFAULT_LOCAL;
    Button getLocation, createEvent;


    //TODO: add this loading dialog
    private ProgressDialog progressDialog;

    //XML variables
    EditText evName, evDesc, evAddress;
    static TextView evTime, evDate;
    static int year, day, month;
    static int hour, minute;


    @Override
    protected void onStart() {

        super.onStart();

        databaseEvents.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot userSnapshot: dataSnapshot.getChildren()){
                    UserInformation user = userSnapshot.getValue(UserInformation.class);

                    host = user;
                    Log.d("user info: ", "onDataChange: " + host.name);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        databaseEvents = FirebaseDatabase.getInstance().getReference("events");

        // Bind Simple Variables
        createEvent = findViewById(R.id.createBtn);

        evName = findViewById(R.id.eventName);
        evDesc = findViewById(R.id.eventDesc);
        evTime = findViewById(R.id.eventTime);
        evDate = findViewById(R.id.eventDate);
        evAddress = findViewById(R.id.eventAddress);

        // Bind On clicks
        createEvent.setOnClickListener(this);
        evDate.setOnClickListener(this);
        evTime.setOnClickListener(this);

        //getting authentication info to link the event to the user creating it
        firebaseAuth = FirebaseAuth.getInstance();
        
    }


    //adding functionality to the buttons
    @Override
    public void onClick(View view) {

        if(view == createEvent){
            addEvent();
        }

        if(view == evTime){
            showTruitonTimePickerDialog(view);
        }


        if(view == evDate){
            showTruitonDatePickerDialog(view);
        }
    }

    /**
     * This method takes all the data provided by the activity and sends it to firebase.
     * (as long as it passes the error checks)
     *
     */
    public void addEvent(){
        //pass name, description, time, date, address, and user.
        String eventName = evName.getText().toString().trim();
        String eventDesc = evDesc.getText().toString().trim();
        String eventTime = evTime.getText().toString().trim();
        String eventDate = evDate.getText().toString().trim();


        Intent i = getIntent();
        String suburb = i.getStringExtra("SUBURB");

        String eventAddress = evAddress.getText().toString().trim();


        //checks all the fields are filled
        if(!TextUtils.isEmpty(eventName) && !TextUtils.isEmpty(eventDesc) && !eventTime.contains("Time")
        && !eventDate.contains("Date")){


            // Checks if the User's Logged in already, if so bypasses the Login Screen and takes them to Choose Screen
            //put this here to ensure that the user is not null and if it is will exit to the log in screen
            if (firebaseAuth.getCurrentUser() == null){
                Toast.makeText(this, "you are not logged in", Toast.LENGTH_LONG).show();
                finish();
                startActivity(new Intent(getApplicationContext(), Login.class));
            }

            //this is just doing a couple of checks to ensure that a address is *actually* sent to firebase
            //TODO: this needs to be cleaned up/properly checked
            if(!TextUtils.isEmpty(eventAddress)){
                String id = databaseEvents.push().getKey();
                ArrayList<UserInformation> attendees = new ArrayList<UserInformation>();
                attendees.add(host);
                Event event = new Event(id, host, suburb, eventAddress, eventName, eventDesc, eventTime, eventDate, attendees);
                databaseEvents.child(id).setValue(event);
                Toast.makeText(this, "event created! its party time", Toast.LENGTH_LONG).show();
                createCalenderEvent(eventName, eventDesc, eventAddress);
            }
            else if(TextUtils.isEmpty(eventAddress)){
                Toast.makeText(this, "you have not entered an address", Toast.LENGTH_LONG).show();
            }
        }
        else{
            Toast.makeText(this, "please enter all fields", Toast.LENGTH_LONG).show();
        }
    }


    /***************************************************************************************
     *
     *    took this code for the clock and date functionality. Referenced the place i got it from below.
     *
     *    Title: Android pick date time from EditText OnClick event
     *    Author: Mohit Gupt
     *    Date: April 15, 2015
     *    Code version: n/a
     *    Availability: https://www.truiton.com/2013/03/android-pick-date-time-from-edittext-onclick-event/
     *
     ***************************************************************************************/


    public static class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener{
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            hour = c.get(Calendar.HOUR_OF_DAY);
            minute = c.get(Calendar.MINUTE);

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            // Do something with the time chosen by the user
            // due to int data type, a bodge job adding zeros manually.
            if(minute < 10){
                evTime.setText(hourOfDay + ":0"	+ minute);
            }
            else if(minute == 0){
                evTime.setText(hourOfDay + ":00" + minute);
            }else {
                evTime.setText(hourOfDay + ":"	+ minute);
            }
        }
    }

    public static class DatePickerFragment extends DialogFragment implements
            DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            year = c.get(Calendar.YEAR);
            month = c.get(Calendar.MONTH);
            day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            // Do something with the date chosen by the user
            evDate.setText(day + "/" + (month + 1) + "/" + year);
        }
    }

    public void showTruitonTimePickerDialog(View v) {
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getSupportFragmentManager(), "timePicker");
    }

    public void showTruitonDatePickerDialog(View v) {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    // TODO: Need to add an end time to event creation
    public void createCalenderEvent(String title, String description, String eventAddress){

        java.util.Calendar beginTime = java.util.Calendar.getInstance();
        beginTime.set(year, month, day, hour, minute);
        java.util.Calendar endTime = java.util.Calendar.getInstance();
        endTime.set(year, month, day, hour, minute + 5);

        Intent intent = new Intent(Intent.ACTION_INSERT)
                .setData(Events.CONTENT_URI)
                .putExtra(EXTRA_EVENT_BEGIN_TIME, beginTime.getTimeInMillis())
                .putExtra(EXTRA_EVENT_END_TIME, endTime.getTimeInMillis())
                .putExtra(Events.TITLE, title)
                .putExtra(Events.DESCRIPTION, description)
                .putExtra(Events.EVENT_LOCATION, eventAddress);
        startActivity(intent);
    }

}
