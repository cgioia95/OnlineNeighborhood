package com.example.onlineneighborhood;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

// LOGIN Activity takes user's email + password, checks these details against Firebase Authenticator
// If successful takes them to Choose screen

public class Login extends AppCompatActivity implements View.OnClickListener {


    // Declare simple variables
    private Button buttonSignIn;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private TextView textViewSignup;
    private Button btnGetLocation;
    private Button chooseBtn;
    private Spinner suburbSpinner;

    private String localSuburb = "DEFAULT_LOCAL_SUBURB";

    private double lon;
    private double lat;
    private TextView textViewSuburb;

    // Two components used to get user Location
    private LocationManager locationManager;
    private LocationListener locationListener;

    private RequestQueue requestQueue;

    // FireBase Authentication
    private FirebaseAuth firebaseAuth;

    // Simple loading screen while authentication is being processed
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        requestQueue = Volley.newRequestQueue(this);


        // Bind Simple Variables
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonSignIn = findViewById(R.id.buttonSignIn);
        textViewSignup = findViewById(R.id.textViewSignup);
        btnGetLocation = (Button) findViewById(R.id.btnGetLocation);
        chooseBtn = (Button) findViewById(R.id.chooseBtn);
        suburbSpinner = findViewById(R.id.suburbSpinner);
        textViewSuburb = findViewById(R.id.textViewSuburb);


        // Set on Click Listeners
        buttonSignIn.setOnClickListener(this);
        textViewSignup.setOnClickListener(this);
        btnGetLocation.setOnClickListener(this);
        chooseBtn.setOnClickListener(this);

        // Initialize the Progress Dialog and the Firebase Authenticator
        progressDialog = new ProgressDialog(this);
        firebaseAuth = FirebaseAuth.getInstance();

        // Checks if the User's Logged in already, if so bypasses the Login Screen and takes them to Choose Screen
        if (firebaseAuth.getCurrentUser() != null){
            finish();
            startActivity(new Intent(getApplicationContext(), HomeScreen.class));

        }

        // Setup the Location Manager and Listener
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);


        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                lon = location.getLongitude();
                lat = location.getLatitude();

                String coordinates = "Long: " + lon + "Lat: " + lat;

                Log.d("LOCATION", "Long: " + lon + "Lat: " + lat );


                localSuburb = getSuburb(lon, lat);

                Log.d("LOCATION", localSuburb );


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


    }

    // Parses user information from EditTexts, checks all fields are filled
    // If all fields filled, attempts user Login
    // If successful, takes user to Choose Screen
    private void userLogin(){
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)){
            Toast.makeText(this, "Please enter email", Toast.LENGTH_SHORT).show();
            // Stopping this function executive further
            return;
            // email is empty
        }

        if (TextUtils.isEmpty(password)){
            // password  is empty
            Toast.makeText(this, "Please enter password", Toast.LENGTH_SHORT).show();
            // Stopping this function executive further
            return;
        }

        progressDialog.setMessage("Logging in User");
        progressDialog.show();

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.dismiss();

                        if (task.isSuccessful()){
                            //start the profile activity
                            finish();
                            Intent i = new Intent(getApplicationContext(), HomeScreen.class);
                            i.putExtra("SUBURB", textViewSuburb.getText().toString());
                            startActivity(i);
                        }

                        else {
                            Toast.makeText(Login.this, "Could not login ... please try again", Toast.LENGTH_SHORT).show();

                        }
                    }
                });

    }


    private String getSuburb(double lon, double lat) {


        String suburb = "NO SUBURB FOUND";
        Geocoder geocoder = new Geocoder(Login.this, Locale.getDefault());


        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lon, 1);


            suburb = addresses.get(0).getLocality();

            Log.d("LOCATION", suburb);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return suburb;
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
    public void configureButton() {

                Log.d("LOCATION", "FETCHING LOCATION UPDATES");

                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5, locationListener);

    }



    // Assigning functions to the two buttons
    @Override
    public void onClick(View view) {
        if (view == buttonSignIn){
            userLogin();
        }

        if (view == textViewSignup){
            startActivity(new Intent(this, MainActivity.class));
        }

        if (view == btnGetLocation){

            Log.d("LOCATION", localSuburb);

            textViewSuburb.setText(localSuburb);

        }

        if (view == chooseBtn){

            textViewSuburb.setText(suburbSpinner.getSelectedItem().toString());
        }
    }
}
