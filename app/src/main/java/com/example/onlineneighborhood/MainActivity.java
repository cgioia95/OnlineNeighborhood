package com.example.onlineneighborhood;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// MAIN activity where user chooses to Register or Login
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    // Declare simple variables
    private Button registerBtn, loginBtn, suburbBtn;
    private String suburb = "NO SUBURB FOUND";
    private Spinner suburbSpinner;
    private TextView tvSuburb;

    // Two components used to get user Location
    private LocationManager locationManager;
    private LocationListener locationListener;
    private double lon;
    private double lat;


    final static String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/onlineNeighbourhood/melbourneSuburbs.csv" ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Bind simple variables
        suburbBtn = (Button) findViewById(R.id.chooseSuburbBtn);
        registerBtn = (Button) findViewById(R.id.registerBtn);
        loginBtn = (Button) findViewById(R.id.loginBtn);
        suburbSpinner = findViewById(R.id.suburbSpinner);
        tvSuburb = findViewById(R.id.tvSuburb);
        tvSuburb.setVisibility(View.INVISIBLE);

        // Set on Click Listeners
        suburbBtn.setOnClickListener(this);
        registerBtn.setOnClickListener(this);
        loginBtn.setOnClickListener(this);


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
                suburb = getSuburb(lon, lat);


                Log.d("LOCATION", suburb );


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

            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 5, locationListener);
        }



    }

    // Assigning functions to the two buttons
    @Override
    public void onClick(View view) {

        if (view == loginBtn && !suburb.equals("NO SUBURB FOUND")) {
            Intent i = new Intent(this, Login.class);
            i.putExtra("SUBURB", suburb);
            startActivity(i);
        } else if(view == loginBtn && suburb.equals("NO SUBURB FOUND")) {
            Toast.makeText(this, "we need a suburb before you can enter", Toast.LENGTH_LONG).show();
        }

        if (view == registerBtn){
            startActivity(new Intent(this, Register.class));
        }

        if(view == suburbBtn){
            suburb = suburbSpinner.getSelectedItem().toString();
            tvSuburb.setText(suburbSpinner.getSelectedItem().toString());
            tvSuburb.setVisibility(View.VISIBLE);
        }

    }


    private String getSuburb(double lon, double lat) {


        String suburb = "NO SUBURB FOUND";
        Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());


        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lon, 1);


            suburb = addresses.get(0).getLocality();

            Log.d("LOCATION", suburb);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return suburb;
    }


}
