package com.example.onlineneighborhood;


import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.lang.reflect.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Locale.getDefault;


/**
 * A simple {@link Fragment} subclass.
 */
public class MapFragment extends Fragment implements OnMapReadyCallback {

    // Google Maps Objects
    private GoogleMap mMap;
    SupportMapFragment mapFragment;


    // Variables for accessing suburb we're currently in
    String suburbName;
    String currentSuburbName;
    Suburb suburb;

    // List of event's we're displaying
    ArrayList<Event> events;

    // Default filter type
    static String filterType = "NO_FILTER";

    // FireBase Database References
    DatabaseReference databaseEvents;
    DatabaseReference databaseUsers;
    DatabaseReference databaseSuburb;

    // Default list of markers
    static ArrayList<Marker> defaultMarkers;

    // Declare the buttons and
    Button  todayFilterButton, dateButton;

    // Declare the dates used
    Date todayDate;
    static Date calenderDate;


    // HashMaps the link markers to events, and marker's ids to markers
    static HashMap<String, Event> markerToEvent = new HashMap<String, Event>();
    static HashMap<String, Marker> markerIDtoMarker = new HashMap<String, Marker>();


    public MapFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Retrieve the global suburb variable
        suburbName = ((OnlineNeighborhood) getActivity().getApplication()).getsuburb();

        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_map, container, false);
        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);

        // Accessing all events, user information and our specific suburb
        databaseEvents = FirebaseDatabase.getInstance().getReference("events");
        databaseUsers = FirebaseDatabase.getInstance().getReference("Users");
        databaseSuburb =  FirebaseDatabase.getInstance().getReference("suburbs").child(suburbName);

        // Retrieve today's date
        todayDate = new Date();
        int year = todayDate.getYear();
        int month = todayDate.getMonth();
        int date = todayDate.getDate();
        todayDate = new Date(year, month, date);

        // Initialize the markers list
        defaultMarkers = new ArrayList<Marker>();

        // Ensure mapFragment is not null
        if (mapFragment == null){
            FragmentManager fm = getFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            mapFragment = SupportMapFragment.newInstance();
            ft.replace(R.id.map, mapFragment).commit();

        }


        // Retrieve the suburb name, based on the suburbID
        databaseSuburb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Suburb currentSuburb = dataSnapshot.getValue(Suburb.class);
                    suburb = currentSuburb;
                    currentSuburbName = currentSuburb.getSubName();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



        mapFragment.getMapAsync(this);
        return  v;
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {

        // Bind our buttons

        todayFilterButton = getView().findViewById(R.id.todayFilterButton);

        dateButton = getView().findViewById(R.id.dateButton);




        // Set OnClick Listener to todayFilter
        todayFilterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Set filter type to TODAY
                filterType = "TODAY_FILTER";

                // Cycle through the markerToEvent map
                // Test the events to ensure they're on today's date
                for (HashMap.Entry<String,Event> entry : markerToEvent.entrySet()) {

                    String stringDate = entry.getValue().getDate();

                    // If today's date, set marker to visible, otherwise invisible
                    try {
                        Date testedDate =new SimpleDateFormat("dd/MM/yyyy").parse(stringDate);

                        if (todayDate.compareTo(testedDate) != 0){

                            markerIDtoMarker.get(entry.getKey()).setVisible(false);
                        }

                        if (todayDate.compareTo(testedDate) == 0 ){
                            markerIDtoMarker.get(entry.getKey()).setVisible(true);

                        }


                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                }

            }
        });

        // Opens up the Date Picker fragment for filtering over a specific day
        dateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                DialogFragment newFragment = new SelectDateFragment();
                newFragment.show(getFragmentManager(), "DatePicker");

            }
        });

        mMap = googleMap;



        // Default location set to Melbourne

        LatLng MELBOURNE = new LatLng(-37.814, 144.96332);

        // Retrieve the coordinates of the suburb we're in
        LatLng SUBURB = getSuburbLocat();

        // On startup, zoom to the suburb we're in
        if (SUBURB != null){
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(SUBURB, 13));
        }

        else {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(MELBOURNE, 10));


        }


        // Running constantly to test what event's are in our inspected suburgb
        databaseSuburb.addValueEventListener(new ValueEventListener() {


            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                // Every time there's a change, clear the map and start from fresh
                mMap.clear();

                // Retrieve today's date
                Date todayDate = new Date();
                int year = todayDate.getYear();
                int month = todayDate.getMonth();
                int date = todayDate.getDate();
                todayDate = new Date(year, month, date);


                // Retrieve all the events in the suburb
                Suburb suburb = dataSnapshot.getValue(Suburb.class);

                            if ((events = suburb.getEvents()) != null){

                                for (final Event event: events) {


                                    if (event != null) {

                                        String title = event.getName();
                                        String address = event.getAddress();
                                        String stringDate = event.getDate();


                                        Geocoder geocoder;
                                        if(getActivity() != null) {
                                            geocoder = new Geocoder(getActivity(), getDefault());

                                            // Convert address to coordiantes
                                            List<Address> addresses = null;
                                            try {
                                                addresses = geocoder.getFromLocationName(address, 1);
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                            Address add = addresses.get(0);
                                            double longitude = add.getLongitude();
                                            double latitude = add.getLatitude();

                                            LatLng locat = new LatLng(latitude, longitude);

                                            // Places markers on the map basedd on the current filter type
                                            // Never allows past events to be marked
                                            try {
                                                Date testedDate = new SimpleDateFormat("dd/MM/yyyy").parse(stringDate);
                                                Log.d("TESTED DATE", testedDate.toString());

                                                if (!testedDate.before(todayDate)) {

                                                    Marker marker = mMap.addMarker(new MarkerOptions()
                                                            .position(locat)
                                                            .title(event.getName())
                                                            .snippet(event.getDescription())
                                                    );


                                                    String markerId = marker.getId();

                                                    markerToEvent.put(markerId, event);

                                                    defaultMarkers.add(marker);

                                                    markerIDtoMarker.put(markerId, marker);

                                                    if (filterType.equals("TODAY_FILTER")) {

                                                        if (!(testedDate.compareTo(todayDate) == 0)) {
                                                            marker.setVisible(false);
                                                        }

                                                    } else if (filterType.equals("CALENDER_FILTER")) {

                                                        if (testedDate.compareTo(calenderDate) != 0) {

                                                            marker.setVisible(false);

                                                        }
                                                    }


                                                }


                                            } catch (ParseException e) {
                                                e.printStackTrace();
                                            }

                                            // Sets onClickListener to marker's info window, sending them to the
                                            // event screen
                                            mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                                                @Override
                                                public void onInfoWindowClick(Marker marker) {

                                                    Intent intent = new Intent(getActivity(), EventScreen.class);

                                                    intent.putExtra("MyObject", event);
                                                    intent.putExtra("SUBURB", suburbName);
                                                    startActivity(intent);


                                                }
                                            });


                                        }
                                    }
                                }
                            }

                        }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }

        });



    }

    // Get coordiantes of the suburb we're in
    public LatLng getSuburbLocat() {

        Geocoder coder = new Geocoder(getContext());
        List<Address> address;
        LatLng LatLan= null;

        try {
            // May throw an IOException
            address = coder.getFromLocationName(currentSuburbName + " Melbourne", 5);
            if (address == null) {
                return null;
            }

            Address location = address.get(0);

            LatLan= new LatLng(location.getLatitude(), location.getLongitude() );

        } catch (IOException ex) {

            ex.printStackTrace();
        }

        return LatLan;
    }


    // DatePiker fragment that pops up when selecting a date
    public static class SelectDateFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar calendar = Calendar.getInstance();
            int yy = calendar.get(Calendar.YEAR);
            int mm = calendar.get(Calendar.MONTH);
            int dd = calendar.get(Calendar.DAY_OF_MONTH);
            return new DatePickerDialog(getActivity(), this, yy, mm, dd);
        }

        public void onDateSet(DatePicker view, int yy, int mm, int dd) {
            populateSetDate(yy, mm+1, dd);
        }
        public Date populateSetDate(int year, int month, int day) {

            calenderDate = new Date(year - 1900, month - 1, day);

            // If calender date chosen, set filter to calender filtering

            if (calenderDate != null){

                filterType = "CALENDER_FILTER";

                for (Marker marker: defaultMarkers){
                    marker.setVisible(true);
                }

                // Cycles through all events, if not the date selected, set to invisible
                for (HashMap.Entry<String,Event> entry : markerToEvent.entrySet()) {


                    String stringDate = entry.getValue().getDate();

                    try {
                        Date testedDate =new SimpleDateFormat("dd/MM/yyyy").parse(stringDate);

                        if (calenderDate.compareTo(testedDate) != 0 ){

                            markerIDtoMarker.get(entry.getKey()).setVisible(false);
                        }


                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                }

            }

            return calenderDate;
        }

    }

            }




