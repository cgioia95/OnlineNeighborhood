package com.example.onlineneighborhood;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

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
import android.provider.Settings;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
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
import static java.util.Locale.getDefault;

public class createEvent extends AppCompatActivity implements View.OnClickListener {

    //Creating initialization variables
    //Bool check to ensure get location has been requested
    private boolean clicked = false;
    UserInformation hostID, host;
    String intentSuburb;

    //Firebase variables
    private FirebaseAuth firebaseAuth;
    DatabaseReference databaseEvents, databaseUsers, databaseSuburb;

    //location variables
    private final String DEFAULT_LOCAL = "NO LOCATION FOUND";
    private String locat = DEFAULT_LOCAL;
    //Long and latitude
    private double lon, lat;
    private Suburb suburb;
    Button getLocation, createEvent;

    //XML variables
    EditText evName, evDesc, evAddress;
    private TextView eventTv;
    static TextView evTime, evDate, evEndTime, evEndDate;
    static int year, day, month;
    static int hour, minute;
    private static boolean startAndEnd;
    private Spinner eventTypeSpinner;
    CheckBox addCal;
    ArrayList<UserInformation> users;

    //Two components used to get user Location
    private LocationManager locationManager;
    private LocationListener locationListener;

    @Override
    protected void onStart() {

        super.onStart();

        //finding host data based off the firebase UID and assigning it to a local UserInformation variable
        databaseUsers.child(firebaseAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserInformation currentUser = dataSnapshot.getValue(UserInformation.class);
                host = currentUser;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });


        databaseSuburb.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Suburb currSuburb = dataSnapshot.getValue(Suburb.class);
                suburb = currSuburb;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //Auto-filling dates
        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        String setHour = timeToString(hour);
        String setMin = timeToString(minute);
        String setEndHour = timeToString(hour+1);

