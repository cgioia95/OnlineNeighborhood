package com.example.onlineneighborhood;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

public class HomeScreen extends AppCompatActivity {

    TextView suburbTextView;
    Button logoutBtn;

    private FirebaseAuth fireBaseAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);



        fireBaseAuth = FirebaseAuth.getInstance();

        suburbTextView = findViewById(R.id.textViewSuburb);

        final Button logoutBtn = findViewById(R.id.logOutBtn);

        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fireBaseAuth.signOut();
                finish();
                startActivity(new Intent(getApplicationContext(), Login.class));            }
        });


        Intent i = getIntent();
        String suburb = i.getStringExtra("SUBURB");

        suburbTextView.setText(suburb);


    }

}
