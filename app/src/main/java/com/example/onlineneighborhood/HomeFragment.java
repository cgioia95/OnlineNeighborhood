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

    //Button logoutBtn;
    // private Button profileBtn;
    ImageView addEvent;
    ImageView filterButton;
    String suburb;
    String currSuburb;
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
        if(getArguments() != null){
            currSuburb = getArguments().getString("SUBURB");

        }



        fireBaseAuth = FirebaseAuth.getInstance();
        addEvent = mView.findViewById(R.id.addEvent);
        suburbTextView = mView.findViewById(R.id.textViewSuburb);
        filterButton = mView.findViewById(R.id.buttonFilter);

//
//        Button logoutBtn = mView.findViewById(R.id.logOutBtn);
//
//        logoutBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                fireBaseAuth.signOut();
//                //finish();
//                startActivity(new Intent(applicationContext, Login.class));
//            }
//        });

        Intent i = getActivity().getIntent();
        currSuburb=suburb = ((OnlineNeighborhood) getActivity().getApplication()).getsuburb();
//        suburb = i.getStringExtra("SUBURB");

        suburbTextView.setText(suburb);
        addEvent.setOnClickListener(this);
        return mView;
    }
    @Override
    public void onStart(){
        super.onStart();
        databaseEvents = FirebaseDatabase.getInstance().getReference("suburbs");

        databaseEvents.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                eventList.clear();
                for(DataSnapshot suburbSnapshot : dataSnapshot.getChildren()) {
                    ArrayList<Event> events = new ArrayList<Event>();
                    Suburb currentSuburb = suburbSnapshot.getValue(Suburb.class);
                    try{

                        if (currSuburb.equals(currentSuburb.getSubName())) {
                            events = currentSuburb.getEvents();
                            for(Event event:events){
                                if(event != null) {
                                    Log.d(TAG, "HOST ID: "+event.getHost());
                                    eventList.add(event);
                                }


                            }
                            break;
                        }

                    } catch (NullPointerException e){
                        //this catches null pointer exceptions, it happens alot
                        //TODO: I need to find a better way to loop through all the suburbs
                        //if you look at the log you can see the 'null pointer' still gets the suburb name. weird.
                    }


                }
                //SET UP EVENTLIST
                //i think when theres no list of events this throws. not sure why but just put an error check on it
                //TODO: NEED TO FIX THIS ASAP. (turns out it still crashes even with this)
                try{
                    mRecyclerView = getActivity().findViewById(R.id.recyclerView);
                    mRecyclerView = getActivity().findViewById(R.id.recyclerView);
                    mLayoutManager = new LinearLayoutManager(getActivity());
                    mAdapter = new EventAdapter(eventList, getActivity());

                    mRecyclerView.setLayoutManager(mLayoutManager);
                    mRecyclerView.setAdapter(mAdapter);
                    mRecyclerView.setAdapter(mAdapter);

                }catch (NullPointerException e){
                    e.printStackTrace();
                }

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

        if(view == filterButton){
            Fragment filterFrag = new eventFilter();
        }

    }


    //loading fragment above the navigation bar
    private boolean loadFragment(Fragment fragment){


        if(fragment != null){

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container,fragment).commit();
        }
        return false;
    }


}