        evTime.setText(setHour +":"+setMin);
        evEndTime.setText(setEndHour+":"+setMin);
        evDate.setText(day+"/"+(month+1)+"/"+year);
        evEndDate.setText(day+"/"+(month+1)+"/"+year);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent i = getIntent();
        intentSuburb = i.getStringExtra("SUBURB");

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_create_event);

        databaseEvents = FirebaseDatabase.getInstance().getReference("events");
        databaseUsers = FirebaseDatabase.getInstance().getReference("Users");
        databaseSuburb =  FirebaseDatabase.getInstance().getReference("suburbs").child(intentSuburb);

        //Bind Simple Variables
        users = new ArrayList<UserInformation>();
        eventTv = findViewById(R.id.eventTv);
        createEvent = findViewById(R.id.editBtn);
        getLocation = findViewById(R.id.btnGetLocation);
        evName = findViewById(R.id.eventName);
        evDesc = findViewById(R.id.eventDesc);
        evTime = findViewById(R.id.eventTime);
        evDate = findViewById(R.id.eventDate);
        evEndDate = findViewById(R.id.endDate);
        evEndTime = findViewById(R.id.endTime);
        evAddress = findViewById(R.id.eventAddress);
        addCal = findViewById(R.id.addCal);

        //Setting up the spinner with different types of event
        eventTypeSpinner = (Spinner) findViewById(R.id.spinnerEventType);
        String[] spinner_array = getApplicationContext().getResources().getStringArray(R.array.eventTypes);

        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(
                this,R.layout.spinner_item,spinner_array
        );
        spinnerArrayAdapter.setDropDownViewResource(R.layout.spinner_item);
        eventTypeSpinner.setAdapter(spinnerArrayAdapter);

        //Bind On-clicks
        getLocation.setOnClickListener(this);
        createEvent.setOnClickListener(this);
        evDate.setOnClickListener(this);
        evTime.setOnClickListener(this);
        evEndTime.setOnClickListener(this);
        evEndDate.setOnClickListener(this);

        //Getting authentication info to link the event to the user creating it
        firebaseAuth = FirebaseAuth.getInstance();

        //Setup the Location Manager and Listener
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //Get a specific location
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                lon = location.getLongitude();
                lat = location.getLatitude();
                String coordinates = "Long: " + lon + "Lat: " + lat;

                Log.d("LOCATION", "Long: " + lon + "Lat: " + lat );

                //Gets the specific location to the address
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

        //Checks if user has granted necessary location tracking permissions
        //If they haven't, requests them from the user
        //If they have, enables the location button to request location updates
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.INTERNET}, 10
            );

            return;
        } else {
            //Configure Button is the method which updates the location every 5 seconds
             locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 5, locationListener);
        }

    }



    @Override
    public void onClick(View view) {
        if(view == getLocation){
            evAddress.setText(locat);
            //If this is true then location services has been requested and we are allowed to use it
            clicked = true;
        }

        if(view == createEvent){
            addEvent();
        }

        if(view == evTime){
            startAndEnd = true;
            showTimePickerDialog(view);
        }
        if(view == evEndTime){
            startAndEnd = false;
            showTimePickerDialog(view);
        }

        if(view == evDate){
            startAndEnd = true;
            showDatePickerDialog(view);
        }

        if(view == evEndDate){
            startAndEnd = false;
            showDatePickerDialog(view);
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

    /**
     * This method takes all the data provided by the activity and sends it to firebase.
     * (as long as it passes the error checks)
     *
     */
    public void addEvent(){

        //Pass name, description, time, date, address, and user.
        String eventName = evName.getText().toString().trim();
        String eventDesc = evDesc.getText().toString().trim();
        String eventTime = evTime.getText().toString().trim();
        String eventDate = evDate.getText().toString().trim();
        String endTime = evEndTime.getText().toString().trim();
        String endDate = evEndDate.getText().toString().trim();
        final String type = eventTypeSpinner.getSelectedItem().toString();

        Log.d("EVENTCREATE", "CREATING EVENT");

        String eventAddress = evAddress.getText().toString().trim();

        //Checks all the fields are filled
        if(!TextUtils.isEmpty(eventName) && !eventTime.contains("Time") && !endDate.contains("Date")
                && !endTime.contains("Time") && !TextUtils.isEmpty(eventDesc) && !eventDate.contains("Date")){

            //Checks if the User's Logged in already, if so bypasses the Login Screen and takes them to Choose Screen
            //Put this here to ensure that the user is not null and if it is will exit to the log in screen
            if (firebaseAuth.getCurrentUser() == null){
                Toast.makeText(this, "you are not logged in", Toast.LENGTH_LONG).show();
                finish();
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }

            //This is just doing a couple of checks to ensure that a address is *actually* sent to firebase
            if(!TextUtils.isEmpty(eventAddress)){
                String addressStatus = validate(eventAddress);

                if (addressStatus!="VALID"){
                    return;
                }
                if(addEventToSuburb(eventAddress, eventName, eventDesc, eventTime, eventDate, endTime, endDate, type)){
                    Toast.makeText(this, "event created! its party time", Toast.LENGTH_LONG).show();
                    if(addCal.isChecked()){
                        createCalenderEvent(eventName, eventDesc, eventAddress);
                    }
                    this.finish();

                }else{
                    Toast.makeText(this, "sorry, something went wrong, please try again", Toast.LENGTH_LONG).show();
                }
            }
            else if(clicked && TextUtils.isEmpty(eventAddress) && !locat.equals(DEFAULT_LOCAL) && !locat.isEmpty()) {
                eventAddress = locat;
                String addressStatus = validate(eventAddress);
                if (addressStatus!="VALID"){
                    return;
                }
                if(addEventToSuburb(eventAddress, eventName, eventDesc, eventTime, eventDate, endTime, endDate, type)){
                    Toast.makeText(this, "event created! its party time", Toast.LENGTH_LONG).show();

                    if(addCal.isChecked()){
                        createCalenderEvent(eventName, eventDesc, eventAddress);
                    }

                    this.finish();

                }else{
                    Toast.makeText(this, "sorry, something went wrong, please try again", Toast.LENGTH_LONG).show();
                }
            }
        }
        else{
            Toast.makeText(this, "please enter all fields", Toast.LENGTH_LONG).show();
        }
    }

    public boolean addEventToSuburb(String eventAddress,String eventName,String eventDesc, String eventTime, String eventDate, String endTime, String endDate, String type){
        try{
            //Creating the event and all the variables needed for the event.
            String id = databaseEvents.push().getKey();
            //The method above generates a random key every second. need to consolidate the value so it
            //doesn't change everytime id is called.
            String confirmid = id;

            //Gets currents users ID and assigns it as the host, creates and adds it to the attendee list
            hostID = new UserInformation(firebaseAuth.getCurrentUser().getUid());
            ArrayList<UserInformation> attendees = new ArrayList<UserInformation>();
            attendees.add(hostID);

            //Creating 2 events. one to add to the suburb, and one to link to the user.
            Event event = new Event(confirmid, hostID, eventAddress, eventName, eventDesc, eventTime, eventDate, endTime, endDate, type, attendees, suburb.getId());
            Event userEvent = new Event(confirmid, suburb.getId());

            //Creating database references and list arrays to properly ensure that a dynamic array will be properly updated to suburb/user values
            DatabaseReference databaseSuburbChange = FirebaseDatabase.getInstance().getReference("suburbs").child(suburb.getId());
            DatabaseReference databaseUpdateUser = FirebaseDatabase.getInstance().getReference("Users").child(hostID.getUid());
            ArrayList<Event> events = new ArrayList<Event>();
            events.add(event);
            ArrayList<Event> userEvents = new ArrayList<Event>();
            userEvents.add(userEvent);
            ArrayList<Event> eventCheck = host.getMyEvents();
            ArrayList<Event> myEventsCheck = host.getMyEventsAttending();

            //Checks if an events array exists in the suburb, if it doesn't, this creates one
            if(suburb.getEvents() == null){
                suburb.setEvents(events);
            }else{
                suburb.getEvents().add(event);
            }

            if(eventCheck == null){
                host.setMyEvents(userEvents);
            }else{
                host.getMyEvents().add(userEvent);
            }

            if(myEventsCheck == null){
                host.setMyEventsAttending(userEvents);
            }else{
                host.getMyEventsAttending().add(userEvent);
            }

            //Sends update to Firebase
            databaseUpdateUser.setValue(host);
            databaseSuburbChange.setValue(suburb);
            return true;
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    /***************************************************************************************
     *
     *    Took this code for the clock and date functionality. Referenced the place I got it from below.
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
            //Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            hour = c.get(Calendar.HOUR_OF_DAY);
            minute = c.get(Calendar.MINUTE);

            //Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            //Do something with the time chosen by the user
            //due to int data type, a bodge job adding zeros manually.
            String setHour = com.example.onlineneighborhood.createEvent.timeToString(hourOfDay);
            String setMin = com.example.onlineneighborhood.createEvent.timeToString(minute);
            if(startAndEnd){
                evTime.setText(setHour + ':' + setMin);

            }else{
                evEndTime.setText(setHour+":"+setMin);
            }
        }
    }

    public static class DatePickerFragment extends DialogFragment implements
            DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            //Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            year = c.get(Calendar.YEAR);
            month = c.get(Calendar.MONTH);
            day = c.get(Calendar.DAY_OF_MONTH);

            //Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            //Do something with the date chosen by the user
            if(startAndEnd){
                evDate.setText(day + "/" + (month + 1) + "/" + year);
            }
            else {
                evEndDate.setText(day + "/" + (month + 1) + "/" + year);
            }
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

    public String validate(String eventAddress){

        List<Address> addresses = null;
        Geocoder geocoder = new Geocoder(getApplicationContext(), getDefault());

        try {
            addresses = geocoder.getFromLocationName(eventAddress, 1);
            if (addresses.size() > 0) {

                Address address = addresses.get(0);
                String testedSuburb = address.getLocality();

                if (!suburb.getSubName().equals(testedSuburb)) {
                    Log.d("VALIDATOR", "NOT IN SUBURB");
                    Log.d("VALIDATOR", "We are in " + intentSuburb + " You have entered: " + testedSuburb);
                    Toast.makeText(this, "Address not in suburb", Toast.LENGTH_LONG).show();
                    return "NOT_IN_SUBURB";
                }

            } else {
                Log.d("VALIDATOR", "2 - INVALID");
                Toast.makeText(this, "Address not valid", Toast.LENGTH_LONG).show();
                return "INVALID";
            }

        } catch (IOException e) {
            Toast.makeText(this, "Address not valid", Toast.LENGTH_LONG).show();
            e.printStackTrace();
            return "INVALID";

        }
        return "VALID";
    }


    //manipulating the time int variables receives from calender and converting them for readability purposes
    public static String timeToString(int time){
        String setTime;
        if(time == 0){
            setTime = "00";
            return setTime;
        } else if(time < 10){
            setTime = "0" + time;
            return setTime;
        } else {
            setTime = "" + time;
            return setTime;
        }
    }
}
