package com.example.onlineneighborhood;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Profile extends AppCompatActivity {

    private FirebaseAuth fireBaseAuth;
    private TextView textViewName;
    private Spinner spinnerPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        fireBaseAuth = FirebaseAuth.getInstance();
     //   Log.d(tag:"Profile", msg: "Firebase Authentication");

        textViewName = (TextView)findViewById(R.id.textViewName);
        spinnerPreferences = (Spinner) findViewById(R.id.spinnerPreferences);


        FirebaseUser user = fireBaseAuth.getCurrentUser();
        textViewName.setText(user.getEmail());

       // final String preference = spinnerPreferences.getSelectedItem().toString();



    }
}
