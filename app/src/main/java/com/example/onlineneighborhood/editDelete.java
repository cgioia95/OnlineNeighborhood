package com.example.onlineneighborhood;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.Dialog;
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
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static android.provider.CalendarContract.EXTRA_EVENT_BEGIN_TIME;
import static android.provider.CalendarContract.EXTRA_EVENT_END_TIME;
import static java.util.Locale.getDefault;

public class editDelete extends AppCompatActivity implements View.OnClickListener{


    public  static  final String TAG = "EDIT/DELETE";

    // Declare the simple variables
    EditText evName, evDesc, evAddress;
    private TextView eventTv;
    static TextView evTime, evDate, evEndTime, evEndDate;
    private Spinner eventTypeSpinner;
    Button btnGetLocation, editBtn, deleteBtn;
    public String eventId;
    CheckBox addCal;
    Event preEvent;

    //Firebase variables
    private FirebaseAuth firebaseAuth;
    DatabaseReference databaseUsers, databaseSuburb, databaseEvent;


    //Location-based initialization variables
    //Longitude and latitude
    private double lon;
    private double lat;
    private Suburb suburb;
    //Bool check to ensure get location has been requested
    private boolean clicked = false;
    private final String DEFAULT_LOCAL = "please wait a few seconds while we get your location";
    private String locat = DEFAULT_LOCAL;

    // Date variables
    static int year, day, month;
    static int hour, minute;
    private static boolean startAndEnd;

    //Two components used to get user Location
    private LocationManager locationManager;
    private LocationListener locationListener;

