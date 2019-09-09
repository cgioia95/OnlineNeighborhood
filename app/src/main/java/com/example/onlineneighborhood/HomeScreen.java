package com.example.onlineneighborhood;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class HomeScreen extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private ArrayList<EventCard> eventList = new ArrayList<>();

    TextView suburbTextView;

    Button logoutBtn;

    private FirebaseAuth fireBaseAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        fireBaseAuth = FirebaseAuth.getInstance();

        suburbTextView = findViewById(R.id.textViewSuburb);

        Button logoutBtn = findViewById(R.id.logOutBtn);

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

        //SETTING UP EVENT LIST

        eventList.add(new EventCard("beerhangout", "emma"));
        eventList.add(new EventCard("beerhangout", "emma"));

        mRecyclerView = findViewById(R.id.recyclerView);
        mLayoutManager = new LinearLayoutManager(this);
        mAdapter = new EventAdapter(eventList, this);

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);


    }
}
