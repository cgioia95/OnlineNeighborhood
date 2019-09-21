package com.example.onlineneighborhood;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.onlineneighborhood.BottomNavigationActivity;
import com.example.onlineneighborhood.Event;
import com.example.onlineneighborhood.EventAdapter;
import com.example.onlineneighborhood.HomeScreen;
import com.example.onlineneighborhood.Login;
import com.example.onlineneighborhood.R;
import com.example.onlineneighborhood.createEvent;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;

public class HomeFragment extends Fragment implements View.OnClickListener, Serializable {

    TextView suburbTextView;

    Button logoutBtn;
    // private Button profileBtn;
    ImageView addEvent;
    String suburb;
    private static final String TAG = "HomeScreen";

    private FirebaseAuth fireBaseAuth;
    DatabaseReference databaseEvents;

    //Setting up recyclerview and adapter for displaying events
    private RecyclerView mRecyclerView;
    private EventAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArrayList<Event> eventList = new ArrayList<>();
    Context applicationContext = BottomNavigationActivity.getContextOfApplication();


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.activity_home_screen, null);


        fireBaseAuth = FirebaseAuth.getInstance();
        addEvent = mView.findViewById(R.id.addEvent);
        suburbTextView = mView.findViewById(R.id.textViewSuburb);

        Button logoutBtn = mView.findViewById(R.id.logOutBtn);
//        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        // profileBtn = findViewById(R.id.profileBtn);

        // profileBtn.setOnClickListener(this);
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fireBaseAuth.signOut();
                //finish();
                startActivity(new Intent(applicationContext, Login.class));
            }
        });

        Intent i = getActivity().getIntent();
        suburb = i.getStringExtra("SUBURB");

        suburbTextView.setText(suburb);
        addEvent.setOnClickListener(this);
        return mView;
    }
    @Override
    public void onStart(){
        super.onStart();
        databaseEvents = FirebaseDatabase.getInstance().getReference("events");

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

                mRecyclerView = getActivity().findViewById(R.id.recyclerView);
                mLayoutManager = new LinearLayoutManager(getActivity());
                mAdapter = new EventAdapter(eventList, getActivity());

                mRecyclerView.setLayoutManager(mLayoutManager);
                mRecyclerView.setAdapter(mAdapter);
                mRecyclerView.setAdapter(mAdapter);

                mAdapter.setOnEventClickListener(new EventAdapter.onEventClickListener() {
                    @Override
                    public void onEventClick(int position) {
                        Event event = eventList.get(position);

                        Intent intent = new Intent(getActivity(), EventScreen.class);

                        intent.putExtra("MyObject", event);
                        startActivity(intent);
                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }



    //@Override
    public void onClick(View view) {

        if (view == addEvent){
            Intent i = new Intent(applicationContext, createEvent.class);
            i.putExtra("SUBURB", suburb);
            startActivity(i);
        }

//       if(view == profileBtn){
//           startActivity(new Intent(this, ProfileScreen.class));
//       }

    }
}
