package com.example.onlineneighborhood;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;

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
import java.util.ArrayList;
import java.util.List;

import static java.util.Locale.getDefault;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    String suburbName;
    Suburb suburb;
    ArrayList<Event> events;



    DatabaseReference databaseEvents;
    DatabaseReference databaseUsers;
    DatabaseReference databaseSuburb;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        databaseEvents = FirebaseDatabase.getInstance().getReference("events");
        databaseUsers = FirebaseDatabase.getInstance().getReference("Users");
        databaseSuburb =  FirebaseDatabase.getInstance().getReference("suburbs");


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */


    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                Log.d("THISISMYTAG", marker.getTitle());
            }
        });


        databaseSuburb.addValueEventListener(new ValueEventListener() {




            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                mMap.clear();


                for(DataSnapshot suburbSnapshot : dataSnapshot.getChildren()) {
                    Suburb currentSuburb = suburbSnapshot.getValue(Suburb.class);
                    Intent i = getIntent();
                    String intentSuburb = i.getStringExtra("SUBURB");
                    Log.d("SUBURB", "" + suburbSnapshot);

                    try{

                        if (intentSuburb.equals(currentSuburb.getSubName())) {

                            suburb = currentSuburb;

                            DatabaseReference databaseSuburbChange = FirebaseDatabase.getInstance().getReference("suburbs").child(suburb.getId());


                            Log.d("CHOSEN: ", "" + suburb + suburb.getSubName());

                            if ((events = suburb.getEvents()) != null){


                                for (Event event: events){

                                    String title = event.getName();
                                    String address = event.getAddress();
                                    Log.d("MAPTEST", title);
                                    Log.d("MAPTEST", address);


                                    Geocoder geocoder = new Geocoder(getApplicationContext(), getDefault());
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


                                    Marker marker = mMap.addMarker(new MarkerOptions()
                                            .position(locat)
                                            .title(event.getName())
                                            .snippet(event.getDescription()));


                                    Log.d("MAPTEST", "LONG: " +  Double.toString(longitude) + " LAT: " + Double.toString(latitude));


                                }


                            }

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
}
