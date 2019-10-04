package com.example.onlineneighborhood;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
public class MyEvents extends Fragment {

    private FirebaseAuth fireBaseAuth;
    DatabaseReference myEvents, SuburbEvents;
    String userid;
    private ArrayList<Event> eventList = new ArrayList<>();
    private String suburbid;
    String eventid;


    public MyEvents() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fireBaseAuth = FirebaseAuth.getInstance();
        userid = fireBaseAuth.getCurrentUser().toString();
        return inflater.inflate(R.layout.fragment_my_events, container, false);
    }

    public void onStart() {
        super.onStart();

        myEvents = FirebaseDatabase.getInstance().getReference("Users").child(userid);

        myEvents.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserInformation user = dataSnapshot.getValue(UserInformation.class);
                eventList = user.getMyEvents();
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        for(Event event: eventList){
            suburbid = event.getSuburbId();
            eventid = event.getId();
            SuburbEvents = FirebaseDatabase.getInstance().getReference("Suburbs").child(suburbid);
            SuburbEvents.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Suburb suburb = dataSnapshot.getValue(Suburb.class);
                    //make sure you acount for nullpointers
                    ArrayList<Event> events = suburb.getEvents();
                    for(Event event : events){
                        if(event.getId().equals(eventid)){
                            //add to event list that you will show on the recycler view.
                            Log.d("EVENT FOUND: ", ""+event.getName());
                        }
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }
}

