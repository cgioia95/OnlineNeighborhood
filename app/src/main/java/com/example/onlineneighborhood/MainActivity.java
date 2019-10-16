package com.example.onlineneighborhood;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.RequestQueue;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// MAIN activity where user chooses to Register or Login
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "ACTIVITY MAIN";
    // Declare simple variables
    private Button registerBtn, loginBtn;
    private EditText editTextEmail, editTextPassword;
    // Two components used to get user Location
    DatabaseReference databaseSuburb;

    private RequestQueue requestQueue;
    // FireBase Authentication
    private FirebaseAuth firebaseAuth;
    // Simple loading screen while authentication is being processed
    private ProgressDialog progressDialog;


    @Override
    protected void onStart() {
        super.onStart();

    }

    final static String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/onlineNeighbourhood/melbourneSuburbs.csv";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null) {
            finish();
            Intent i = new Intent(this, ChooseSuburb.class);
            startActivity(i);

        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editTextEmail = findViewById(R.id.username);
        editTextPassword = findViewById(R.id.password);
        databaseSuburb =  FirebaseDatabase.getInstance().getReference("suburbs");

        // Bind simple variables
        registerBtn = (Button) findViewById(R.id.registerBtn);
        loginBtn = (Button) findViewById(R.id.loginBtn);


        // Set on Click Listeners
        registerBtn.setOnClickListener(this);
        loginBtn.setOnClickListener(this);


        // Initialize the Progress Dialog and the Firebase Authenticator
        progressDialog = new ProgressDialog(this);

    }


    // Assigning functions to the two buttons
    @Override
    public void onClick(View view) {

        if (view == loginBtn) {
            userLogin();
        }

        if (view == registerBtn){
            startActivity(new Intent(this, Register.class));
        }

    }

    // Parses user information from EditTexts, checks all fields are filled
    // If all fields filled, attempts user Login
    // If successful, takes user to Choose Screen
    private void userLogin() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please enter email", Toast.LENGTH_SHORT).show();
            // Stopping this function executive further
            return;
            // email is empty
        }

        if (TextUtils.isEmpty(password)) {
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

                        if (task.isSuccessful()) {
                            //start the profile activity
                            finish();
                            Intent i = new Intent(getApplicationContext(), ChooseSuburb.class);
                            startActivity(i);
                        } else {
                            Toast.makeText(MainActivity.this, "Could not login ... please try again", Toast.LENGTH_SHORT).show();

                        }
                    }
                });

    }

}
