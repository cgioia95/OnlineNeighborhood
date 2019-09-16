package com.example.onlineneighborhood;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static android.provider.CalendarContract.*;

public class createEvent extends AppCompatActivity implements View.OnClickListener {


    //creating initialization variables
    //long and latitude
    private double lon;
    private double lat;
    private Suburb suburb;
    private String suburbName = "NO SUBURB FOUND";
    //bool check to ensure get location has been requested
    private boolean clicked = false;
    UserInformation host;


    //firebase variables
    private FirebaseAuth firebaseAuth;
    DatabaseReference databaseEvents;
    DatabaseReference databaseUsers;
    DatabaseReference databaseSuburb;



    //location variables
    private final String DEFAULT_LOCAL = "please wait a few seconds while we get your location";
    private String locat = DEFAULT_LOCAL;
    Button getLocation, createEvent;


    //TODO: add this loading dialog
    private ProgressDialog progressDialog;

    //XML variables
    EditText evName, evDesc, evAddress;
    private TextView eventTv;
    static TextView evTime, evDate;
    static int year, day, month;
    static int hour, minute;
    ArrayList<UserInformation> users;

    // Two components used to get user Location
    private LocationManager locationManager;
    private LocationListener locationListener;

    @Override
    protected void onStart() {

        super.onStart();

        databaseUsers.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                for(DataSnapshot hostSnapshot : dataSnapshot.getChildren()){

                    String checkCurUser = firebaseAuth.getCurrentUser().getUid();
                    if(checkCurUser.equals(hostSnapshot.getKey())){
                        UserInformation currentUser = hostSnapshot.getValue(UserInformation.class);
                        host = currentUser;
                        break;
                    }
                    Log.d("HOSTS: ", ""+hostSnapshot);
                }

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("error: ", ""+databaseError);

            }
        });


        databaseSuburb.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot suburbSnapshot : dataSnapshot.getChildren()) {
                   Suburb currentSuburb = suburbSnapshot.getValue(Suburb.class);
                   Intent i = getIntent();
                   String intentSuburb = i.getStringExtra("SUBURB");
                    Log.d("SUBURB", "" + suburbSnapshot);
                    try{

                        if (intentSuburb.equals(currentSuburb.getSubName())) {
                            suburb = currentSuburb;
                            Log.d("CHOSEN: ", "" + suburb + suburb.getSubName());
                            break;
                        }

                    } catch (NullPointerException e){
                        //this catches null pointer exceptions, it happens alot
                        //TODO: I need to find a better way to loop through all the suburbs
                        //if you look at the log you can see the 'null pointer' still gets the suburb name. weird.
                        Log.d("ERROR VALUES", "" + currentSuburb.getSubName());
                    }


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


        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_create_event);

        databaseEvents = FirebaseDatabase.getInstance().getReference("events");
        databaseUsers = FirebaseDatabase.getInstance().getReference("Users");
        databaseSuburb =  FirebaseDatabase.getInstance().getReference("suburbs");

        //Metrics of the popup window. Currently setting it to 80% of screen width and height
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout((int)(width*.8), (int)(height*.8));

        // Bind Simple Variables
        users = new ArrayList<UserInformation>();
        eventTv = findViewById(R.id.eventTv);
        createEvent = findViewById(R.id.createBtn);
        getLocation = findViewById(R.id.btnGetLocation);
        evName = findViewById(R.id.eventName);
        evDesc = findViewById(R.id.eventDesc);
        evTime = findViewById(R.id.eventTime);
        evDate = findViewById(R.id.eventDate);
        evAddress = findViewById(R.id.eventAddress);

        // Bind On clicks
        getLocation.setOnClickListener(this);
        createEvent.setOnClickListener(this);
        evDate.setOnClickListener(this);
        evTime.setOnClickListener(this);

        //getting authentication info to link the event to the user creating it
        firebaseAuth = FirebaseAuth.getInstance();

        // Setup the Location Manager and Listener
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // just taking Gioias code and converting it to get a specific location
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                lon = location.getLongitude();
                lat = location.getLatitude();

                String coordinates = "Long: " + lon + "Lat: " + lat;

                Log.d("LOCATION", "Long: " + lon + "Lat: " + lat );


                //gets the specific location to the address
                locat = getLocation(lon, lat);


               Log.d("LOCATION", locat );


            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);

            }
        };

        // Checks if user has granted necessary location tracking permissions
        // If they haven't, requests them from the user
        // If they have, enables the location button to request location updates
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.INTERNET}, 10
            );

            return;
        } else {
            //configure Button is the method which updates the location every 5 seconds
            configureButton();
        }

        //sets the
        eventTv.setText(locat);


    }


    //adding functionality to the buttons
    @Override
    public void onClick(View view) {
        if(view == getLocation){
            eventTv.setText(locat);
            //if this is true then location services has been requested and we are allowed to use it
            clicked = true;
        }

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
     * This method takes the long and lat provided and converts it through googles API to
     * an actual address
     *
     * @param lon longitude for geo location, param is provided by locationListener
     * @param lat latitude for geo location, param is provided by locationListener
     * @return returns specific address of the two long/lat co-ordinates
     */
    private String getLocation(double lon, double lat) {


        String location = "NO LOCATION FOUND";
        Geocoder geocoder = new Geocoder(createEvent.this, Locale.getDefault());


        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lon, 1);
            location = addresses.get(0).getAddressLine(0);

            Log.d("LOCATION", location);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return location;
    }


    // Once the Location Button is pressed, the location manager will start returning user's location
    public void configureButton() {

        Log.d("LOCATION", "FETCHING LOCATION UPDATES");


        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 5, locationListener);

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
                Event event = new Event(id, host, eventAddress, eventName, eventDesc, eventTime, eventDate, attendees);

                DatabaseReference databaseSuburbChange = FirebaseDatabase.getInstance().getReference("suburbs").child(suburb.getId());
                ArrayList<Event> events = new ArrayList<Event>();
                events.add(event);
                if(suburb.getEvents() == null){
                    suburb.setEvents(events);
                }else{
                    suburb.getEvents().add(event);
                }
                databaseSuburbChange.setValue(suburb);


                databaseEvents.child(id).setValue(event);

                Toast.makeText(this, "event created! its party time", Toast.LENGTH_LONG).show();

                createCalenderEvent(eventName, eventDesc, eventAddress);
            }
            else if(!TextUtils.isEmpty(eventAddress)){
                String id = databaseEvents.push().getKey();
                ArrayList<UserInformation> attendees = new ArrayList<UserInformation>();
                attendees.add(host);
                Event event = new Event(id, host, eventAddress, eventName, eventDesc, eventTime, eventDate, attendees);

                DatabaseReference databaseSuburbChange = FirebaseDatabase.getInstance().getReference("suburbs").child(suburb.getId());
                ArrayList<Event> events = new ArrayList<Event>();
                events.add(event);
                if(suburb.getEvents() == null){
                    suburb.setEvents(events);
                }else{
                    suburb.getEvents().add(event);
                }
                databaseSuburbChange.setValue(suburb);


                databaseEvents.child(id).setValue(event);

                Toast.makeText(this, "event created! its party time", Toast.LENGTH_LONG).show();

                createCalenderEvent(eventName, eventDesc, eventAddress);

            }
            else if(clicked && TextUtils.isEmpty(eventAddress) && !locat.equals(DEFAULT_LOCAL) && !locat.isEmpty()) {
                eventAddress = locat;
                String id = databaseEvents.push().getKey();
                ArrayList<UserInformation> attendees = new ArrayList<UserInformation>();
                attendees.add(host);
                Event event = new Event(id, host, eventAddress, eventName, eventDesc, eventTime, eventDate, attendees);

                DatabaseReference databaseSuburbChange = FirebaseDatabase.getInstance().getReference("suburbs").child(suburb.getId());
                ArrayList<Event> events = new ArrayList<Event>();
                events.add(event);
                if(suburb.getEvents() == null){
                    suburb.setEvents(events);
                }else{
                    suburb.getEvents().add(event);
                }
                databaseSuburbChange.setValue(suburb);

                databaseEvents.child(id).setValue(event);

                Toast.makeText(this, "event created! its party time", Toast.LENGTH_LONG).show();

                createCalenderEvent(eventName, eventDesc, eventAddress);
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
