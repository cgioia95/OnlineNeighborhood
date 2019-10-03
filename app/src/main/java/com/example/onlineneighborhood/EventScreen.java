package com.example.onlineneighborhood;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class EventScreen extends AppCompatActivity {
    private static final String TAG = "EventScreen";

    public TextView mEventName, mDescription, mTime, mDate, attendingTextView;
    public Button attendBtn;
    FirebaseAuth firebaseAuth;

    DatabaseReference databaseUsers, databaseSuburb, databaseEvent, databaseUser;

    public String thisUserString;

    public UserInformation thisUserInformation, currentUser;

    public ArrayList<UserInformation> attendees;

    public boolean attending;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_screen);

        Intent i = getIntent();
        final String intentSuburb = i.getStringExtra("SUBURB");

        firebaseAuth = FirebaseAuth.getInstance();

        databaseUsers = FirebaseDatabase.getInstance().getReference("Users");


        final Event mEvent= (Event) i.getSerializableExtra("MyObject");

        mEventName = findViewById(R.id.eventName);
        mDescription = findViewById(R.id.eventDesc);
        mDate = findViewById(R.id.eventDate);
        mTime = findViewById(R.id.eventTime);

        attendingTextView = findViewById(R.id.attendingTextView);

        databaseSuburb =  FirebaseDatabase.getInstance().getReference("suburbs").child(intentSuburb);


        databaseSuburb.child("events").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot eventSnapshot: dataSnapshot.getChildren()){


                    if (mEvent.getId().equals(eventSnapshot.child("id").getValue().toString())){

                        databaseEvent = eventSnapshot.getRef();



                        Event event = eventSnapshot.getValue(Event.class);

                        attendees = event.getAttendees();

                        Log.d(TAG, eventSnapshot.toString());
                        Log.d(TAG, "MATCH");


                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        thisUserString = firebaseAuth.getCurrentUser().getUid();

        Log.d(TAG, "This User String found: " + this.thisUserString);

        databaseUsers.child(thisUserString).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                currentUser = dataSnapshot.getValue(UserInformation.class);

                databaseUser = dataSnapshot.getRef();

                if (currentUser.getMyEventsAttending() != null) {

                    for (Event event : currentUser.getMyEventsAttending()) {
                        if (event != null) {
                            Log.d(TAG, "User Update: " + event.getId());

                        }
                    }

                    Log.d(TAG, "User Found: " + thisUserInformation.getUid());
                }



            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        thisUserInformation = new UserInformation(thisUserString);

        attendBtn = findViewById(R.id.attendBtn);

        mEventName.setText(mEvent.getName());
        mDescription.setText(mEvent.getDescription());
        mDate.setText(mEvent.getDate());
        mTime.setText(mEvent.getTime());

        String host = mEvent.getHost().getUid();


//        attendees = mEvent.getAttendees();


        attending = false;

        if (attendees != null) {
            for (UserInformation user : attendees) {

                if (user.getUid().equals(thisUserString)) {
                    Log.d(TAG, "ALREADY ATTENDING");
                    attending = true;

                    Toast.makeText(getApplicationContext(), "Already attending the event!", Toast.LENGTH_SHORT);

                    break;

                }

            }

        }

        if (attending == true){

            attendBtn.setText("UNATTEND");
            attendingTextView.setText("ATTENDING");

        } else {
            attendBtn.setText("ATTEND");
            attendingTextView.setText("UNATTENDING");
        }

        // Logic to run if I'm the hosting accessing the event page
        // No Attend/Unattend button & Attendance status is just host
        if (host.equals(thisUserString)){
            attendBtn.setVisibility(View.INVISIBLE);
            attendingTextView.setText("HOST");
        }


        attendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                attending = false;

                if (attendees != null) {
                    for (UserInformation user : attendees) {

                        if (user.getUid().equals(thisUserString)) {
                            Log.d(TAG, "ALREADY ATTENDING");
                            attending = true;

                            Toast.makeText(getApplicationContext(), "Already attending the event!", Toast.LENGTH_SHORT);

                            break;

                        }

                    }

                }

                Log.d(TAG, "Commencing attendance check");

                if (!attending){

                    Log.d(TAG, "User currently not attending");


                    if (currentUser.getMyEventsAttending()!= null) {
                        for (Event event : currentUser.getMyEventsAttending()) {

                            Log.d(TAG, event.getId().toString());

                        }

                    }

                    attendees.add(thisUserInformation);



                    Event updatedEvent = new Event(mEvent.getId(), mEvent.getHost(), mEvent.getAddress(), mEvent.getEventName(), mEvent.getDescription(),
                            mEvent.getTime(), mEvent.getDate(), mEvent.getEndTime(), mEvent.getEndDate(), mEvent.getType(),attendees);

                    Event userEvent = new Event(mEvent.getId(), intentSuburb);

                    ArrayList<Event> userEvents = new ArrayList<Event>();

                    userEvents.add(userEvent);


                    ArrayList<Event> eventCheck = currentUser.getMyEventsAttending();

                    if(eventCheck == null){
                        currentUser.setMyEventsAttending(userEvents);
                    }else{
                        currentUser.getMyEventsAttending().add(userEvent);
                    }

                    databaseUser.setValue(currentUser);

                    Log.d(TAG, "ADDING attendee : " + updatedEvent.toString());

                    databaseEvent.setValue(updatedEvent);
//                     Need to place this new event in the subrb/events reference



                    attending = true;
                    attendBtn.setText("UNATTEND");
                    attendingTextView.setText("ATTENDING");

                    Toast.makeText(getApplicationContext(), "You're now attending the event!" , Toast.LENGTH_SHORT );

                    Log.d(TAG, "User now attending");



                }

                // Unattend logic
                else {

                    Log.d(TAG, "User is already attending, remove the from attendance ");


                    // 1. Remove from myAttending list

                    final DatabaseReference userEventsAttending = databaseUsers.child(thisUserString).child("myEventsAttending");

                    userEventsAttending.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {

                                String id = eventSnapshot.child("id").getValue().toString();

                                if (mEvent.getId().equals(id)) {
                                    Log.d(TAG, "Event Reference: " + eventSnapshot.getRef().toString());
                                    Log.d(TAG, "Event ID: " +  id);
                                    eventSnapshot.getRef().removeValue();


                                }

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    } );



                    attendees.remove(thisUserInformation);

                    for (UserInformation attendee: attendees){
                        if (attendee.getUid().equals(thisUserString)){
                            attendees.remove(attendee);
                        }
                    }

                    Event updatedEvent = new Event(mEvent.getId(), mEvent.getHost(), mEvent.getAddress(), mEvent.getEventName(), mEvent.getDescription(),
                            mEvent.getTime(), mEvent.getDate(), mEvent.getEndTime(), mEvent.getEndDate(), mEvent.getType(),attendees);

                    databaseEvent.setValue(updatedEvent);



                    // 2.Remove from the attendees list





                    attending = false;
                    attendBtn.setText("ATTEND");
                    attendingTextView.setText("NOT ATTENDING");

                    Toast.makeText(getApplicationContext(), "You're no longer attending event!" , Toast.LENGTH_SHORT );

                    Log.d(TAG, "User removed from attending");

                }

                Log.d(TAG, "At end of click the attedance status is: " + attending);

            }
        });




    }
}
