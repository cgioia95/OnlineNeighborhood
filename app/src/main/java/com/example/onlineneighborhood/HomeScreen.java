package com.example.onlineneighborhood;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import java.util.ArrayList;

public class HomeScreen extends AppCompatActivity implements View.OnClickListener {

    TextView suburbTextView;

    Button logoutBtn;
   // private Button profileBtn;
    ImageView addEvent;
    String suburb;

    private FirebaseAuth fireBaseAuth;
    DatabaseReference databaseEvents;

    //Setting up recyclerview and adapter for displaying events
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArrayList<Event> eventList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        databaseEvents = FirebaseDatabase.getInstance().getReference("events");


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        fireBaseAuth = FirebaseAuth.getInstance();
        addEvent = findViewById(R.id.addEvent);

        suburbTextView = findViewById(R.id.textViewSuburb);

        Button logoutBtn = findViewById(R.id.logOutBtn);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
       // profileBtn = findViewById(R.id.profileBtn);

       // profileBtn.setOnClickListener(this);
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fireBaseAuth.signOut();
                finish();
                startActivity(new Intent(getApplicationContext(), Login.class));
            }
        });

        Intent i = getIntent();
        suburb = i.getStringExtra("SUBURB");

        suburbTextView.setText(suburb);
        addEvent.setOnClickListener(this);


    }

    @Override
    protected void onStart() {
        super.onStart();

        databaseEvents.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                eventList.clear();
                for (DataSnapshot eventSnapShot : dataSnapshot.getChildren()) {
                    Event event = eventSnapShot.getValue(Event.class);

                    eventList.add(event);
                    Log.d("DISPLAY EVENT DATE", event.getDate());

                }
                //SET UP EVENTLIST

                mRecyclerView = findViewById(R.id.recyclerView);
                mLayoutManager = new LinearLayoutManager(HomeScreen.this);
                mAdapter = new EventAdapter(eventList, HomeScreen.this);

                mRecyclerView.setLayoutManager(mLayoutManager);
                mRecyclerView.setAdapter(mAdapter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


    @Override
    public void onClick(View view) {

        if (view == addEvent){
            Intent i = new Intent(getApplicationContext(), createEvent.class);
            i.putExtra("SUBURB", suburb);
            startActivity(i);
        }

//       if(view == profileBtn){
//           startActivity(new Intent(this, ProfileScreen.class));
//       }

    }
}
