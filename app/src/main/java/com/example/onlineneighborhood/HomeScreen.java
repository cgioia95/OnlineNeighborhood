package com.example.onlineneighborhood;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.app.Dialog;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class HomeScreen extends AppCompatActivity implements View.OnClickListener {

    TextView suburbTextView;
    Button logoutBtn;
    ImageView addEvent;
    String suburb;

    private FirebaseAuth fireBaseAuth;
    DatabaseReference databaseEvents;

    //creating initialization variables for location when creating an event
    //long and latitude
    private double lon;
    private double lat;
    //bool check to ensure get location has been requested
    private boolean clicked = false;
    UserInformation host;

    //location variables
    private final String DEFAULT_LOCAL = "please wait a few seconds while we get your location";
    private String locat = DEFAULT_LOCAL;
    Button getLocation, createEvent;


    //TODO: add this loading dialog
    private ProgressDialog progressDialog;

    //XML variables
    EditText evName, evDesc, evAddress;
    private TextView eventTv;
    static TextView evTime, evDate;
    static int year, day, month;
    static int hour, minute;

    // Two components used to get user Location
    private LocationManager locationManager;
    private LocationListener locationListener;


    // Set up popup (dialog) screen for CreateEvent
    Dialog myDialog;

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
    protected void onStart() {
        super.onStart();

        databaseEvents.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                eventList.clear();
                for(DataSnapshot eventSnapShot : dataSnapshot.getChildren()) {
                    Event event = eventSnapShot.getValue(Event.class);
                    eventList.add(event);
                    Log.d("DISPLAY EVENT DATE", event.getDate());

                }

                //Getting current user from DB
                for(DataSnapshot userSnapshot: dataSnapshot.getChildren()){
                    UserInformation user = userSnapshot.getValue(UserInformation.class);
                    host = user;
                    Log.d("user info: ", "onDataChange: " + host.name);

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
    }


    public void ShowPopup(View v) {
        TextView txtclose;



        myDialog.setContentView(R.layout.create_event_pop_up);
        txtclose =(TextView) myDialog.findViewById(R.id.txtclose);
        txtclose.setText("X");

        databaseEvents = FirebaseDatabase.getInstance().getReference("events");

        // Bind Simple Variables
        eventTv = findViewById(R.id.eventTv);
        createEvent = findViewById(R.id.createBtn);
        getLocation = findViewById(R.id.btnGetLocation);
        evName = findViewById(R.id.eventName);
        evDesc = findViewById(R.id.eventDesc);
        evTime = findViewById(R.id.eventTime);
        evDate = findViewById(R.id.eventDate);
        evAddress = findViewById(R.id.eventAddress);

        txtclose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDialog.dismiss();
            }
        });
        myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        myDialog.show();
    }
}