    @Override
    protected void onStart() {


        super.onStart();

        // Inspects the suburb we're currently in
        databaseSuburb.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Suburb currSuburb = dataSnapshot.getValue(Suburb.class);
                            suburb = currSuburb;

                            // Retrieve all the events within that suburb
                databaseSuburb.child("events").addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot eventSnapshot: dataSnapshot.getChildren()){

                            // Look for the event matching the one we're editing
                            if (eventId.equals(eventSnapshot.child("id").getValue().toString())){
                                databaseEvent = eventSnapshot.getRef();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent i = getIntent();

        // Edit delete pops up as a window
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_edit_delete);

        // Initialize all variables
        btnGetLocation =findViewById(R.id.btnGetLocation);
        editBtn = findViewById(R.id.editBtn);
        deleteBtn = findViewById(R.id.deleteBtn);

        //Get views of event details
        evName = findViewById(R.id.eventName);
        evDesc = findViewById(R.id.eventDesc);
        evAddress = findViewById(R.id.eventAddress);
        eventTv = findViewById(R.id.eventTv);
        evTime = findViewById(R.id.eventTime);
        evDate = findViewById(R.id.eventDate);
        evEndTime = findViewById(R.id.endTime);
        evEndDate = findViewById(R.id.endDate);
        eventTypeSpinner = findViewById(R.id.spinnerEventType);
        addCal = findViewById(R.id.addCal);

        //Setting up the spinner with different types of event
        eventTypeSpinner = (Spinner) findViewById(R.id.spinnerEventType);
        String[] spinner_array = getApplicationContext().getResources().getStringArray(R.array.eventTypes);

        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(
                this,R.layout.spinner_item,spinner_array
        );
        spinnerArrayAdapter.setDropDownViewResource(R.layout.spinner_item);
        eventTypeSpinner.setAdapter(spinnerArrayAdapter);

        // Retrieve the event we're actually changing and get its suburb id
        preEvent =  (Event)i.getSerializableExtra("MyObject");
        String intentSuburb = preEvent.getSuburbId();

        // Initialize our firebase references
        databaseUsers = FirebaseDatabase.getInstance().getReference("Users");
        databaseSuburb =  FirebaseDatabase.getInstance().getReference("suburbs").child(intentSuburb);

        //Getting authentication info to link the event to the user creating it
        firebaseAuth = FirebaseAuth.getInstance();
        eventId = preEvent.getId();


        // Retrieve all our event info, prior to edits and set on click listeners for all of them
        String preName = preEvent.getName();
        String preDescription = preEvent.getDescription();
        String preType = preEvent.getType();
        String preStartDate = preEvent.getDate();
        String preStartTime = preEvent.getTime();
        String preEndDate = preEvent.getDate();
        String preEndTime = preEvent.getEndTime();
        String preAddress = preEvent.getAddress();

        //Set views of event details
        evName.setText(preName);
        evDesc.setText(preDescription);
        eventTv.setText(preAddress);
        eventTypeSpinner.setSelection(Arrays.asList(getResources().getStringArray(R.array.eventTypes)).indexOf(preType));
        evTime.setText(preStartTime);
        evDate.setText(preStartDate);
        evEndTime.setText(preEndTime);
        evEndDate.setText(preEndDate);
        evAddress.setText(preAddress);

        evTime.setOnClickListener(this);
        evDate.setOnClickListener(this);
        evEndTime.setOnClickListener(this);
        evEndDate.setOnClickListener(this);
        btnGetLocation.setOnClickListener(this);
        editBtn.setOnClickListener(this);

        //Setup the Location Manager and Listener
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //Gets our specific location
        locationListener = new LocationListener() {

            @Override
            public void onLocationChanged(Location location) {

                lon = location.getLongitude();
                lat = location.getLatitude();

                //Gets the specific location to the address
                locat = getLocation(lon, lat);
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

        eventTv.setText(locat);
    }

    //Adding functionality to the buttons
    @Override
    public void onClick(View view) {

        if (view == deleteBtn ){
            deleteEvent();
            this.finish();
        }

        if(view == btnGetLocation){
            eventTv.setText(locat);
            //If this is true then location services has been requested and we are allowed to use it
            clicked = true;
        }

        if(view == editBtn){
            editEvent();
        }

        if(view == evTime){
            startAndEnd = true;
            showTruitonTimePickerDialog(view);
        }
        if(view == evEndTime){
            startAndEnd = false;
            showTruitonTimePickerDialog(view);
        }

        if(view == evDate){
            startAndEnd = true;
            showTruitonDatePickerDialog(view);
        }

        if(view == evEndDate){
            startAndEnd = false;
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
        Geocoder geocoder = new Geocoder(editDelete.this, Locale.getDefault());

        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lon, 1);
            location = addresses.get(0).getAddressLine(0);

            Log.d("LOCATION", location);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return location;
    }


    // Logic to edit events
    public void editEvent(){

        // Retrieve the currently entered edited information
        String eventName = evName.getText().toString().trim();
        String eventDesc = evDesc.getText().toString().trim();
        String eventTime = evTime.getText().toString().trim();
        String eventDate = evDate.getText().toString().trim();
        String endTime = evEndTime.getText().toString().trim();
        String endDate = evEndDate.getText().toString().trim();
        final String type = eventTypeSpinner.getSelectedItem().toString();
        String eventAddress = evAddress.getText().toString().trim();

        //Checks all the fields are filled
        if(!TextUtils.isEmpty(eventName) && !eventTime.contains("Time") && !endDate.contains("Date")
                && !endTime.contains("Time") && !TextUtils.isEmpty(eventDesc) && !eventDate.contains("Date")){

            //Checks if the User's Logged in already, if so bypasses the Login Screen and takes them to Choose Screen
            //put this here to ensure that the user is not null and if it is will exit to the log in screen
            if (firebaseAuth.getCurrentUser() == null){
                Toast.makeText(this, "you are not logged in", Toast.LENGTH_LONG).show();
                finish();
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }

            //This is just doing a couple of checks to ensure that a address is *actually* sent to firebase
            //TODO: this needs to be cleaned up/properly checked
            if(!TextUtils.isEmpty(eventAddress)){
                String addressStatus = validate(eventAddress);
                if (addressStatus!="VALID"){
                    return;
                }
                if(editEventInSuburb(eventAddress, eventName, eventDesc, eventTime, eventDate, endTime, endDate, type)){
                    Toast.makeText(this, "event changed! its party time", Toast.LENGTH_LONG).show();
                    if(addCal.isChecked()){
                        createCalenderEvent(eventName, eventDesc, eventAddress);
                    }
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
                if(editEventInSuburb(eventAddress, eventName, eventDesc, eventTime, eventDate, endTime, endDate, type)){
                    Toast.makeText(this, "event changed! its party time", Toast.LENGTH_LONG).show();

                    if(addCal.isChecked()){
                        createCalenderEvent(eventName, eventDesc, eventAddress);
                    }
                }else{
                    Toast.makeText(this, "sorry, something went wrong, please try again", Toast.LENGTH_LONG).show();
                }
            }
        }
        else{
            Toast.makeText(this, "please enter all fields", Toast.LENGTH_LONG).show();
        }
    }

    // Removes the event from:
    // 1. Suburb/Events 2. The host's host list 3. The atttending list of all attendees
    public void deleteEvent(){
        databaseEvent.removeValue();

        // Removes the myEvents hosting section of the user deleting this event
        DatabaseReference userEvents = databaseUsers.child(firebaseAuth.getCurrentUser().getUid()).child("myEvents");


        userEvents.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                    String id = eventSnapshot.child("id").getValue().toString();

                    if (eventId.equals(id)) {
                        eventSnapshot.getRef().removeValue();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        } );

        //Delete the reference for all attendees
        ArrayList<UserInformation> attendees = preEvent.getAttendees();
        for (UserInformation attendee: attendees){
            String attendeeString = attendee.getUid();
            DatabaseReference userEventsAttending = databaseUsers.child(attendeeString).child("myEventsAttending");
        }

    }

    // Updates the database with the changes
    public boolean editEventInSuburb(String eventAddress,String eventName,String eventDesc, String eventTime, String eventDate, String endTime, String endDate, String type){

        databaseEvent.child("address").setValue(eventAddress);
        databaseEvent.child("date").setValue(eventDate);
        databaseEvent.child("description").setValue(eventDesc);
        databaseEvent.child("date").setValue(eventDate);
        databaseEvent.child("time").setValue(eventTime);
        databaseEvent.child("endDate").setValue(endDate);
        databaseEvent.child("endTime").setValue(endTime);
        databaseEvent.child("eventName").setValue(eventName);
        databaseEvent.child("type").setValue(type);

        // result event sent back to the Event Screen
        Event resultEvent = new Event(preEvent.getId(), preEvent.getHost(), eventAddress, eventName,
                                      eventDesc, eventTime, eventDate, endTime, endDate, type,
                                      preEvent.getAttendees(), preEvent.getSuburbId());
        Intent resultIntent = new Intent();
        resultIntent.putExtra("MyObject", resultEvent);

        setResult(1, resultIntent);
        finish();
        return true;
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
    public static class TimePickerFragmentEdit extends DialogFragment implements TimePickerDialog.OnTimeSetListener{

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
            if(startAndEnd){
                if(minute < 10){
                    evTime.setText(hourOfDay + ":0"	+ minute);
                }
                else if(minute == 0){
                    evTime.setText(hourOfDay + ":00" + minute);
                }else {
                    evTime.setText(hourOfDay + ":"	+ minute);
                }
            }else{
                if(minute < 10){
                    evEndTime.setText(hourOfDay + ":0"	+ minute);
                }
                else if(minute == 0){
                    evEndTime.setText(hourOfDay + ":00" + minute);
                }else {
                    evEndTime.setText(hourOfDay + ":"	+ minute);
                }
            }
        }
    }

    public static class DatePickerFragmentEdit extends DialogFragment implements
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

    public void showTruitonTimePickerDialog(View v) {
        DialogFragment newFragment = new editDelete.TimePickerFragmentEdit();
        newFragment.show(getSupportFragmentManager(), "timePicker");
    }

    public void showTruitonDatePickerDialog(View v) {
        DialogFragment newFragment = new editDelete.DatePickerFragmentEdit();
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    // TODO: Need to add an end time to event creation
    public void createCalenderEvent(String title, String description, String eventAddress){

        java.util.Calendar beginTime = java.util.Calendar.getInstance();
        beginTime.set(year, month, day, hour, minute);
        java.util.Calendar endTime = java.util.Calendar.getInstance();
        endTime.set(year, month, day, hour, minute + 5);

        Intent intent = new Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(EXTRA_EVENT_BEGIN_TIME, beginTime.getTimeInMillis())
                .putExtra(EXTRA_EVENT_END_TIME, endTime.getTimeInMillis())
                .putExtra(CalendarContract.Events.TITLE, title)
                .putExtra(CalendarContract.Events.DESCRIPTION, description)
                .putExtra(CalendarContract.Events.EVENT_LOCATION, eventAddress);
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
                    Log.d("VALIDATOR", "We are in " + suburb.getSubName() + " You have entered: " + testedSuburb);
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
}
