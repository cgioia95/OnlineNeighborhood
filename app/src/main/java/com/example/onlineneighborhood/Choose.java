package com.example.onlineneighborhood;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.IOException;
import java.util.List;
import java.util.Locale;


public class Choose extends AppCompatActivity implements View.OnClickListener {

    // declare simple variables
    private Button logOutBtn;
    private Button btnGetLocation;
    private double lon;
    private double lat;
    private TextView textViewSuburb;

    // Two components used to get user Location
    private LocationManager locationManager;
    private LocationListener locationListener;

    // Firebase Authentication
    private FirebaseAuth fireBaseAuth;

    private RequestQueue requestQueue;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose);

        requestQueue = Volley.newRequestQueue(this);

        // Bind simple variables
        btnGetLocation = (Button) findViewById(R.id.btnGetLocation);
        logOutBtn = (Button) findViewById(R.id.logOutBtn);
        textViewSuburb = findViewById(R.id.textViewSuburb);


        // Setup the Location Manager and Listener
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);


        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                lon = location.getLongitude();
                lat = location.getLatitude();

                String coordinates = "Long: " + lon + "Lat: " + lat;

                Log.d("LOCATION", "Long: " + lon + "Lat: " + lat );


                String suburb = getSuburb(lon, lat);

                textViewSuburb.setText(suburb);

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
            configureButton();
        }


        // Checks to see if the User is logged in at this point, if not ,returns them to the Login Screen
        fireBaseAuth = FirebaseAuth.getInstance();

        if (((FirebaseAuth) fireBaseAuth).getCurrentUser() == null){
            finish();
            startActivity(new Intent(this, Login.class));
        }

        // Sets on click listener to the logout button
        logOutBtn.setOnClickListener(this);



    }

    private String getSuburb(double lon, double lat) {


        String suburb = "NO SUBURB FOUND";
        Geocoder geocoder = new Geocoder(Choose.this, Locale.getDefault());


        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lon, 1);

//            suburb = addresses.get(0).getAddressLine(0);

            suburb = addresses.get(0).getLocality();

                    Log.d("LOCATION", suburb);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return suburb;
    }

    @Override
    public void onClick(View view) {

        if (view == logOutBtn){
            fireBaseAuth.signOut();
            finish();
            startActivity(new Intent(this, Login.class));
        }

    }

    // Parses the results of requesting permission to see if permission was granted
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case 10:
                if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    configureButton();
                return;
        }
    }

    // Once the Location Button is pressed, the location manager will start returning user's location
    private void configureButton() {

        btnGetLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                locationManager.requestLocationUpdates("gps", 5000, 5, locationListener);

            }
        });


    }






}
