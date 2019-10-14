package com.example.onlineneighborhood;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class MyEvents extends Fragment implements View.OnClickListener {

    private FirebaseAuth fireBaseAuth;
    DatabaseReference databaseUserReference, suburbEvents;
    String userID;
    private static final String TAG = "MyEvents";

    // Auxiliary lists for retrieving final 'Attending' and 'Hosting' lists for user
    private ArrayList<Event> userMyEvents = new ArrayList<>();
    private ArrayList<Event> userMyEventsAttending = new ArrayList<>();

    // Variables recycler-view and adapter for displaying events
    private RecyclerView mRecyclerView;
    private MyEventAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    Button attending;
    Button hosting;

    public MyEvents() {
        // Required empty public constructor
    }

    @Override
    public void onStart() {
        super.onStart();

        // Retrieve 'hosting' list and display it on default
        getDataHost();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View mView = inflater.inflate(R.layout.fragment_my_events, null);

        // Set 'Attending' and 'Hosting' buttons
        attending = mView.findViewById(R.id.my_events_attending_button);
        attending.setOnClickListener(this);
        hosting = mView.findViewById(R.id.my_events_hosting_button);
        hosting.setOnClickListener(this);

        mRecyclerView = mView.findViewById(R.id.recyclerView);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mAdapter = new MyEventAdapter(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
        return mView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Firebase initialisation references
        fireBaseAuth = FirebaseAuth.getInstance();
        userID = fireBaseAuth.getCurrentUser().getUid();
        databaseUserReference = FirebaseDatabase.getInstance().getReference("Users").child(userID);

    }

    public void getDataHost() {
        databaseUserReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mAdapter.clearData();
                userMyEvents.clear();
                userMyEventsAttending.clear();

                UserInformation user = dataSnapshot.getValue(UserInformation.class);

                if (dataSnapshot.child("myEvents").exists()){
                    userMyEvents = user.getMyEvents();

                    for (Event event : userMyEvents) {
                        if (event != null) {
                            String suburbID = event.getSuburbId();
                            final String eventID = event.getId();

                            // Retrieve full event details from 'Suburbs'
                            suburbEvents = FirebaseDatabase.getInstance().getReference("suburbs").child(suburbID);
                            suburbEvents.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    Suburb suburb = dataSnapshot.getValue(Suburb.class);
                                    // Accounting for suburbs without existing events
                                    if (suburb.getEvents() != null) {
                                        ArrayList<Event> events = suburb.getEvents();
                                        for (Event event : events) {
                                            //Accounting for deleted events
                                            if (event != null) {
                                                if (event.getId().equals(eventID)) {
                                                    event.setSuburbId(suburb.getId());
                                                    mAdapter.addDataAndUpdate(event);

                                                }
                                            }
                                        }
                                        // Allows for clicking into an event after short click
                                        mAdapter.setOnEventClickListener(new MyEventAdapter.onEventClickListener() {
                                            @Override
                                            public void onEventClick(int position) {
                                                Log.d(TAG, "onEventClick: " + position);
                                                Event event = mAdapter.getEventList().get(position);
                                                Intent intent = new Intent(getActivity(), EventScreen.class);
                                                intent.putExtra("MyObject", event);
                                                intent.putExtra("SUBURB", event.getSuburbId());
                                                startActivity(intent);
                                            }


                                        });
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public void getAttendingData() {

        databaseUserReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                mAdapter.clearData();
                userMyEvents.clear();
                userMyEventsAttending.clear();;

                UserInformation user = dataSnapshot.getValue(UserInformation.class);

                // Retrieve user's 'Attending' list
                if (dataSnapshot.child("myEventsAttending").exists()){
                    userMyEventsAttending = user.getMyEventsAttending();
                    for (Event event : userMyEventsAttending) {

                        if(event!=null)  {
                            Log.d(TAG, "onDataChange: event" + event);
                            String suburbid = event.getSuburbId();
                            Log.d(TAG, "onDataChange: suburbid" + suburbid);

                            final String eventid = event.getId();

                            // Retrieve full event details from 'Suburbs'
                            suburbEvents = FirebaseDatabase.getInstance().getReference("suburbs").child(suburbid);
                            suburbEvents.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    Suburb suburb = dataSnapshot.getValue(Suburb.class);
                                    // Accounting for suburbs without existing events
                                    if (suburb.getEvents() != null) {
                                        ArrayList<Event> events = suburb.getEvents();
                                        for (Event event : events) {
                                            //Accounting for deleted events
                                            if (event != null && event.getId().equals(eventid)) {
                                                // Only add events not being hosted by the user
                                                String hostID = event.getHost().getUid();
                                                if (!hostID.equals(userID)) {
                                                    mAdapter.addDataAndUpdate(event);
                                                }
                                            }
                                        }
                                        // Allows for clicking into an event after short click
                                        mAdapter.setOnEventClickListener(new MyEventAdapter.onEventClickListener() {
                                            @Override
                                            public void onEventClick(int position) {
                                                Log.d(TAG, "onEventClick: " + position);
                                                Event event = mAdapter.getEventList().get(position);
                                                Intent intent = new Intent(getActivity(), EventScreen.class);
                                                intent.putExtra("MyObject", event);
                                                intent.putExtra("SUBURB", event.getSuburbId());
                                                startActivity(intent);
                                            }


                                        });
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    //Buttons to switch between viewing hostdata and attending data
    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.my_events_attending_button:
                getAttendingData();
                break;

            case R.id.my_events_hosting_button:
                getDataHost();
                break;

            default:
                break;
        }
    }
}
