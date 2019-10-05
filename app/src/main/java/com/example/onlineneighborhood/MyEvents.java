package com.example.onlineneighborhood;


import android.content.Context;
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

import static com.example.onlineneighborhood.editDelete.TAG;


/**
 * A simple {@link Fragment} subclass.
 */
public class MyEvents extends Fragment {

    // Current issues:
    // 1. Events not appearing after clicking out of my events and coming back

    private FirebaseAuth fireBaseAuth;
    DatabaseReference databaseUserReference, SuburbEvents;
    String userid;
    private ArrayList<Event> userMyEvents = new ArrayList<>();
    private ArrayList<Event> userMyEventsAttending = new ArrayList<>();

    private ArrayList<Event> userMyEventsToView = new ArrayList<>();
    private ArrayList<Event> userMyEventsAttendingToView = new ArrayList<>();


    //Setting up recyclerview and adapter for displaying events
    private RecyclerView mRecyclerView;
    private EventAdapter mAdapterForHosting, mAdapterForAttending;
    private RecyclerView.LayoutManager mLayoutManager;
    Context applicationContext = BottomNavigationActivity.getContextOfApplication();

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

        fireBaseAuth = FirebaseAuth.getInstance();
        userid = fireBaseAuth.getCurrentUser().getUid();
        attending = mView.findViewById(R.id.my_events_attending_button);
        hosting = mView.findViewById(R.id.my_events_hosting_button);

        return mView;
    }

    public void onStart() {
        super.onStart();

        databaseUserReference = FirebaseDatabase.getInstance().getReference("Users").child(userid);

        databaseUserReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                userMyEventsToView.clear();
                userMyEventsAttendingToView.clear();
                UserInformation user = dataSnapshot.getValue(UserInformation.class);
                //Log.d("myEvents Listener ", "test pass");
                // Set up for list of events hosting
                if (dataSnapshot.child("myEvents").exists()){
                    userMyEvents = user.getMyEvents();

                    for (Event event : userMyEvents) {
                        String suburbid = event.getSuburbId();
                        final String eventid = event.getId();

                        SuburbEvents = FirebaseDatabase.getInstance().getReference("suburbs").child(suburbid);
                        SuburbEvents.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                Suburb suburb = dataSnapshot.getValue(Suburb.class);
                                //Accounting for suburbs without existing events
                                if (suburb.getEvents() != null) {
                                    ArrayList<Event> events = suburb.getEvents();
                                    for (Event event : events) {
                                        //Accounting for deleted events
                                        if (event != null) {
                                            if (event.getId().equals(eventid)) {
                                                userMyEventsToView.add(event);
                                                //Log.d("EVENT HOSTING FOUND ", "" + event.getName());
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

                // Set up for list of events hosting
                if (dataSnapshot.child("myEventsAttending").exists()){
                    userMyEventsAttending = user.getMyEventsAttending();
                    for (Event event : userMyEventsAttending) {
                        String suburbid = event.getSuburbId();
                        final String eventid = event.getId();

                        SuburbEvents = FirebaseDatabase.getInstance().getReference("suburbs").child(suburbid);
                        SuburbEvents.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                Suburb suburb = dataSnapshot.getValue(Suburb.class);
                                //Accounting for suburbs without existing events
                                if (suburb.getEvents() != null) {
                                    ArrayList<Event> events = suburb.getEvents();
                                    for (Event event : events) {
                                        //Accounting for deleted events
                                        if (event != null) {
                                            if (event.getId().equals(eventid)) {
                                                userMyEventsAttendingToView.add(event);
                                                //Log.d("EVENT ATTENDING FOUND ", "" + event.getName());
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

                Log.d("myEvents RV ", "pass");

                // Recycler view set up and adaption
                mRecyclerView = getActivity().findViewById(R.id.recyclerView);
                mLayoutManager = new LinearLayoutManager(getActivity());
                mAdapterForHosting = new EventAdapter(userMyEventsToView, getActivity());
                //mAdapterForAttending = new EventAdapter(userMyEventsAttendingToView, getActivity());

                mRecyclerView.setLayoutManager(mLayoutManager);
                mRecyclerView.setAdapter(mAdapterForHosting);

//                mAdapterForHosting.setOnEventLongClickListener(new EventAdapter.onEventLongClickListener() {
//                    @Override
//                    public void onEventLongClick(int position) {
//
//                        Event event = userMyEventsToView.get(position);
//
//                        String hostId = event.getHost().getUid();
//
//                        String myId = fireBaseAuth.getCurrentUser().getUid();
//                        //Log.d(TAG, "Long Click");
//
//
//                        if (myId.equals(hostId)){
//                            Log.d(TAG, "Permission Granted");
//
//                            Intent intent = new Intent(applicationContext, editDelete.class);
//                            intent.putExtra("MyObject", event);
//                            intent.putExtra("SUBURB", event.getSuburbId());
//
//
//                            startActivity(intent);
//
//                        }
//
//                        else {
//                            Log.d(TAG, "Permission Denied");
//                        }
//
//
//                    }
//                });
//
//                mAdapterForHosting.setOnEventClickListener(new EventAdapter.onEventClickListener() {
//                    @Override
//                    public void onEventClick(int position) {
//                        Event event = userMyEventsToView.get(position);
//
//                        Intent intent = new Intent(getActivity(), EventScreen.class);
//
//                        Log.d(TAG, "Single Click");
//
//                        intent.putExtra("MyObject", event);
//                        intent.putExtra("SUBURB", event.getSuburbId());
//
//                        startActivity(intent);
//                    }
//                });

//                mAdapterForAttending.setOnEventClickListener(new EventAdapter.onEventClickListener() {
//                    @Override
//                    public void onEventClick(int position) {
//                        Event event = userMyEventsAttendingToView.get(position);
//
//                        Intent intent = new Intent(getActivity(), EventScreen.class);
//
//                        Log.d(TAG, "Single Click");
//
//                        intent.putExtra("MyObject", event);
//                        intent.putExtra("SUBURB", event.getSuburbId());
//
//                        startActivity(intent);
//                    }
//                });
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

//    public void onClick(View view) {
//
//        if (view == hosting){
//            mRecyclerView.setAdapter(mAdapterForHosting);
//        }
//
//        if (view == attending){
//            mRecyclerView.setAdapter(mAdapterForAttending);
//        }
//
//
//    }
}

