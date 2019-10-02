package com.example.onlineneighborhood;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toolbar;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static java.util.Locale.getDefault;


public class BottomNavigationActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {


    private GoogleMap mMap;
    String suburbName;
    Suburb suburb;
    ArrayList<Event> events;
    DatabaseReference databaseEvents;
    DatabaseReference databaseUsers;
    DatabaseReference databaseSuburb;
    private FirebaseAuth fireBaseAuth;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private String uid;
    public static Context contextOfApplication;

    public static Context getContextOfApplication()
    {
        return contextOfApplication;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_bottom_navigation);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        navView.setOnNavigationItemSelectedListener(this);
        contextOfApplication = getApplicationContext();

        databaseEvents = FirebaseDatabase.getInstance().getReference("events");
        databaseUsers = FirebaseDatabase.getInstance().getReference("Users");
        databaseSuburb =  FirebaseDatabase.getInstance().getReference("suburbs");
        fireBaseAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference=storage.getReference();
        if (fireBaseAuth.getCurrentUser() != null)
            uid = fireBaseAuth.getCurrentUser().getUid();


        //Setting toolbar for adding profile icon
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Online Neighborhood");


        //To load home screen automatically after logging in
        String intentSuburb = ((OnlineNeighborhood) this.getApplication()).getsuburb();
        Bundle bundle = new Bundle();
        bundle.putString("SUBURB", intentSuburb);
        HomeFragment home = new HomeFragment();
        home.setArguments(bundle);
        loadFragment(home);


    }

    private void setSupportActionBar(Toolbar toolbar) {
    }

    //inflate toolbar menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_menu, menu);
        return true;
    }


    //managing toolbar items
    public boolean onOptionsItemSelected(MenuItem item){
        if(item.getItemId() == R.id.profile){

            Intent i = new Intent(getApplicationContext(), UserProfile.class);
            startActivity(i);
        }
        return super.onOptionsItemSelected(item);
    }

    //loading fragment above the navigation bar
    private boolean loadFragment(Fragment fragment){


       if(fragment != null){

           getSupportFragmentManager()
                   .beginTransaction()
                   .replace(R.id.fragment_container,fragment).commit();
       }
       return false;
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

        Intent i = getIntent();
        String intentSuburb = i.getStringExtra("SUBURB");
        Bundle bundle = new Bundle();
        bundle.putString("SUBURB", intentSuburb);
        HomeFragment home = new HomeFragment();
        home.setArguments(bundle);

        Fragment fragment = null;
        switch (menuItem.getItemId()){

            case R.id.navigation_events:
//              Add "My events" fragment here
                break;


            case R.id.navigation_map:
                // Obtain the SupportMapFragment and get notified when the map is ready to be used.

                fragment = new MapFragment();
                break;

            case R.id.navigation_home:
                fragment= home;
                break;

        }
        return loadFragment(fragment);
    }



    //Handling Maps
    //@Override
    public void onMapReady(GoogleMap googleMap) {

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
                                    String date = event.getDate();


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

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }



}
