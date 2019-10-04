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
    DatabaseReference myEvents;
    String userid;
    private ArrayList<Event> eventList = new ArrayList<>();

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

    public void onStart(){
        super.onStart();

       // mySuburbId = FirebaseDatabase.getInstance().getReference("Users").child(userid);
        myEvents = FirebaseDatabase.getInstance().getReference("Users").child(userid);

        myEvents.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Suburb currSuburb = dataSnapshot.getValue(Suburb.class);
                if(currSuburb != null){
                    ArrayList<Event> events = currSuburb.getEvents();
                    for(Event event:events){
                        if(event != null) {
                            Log.d(TAG, "HOST ID: "+event.getHost());
                            eventList.add(event);
                        }
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }
