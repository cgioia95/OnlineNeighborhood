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
import android.widget.TextView;

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

    // Auxiliary lists for retrieving final 'Attending' and 'Hosting' lists for user
    private ArrayList<Event> userMyEvents = new ArrayList<>();
    private ArrayList<Event> userMyEventsAttending = new ArrayList<>();

    // Final 'Attending' and 'Hosting' lists for user
    private ArrayList<Event> userHostingList = new ArrayList<>();
    private ArrayList<Event> userAttendingList = new ArrayList<>();

    // Setting up recycler-view and adapter for displaying events
    private RecyclerView mRecyclerView;
    private EventAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    Button attending;
    Button hosting;

    public MyEvents() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View mView = inflater.inflate(R.layout.activity_my_events, null);

        // Set 'Attending' and 'Hosting' buttons
        attending = mView.findViewById(R.id.my_events_attending_button);
        attending.setOnClickListener(this);
        hosting = mView.findViewById(R.id.my_events_hosting_button);
        hosting.setOnClickListener(this);

        return mView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fireBaseAuth = FirebaseAuth.getInstance();
        userID = fireBaseAuth.getCurrentUser().getUid();
        databaseUserReference = FirebaseDatabase.getInstance().getReference("Users").child(userID);

        databaseUserReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                userHostingList.clear();
                userAttendingList.clear();
                UserInformation user = dataSnapshot.getValue(UserInformation.class);

                // Retrieve user's 'Hosting' list
                if (dataSnapshot.child("myEvents").exists()){
                    userMyEvents = user.getMyEvents();

                    for (Event event : userMyEvents) {
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
                                                userHostingList.add(event);
                                            }
                                        }
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                }

                // Retrieve user's 'Attending' list
                if (dataSnapshot.child("myEventsAttending").exists()){
                    userMyEventsAttending = user.getMyEventsAttending();
                    for (Event event : userMyEventsAttending) {
                        String suburbid = event.getSuburbId();
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
                                                userAttendingList.add(event);
                                            }
                                        }
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                    }
                }

                mRecyclerView = getActivity().findViewById(R.id.recyclerView);
                mLayoutManager = new LinearLayoutManager(getActivity());
                mAdapter = new EventAdapter(userHostingList, getActivity());
                mRecyclerView.setLayoutManager(mLayoutManager);
                mRecyclerView.setAdapter(mAdapter);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onStart() {

        super.onStart();

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.my_events_attending_button:
                mAdapter = new EventAdapter(userAttendingList, getActivity());
                mRecyclerView.setLayoutManager(mLayoutManager);
                mRecyclerView.setAdapter(mAdapter);

                mAdapter.setOnEventClickListener(new EventAdapter.onEventClickListener() {
                    @Override
                    public void onEventClick(int position) {
                        Event event = userAttendingList.get(position);

                        Intent intent = new Intent(getActivity(), EventScreen.class);

                        Log.d("MyEvents", "Test Click");

                        intent.putExtra("MyObject", event);
                        intent.putExtra("SUBURB", event.getSuburbId());

                        startActivity(intent);
                    }


                });

                break;

            case R.id.my_events_hosting_button:
                mAdapter = new EventAdapter(userHostingList, getActivity());
                mRecyclerView.setLayoutManager(mLayoutManager);
                mRecyclerView.setAdapter(mAdapter);

                mAdapter.setOnEventClickListener(new EventAdapter.onEventClickListener() {
                    @Override
                    public void onEventClick(int position) {
                        Event event = userHostingList.get(position);

                        Intent intent = new Intent(getActivity(), EventScreen.class);

                        Log.d("MyEvents", "Test Click");

                        intent.putExtra("MyObject", event);
                        intent.putExtra("SUBURB", event.getSuburbId());

                        startActivity(intent);
                    }


                });
                break;

            default:
                break;
        }

    }
}

