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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class EventScreen extends AppCompatActivity {
    private static final String TAG = "EventScreen";

    public TextView mEventName, mDescription, mTime, mDate;
    public Button attendBtn;
    FirebaseAuth firebaseAuth;

    DatabaseReference databaseUsers, databaseSuburb, databaseEvent;

    public String thisUserString;

    public UserInformation thisUserInformation;

    public ArrayList<UserInformation> attendees;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_screen);

        Intent i = getIntent();
        String intentSuburb = i.getStringExtra("SUBURB");

        firebaseAuth = FirebaseAuth.getInstance();

        databaseUsers = FirebaseDatabase.getInstance().getReference("Users");


        final Event mEvent= (Event) i.getSerializableExtra("MyObject");

        mEventName = findViewById(R.id.eventName);
        mDescription = findViewById(R.id.eventDesc);
        mDate = findViewById(R.id.eventDate);
        mTime = findViewById(R.id.eventTime);

        databaseSuburb =  FirebaseDatabase.getInstance().getReference("suburbs").child(intentSuburb);


        databaseSuburb.child("events").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot eventSnapshot: dataSnapshot.getChildren()){


                    if (mEvent.getId().equals(eventSnapshot.child("id").getValue().toString())){

                        databaseEvent = eventSnapshot.getRef();

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

//        databaseUsers.child(thisUserString).addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//
//
//                UserInformation currentUser = dataSnapshot.getValue(UserInformation.class);
//
//                thisUserInformation = new UserInformation(currentUser.getUid());
//
//
//
//                Log.d(TAG, "User Found: " + thisUserInformation.getUid());
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });

        thisUserInformation = new UserInformation(thisUserString);

        attendBtn = findViewById(R.id.attendBtn);

        Log.d(TAG, "onCreate: eventName " + mEvent.getName());
        Log.d(TAG, "onCreate: eventDesc " + mEvent.getDescription());
        Log.d(TAG, "onCreate: eventTime" + mEvent.getTime());

        mEventName.setText(mEvent.getName());
        mDescription.setText(mEvent.getDescription());
        mDate.setText(mEvent.getDate());
        mTime.setText(mEvent.getTime());

        attendees = mEvent.getAttendees();


        Log.d(TAG, "Attendees: " + attendees.toString());
        Log.d(TAG, "Attendees: " + attendees.get(0).getUid());
        Log.d(TAG, "Attendees: " + attendees.get(0).getName());


        attendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                boolean alreadyAttending = false;

                for (UserInformation user: attendees){

                    if (user.getUid().equals(thisUserString)){
                        Log.d(TAG, "ALREADY ATTENDING");
                        alreadyAttending = true;
                        break;

                    }

                }

                if (!alreadyAttending){

                    attendees.add(thisUserInformation);

                    Log.d(TAG, "ADDING attendeed : " + attendees.get(0).getUid());

                    Event updatedEvent = new Event(mEvent.getId(), mEvent.getHost(), mEvent.getAddress(), mEvent.getEventName(), mEvent.getDescription(),
                            mEvent.getTime(), mEvent.getDate(), mEvent.getEndTime(), mEvent.getEndDate(), mEvent.getType(),attendees);


                    Log.d(TAG, "ADDING attendeed : " + updatedEvent.toString());

                    databaseEvent.setValue(updatedEvent);
                    // Need to place this new event in the subrb/events reference

                }

            }
        });




    }
}
