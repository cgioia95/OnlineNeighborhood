package com.example.onlineneighborhood;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

public class HomeScreen extends AppCompatActivity implements View.OnClickListener {

    TextView suburbTextView;

    Button logoutBtn;
    ImageView addEvent;
    String suburb;

    private FirebaseAuth fireBaseAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        fireBaseAuth = FirebaseAuth.getInstance();
        addEvent = findViewById(R.id.addEvent);

        suburbTextView = findViewById(R.id.textViewSuburb);

        Button logoutBtn = findViewById(R.id.logOutBtn);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fireBaseAuth.signOut();
                finish();
                startActivity(new Intent(getApplicationContext(), Login.class));            }
        });

        Intent i = getIntent();
        suburb = i.getStringExtra("SUBURB");

        suburbTextView.setText(suburb);
        addEvent.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        if (view == addEvent){
            Intent i = new Intent(getApplicationContext(), createEvent.class);
            i.putExtra("SUBURB", suburb);
            startActivity(i);
        }
    }
}
