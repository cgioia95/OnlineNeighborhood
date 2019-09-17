package com.example.onlineneighborhood;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;

// MAIN activity where user chooses to Register or Login
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    // Declare simple variables
    private Button registerBtn;
    private Button loginBtn;
    private Button profileBtn;
    private FirebaseAuth firebaseAuth;
    DatabaseReference databaseEvents;
    final static String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/onlineNeighbourhood/melbourneSuburbs.csv" ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Bind simple variables
        registerBtn = (Button) findViewById(R.id.registerBtn);
        loginBtn = (Button) findViewById(R.id.loginBtn);

        // Set on Click Listeners
        registerBtn.setOnClickListener(this);
        loginBtn.setOnClickListener(this);

    }

    // Assigning functions to the two buttons
    @Override
    public void onClick(View view) {

        if (view == registerBtn){
            startActivity(new Intent(this, Register.class));
        }

        if (view == loginBtn){
            startActivity(new Intent(this, Login.class));
        }

    }


    private void readSuburbData() {
        InputStream is = getResources().openRawResource(R.raw.melbourne_suburbs);
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(is, Charset.forName("UTF-8"))
        );

        String line = "";
        try {
            int i = 0;
            while ((line = reader.readLine()) != null) {
                //spilt by ","
                String [] tokens = line.split(",");

                //read the data
                System.out.println(tokens[0]);
                String name = tokens[0];
                String postCode = tokens[1];


                String id = databaseEvents.push().getKey();
                ArrayList<Event> events = new ArrayList<Event>();
                Suburb suburb = new Suburb(id, name, postCode, events);
                databaseEvents.child(id).setValue(suburb);

            }
        } catch (IOException e){
            Log.wtf("CSV Read", "error reading data file on line" + line, e);
            e.printStackTrace();
        }


    }

}
