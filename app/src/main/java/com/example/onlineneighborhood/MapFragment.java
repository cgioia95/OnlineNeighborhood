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
    private GoogleMap mMap;

    SupportMapFragment mapFragment;

    String suburbName;
    Suburb suburb;
    ArrayList<Event> events;

    String filterType = "NO_FILTER";

    DatabaseReference databaseEvents;
    DatabaseReference databaseUsers;
    DatabaseReference databaseSuburb;

    ArrayList<Marker> defaultMarkers;


    Button filterButton, todayFilterButton, dateButton, calenderFilterButton;
    EditText editTextDays;

    Date todayDate;
    Date newDate;
    static Date calenderDate;



    HashMap<String, Event> markerToEvent = new HashMap<String, Event>();
    HashMap<String, Marker> markerIDtoMarker = new HashMap<String, Marker>();


    public MapFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {



        Intent i = getActivity().getIntent();
        suburbName = i.getStringExtra("SUBURB");

        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_map, container, false);
        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);

        databaseEvents = FirebaseDatabase.getInstance().getReference("events");
        databaseUsers = FirebaseDatabase.getInstance().getReference("Users");
        databaseSuburb =  FirebaseDatabase.getInstance().getReference("suburbs").child(suburbName);

        todayDate = new Date();
        int year = todayDate.getYear();
        int month = todayDate.getMonth();
        int date = todayDate.getDate();
        todayDate = new Date(year, month, date);

        defaultMarkers = new ArrayList<Marker>();

        Log.d("MAGFRAGMENT",""+ suburbName);

        if (mapFragment == null){
            FragmentManager fm = getFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            mapFragment = SupportMapFragment.newInstance();
            ft.replace(R.id.map, mapFragment).commit();

        }



        databaseSuburb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Suburb currentSuburb = dataSnapshot.getValue(Suburb.class);
                    suburb = currentSuburb;

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

        todayFilterButton = getView().findViewById(R.id.todayFilterButton);

        dateButton = getView().findViewById(R.id.dateButton);

        calenderFilterButton = getView().findViewById(R.id.calenderFilterButton);




        todayFilterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                filterType = "TODAY_FILTER";

                Log.d( "TODAYDATE" , todayDate.toString());

                for (HashMap.Entry<String,Event> entry : markerToEvent.entrySet()) {

                    Log.d("MAPTEST" , entry.getValue().getName());
                    Log.d("MAPTEST" , entry.getValue().getDate());

                    String stringDate = entry.getValue().getDate();

                    try {
                        Date testedDate =new SimpleDateFormat("dd/MM/yyyy").parse(stringDate);
                        Log.d("TESTED DATE" , testedDate.toString());

                        if (todayDate.compareTo(testedDate) != 0){

                            markerIDtoMarker.get(entry.getKey()).setVisible(false);
                            Log.d("MATCHTEST" , "MATCH");
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

        dateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                DialogFragment newFragment = new SelectDateFragment();
                newFragment.show(getFragmentManager(), "DatePicker");

            }
        });

        calenderFilterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (calenderDate != null){

                    filterType = "CALENDER_FILTER";

                    Log.d("CALENDER FILTER BUTTON", calenderDate.toString());

                    for (Marker marker: defaultMarkers){
                        marker.setVisible(true);
                    }

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

            }
        });

        mMap = googleMap;



        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                Log.d("THISISMYTAG", marker.getTitle());
            }
        });

        LatLng MELBOURNE = new LatLng(-37.814, 144.96);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(MELBOURNE, 10));



        databaseSuburb.addValueEventListener(new ValueEventListener() {


            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                mMap.clear();

                Date todayDate = new Date();
                int year = todayDate.getYear();
                int month = todayDate.getMonth();
                int date = todayDate.getDate();
                todayDate = new Date(year, month, date);

                Log.d("SNAPSHOT" , dataSnapshot.toString());

                Log.d("SNAPSHOT" , String.valueOf(dataSnapshot.child("events")));

                Suburb suburb = dataSnapshot.getValue(Suburb.class);

                            if ((events = suburb.getEvents()) != null){

                                for (final Event event: events) {


                                    if (event != null) {

                                        String title = event.getName();
                                        String address = event.getAddress();
                                        String stringDate = event.getDate();


                                        Log.d("MAPTEST", title);
                                        Log.d("MAPTEST", address);


                                        Geocoder geocoder = new Geocoder(getActivity(), getDefault());
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


                                        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                                            @Override
                                            public void onInfoWindowClick(Marker marker) {

                                                Intent intent = new Intent(getActivity(), EventScreen.class);

                                                intent.putExtra("MyObject", event);
                                                intent.putExtra("SUBURB", suburbName );
                                                startActivity(intent);


                                            }
                                        });


                                        Log.d("MAPTEST", "LONG: " + Double.toString(longitude) + " LAT: " + Double.toString(latitude));
                                        
                                    }
                                }
                            }

                        }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }

        });


                }

    // NEED TO REFERENCE THIS
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
            Log.d("CALENDER DATE POPULATE" , calenderDate.toString());

            return calenderDate;
        }

    }

            }




