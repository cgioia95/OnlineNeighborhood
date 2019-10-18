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
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toolbar;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static java.util.Locale.getDefault;


public class BottomNavigationActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, HomeFragment.Callbacks {


    private GoogleMap mMap;
    public static Context contextOfApplication;
    private Menu menu;
    private ImageView profile;
    private String suburbid;
    String intentSuburb, uid;
    Suburb suburb, toolbarSub;
    ArrayList<Event> events;
    String suburbName = "LOADING";

    // Firebase reference variables
    DatabaseReference databaseEvents, databaseUsers, databaseSuburb;
    private FirebaseAuth fireBaseAuth;
    private FirebaseStorage storage;
    private StorageReference storageReference;


    public static Context getContextOfApplication()
    {
        return contextOfApplication;
    }

    @Override
    protected void onStart() {
        databaseEvents = FirebaseDatabase.getInstance().getReference("suburbs").child(suburbid);

        databaseEvents.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                toolbarSub = dataSnapshot.getValue(Suburb.class);
                getSupportActionBar().setTitle(toolbarSub.getSubName());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        super.onStart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_bottom_navigation);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        navView.setOnNavigationItemSelectedListener(this);

        if (savedInstanceState == null) {
            navView.getMenu().performIdentifierAction(R.id.navigation_home, 0);
        }

        contextOfApplication = getApplicationContext();
        databaseEvents = FirebaseDatabase.getInstance().getReference("events");
        databaseUsers = FirebaseDatabase.getInstance().getReference("Users");
        databaseSuburb =  FirebaseDatabase.getInstance().getReference("suburbs");
        fireBaseAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference=storage.getReference();
        if (fireBaseAuth.getCurrentUser() != null)
            uid = fireBaseAuth.getCurrentUser().getUid();
        suburbid = ((OnlineNeighborhood) this.getApplication()).getsuburb();
        //Setting toolbar for adding profile icon
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        profile = toolbar.findViewById(R.id.profile);
        downloadImage();
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(suburbName);

        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), UserProfile.class);
                startActivity(i);
            }

        });


    }

    private void setSupportActionBar(Toolbar toolbar) {
    }

    //Inflate toolbar menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_menu, menu);
        this.menu = menu;
        return true;
    }

    //Managing toolbar items
    public boolean onOptionsItemSelected(MenuItem item){

        return super.onOptionsItemSelected(item);
    }

    //Loading fragment above the navigation bar
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

        Fragment fragment = null;
        switch (menuItem.getItemId()){

            //On clicking button to lead to 'MyEvents'
            case R.id.navigation_events:
                fragment = new MyEvents();
                menuItem.setChecked(true);
                break;

            //On clicking events map button
            case R.id.navigation_map:
                // Obtain the SupportMapFragment and get notified when the map is ready to be used.
                menuItem.setChecked(true);
                fragment = new MapFragment();
                break;

            //On click home button, to show active list of checked in suburb's events
            case R.id.navigation_home:
                menuItem.setChecked(true);
                Log.d("Bottom Navigation", "onNavigationItemSelected: Home");
                fragment= new HomeFragment();
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

        //Zoom into Melbourne as standard for now
        LatLng MELBOURNE = new LatLng(-37.814, 144.96);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(MELBOURNE, 10));

        databaseSuburb.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                mMap.clear();
                for (DataSnapshot suburbSnapshot : dataSnapshot.getChildren()) {
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
                        //This catches null pointer exceptions - it happens a lot
                        //TODO: I need to find a better way to loop through all the suburbs
                        //If you look at the log you can see the 'null pointer' still gets the suburb name. weird.
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

    protected void downloadImage(){

        storageReference.child("profilePics/" + uid).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                //Got the download URL for 'users/me/profile.png' in uri
                Log.d("Bottom Navigation", "DOWNLOAD URL: " + uri.toString());
                Picasso.get().load(uri).into(profile);
                return;

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                //Handle any errors
                Log.d("Bottom Navigation", "DOWNLOAD URL: FAILURE");
                storageReference.child("profilePics/" + "default.png").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        // Got the download URL for 'users/me/profile.png' in uri
                        Log.d("Bottom Navigation", "DOWNLOAD URL: " + uri.toString());
                        Picasso.get().load(uri).into(profile);
                        return;
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle any errors
                        Log.d("Bottom Navigation", "DOWNLOAD URL: FAILURE");
                    }
                });
            }
        });
    }


    @Override
    public void onButtonClicked() {

        Bundle bundle = new Bundle();
        bundle.putString("SUBURB", intentSuburb);
        HomeFragment home = new HomeFragment();
        home.setArguments(bundle);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, home)
                .commit();
    }
}
