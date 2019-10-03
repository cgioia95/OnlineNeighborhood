package com.example.onlineneighborhood;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class EventScreen extends AppCompatActivity {
    private static final String TAG = "EventScreen";

    private FirebaseStorage storage;
    private StorageReference storageReference;
    public TextView mEventName, mDescription, mTime, mDate;
    public ImageView hostPic;
    public Button attendBtn;
    FirebaseAuth firebaseAuth;

    DatabaseReference databaseUsers, databaseSuburb, databaseEvent, databaseUser;

    public String thisUserString;

    public UserInformation thisUserInformation, currentUser;

    public ArrayList<UserInformation> attendees;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_screen);

        Intent i = getIntent();
        final String intentSuburb = ((OnlineNeighborhood) this.getApplication()).getsuburb();

        firebaseAuth = FirebaseAuth.getInstance();
        databaseUsers = FirebaseDatabase.getInstance().getReference("Users");

        Log.d(TAG, "HERE");
        final Event mEvent= (Event) i.getSerializableExtra("MyObject");
        Log.d(TAG, "onCreate: " + mEvent);
        mEventName = findViewById(R.id.eventName);
        mDescription = findViewById(R.id.eventDesc);
        mDate = findViewById(R.id.eventDate);
        mTime = findViewById(R.id.eventTime);
        hostPic = findViewById(R.id.imageView);

       


        storage = FirebaseStorage.getInstance();
        storageReference=storage.getReference();

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

        databaseUsers.child(thisUserString).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                currentUser = dataSnapshot.getValue(UserInformation.class);

                databaseUser = dataSnapshot.getRef();

                Log.d(TAG, "User Found: " + thisUserInformation.getUid());


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        thisUserInformation = new UserInformation(thisUserString);

        attendBtn = findViewById(R.id.attendBtn);
        Log.d(TAG, "onCreate: "+ mEvent);

        Log.d(TAG, "onCreate: eventName " + mEvent.getName());
        Log.d(TAG, "onCreate: eventDesc " + mEvent.getDescription());
        Log.d(TAG, "onCreate: eventTime" + mEvent.getTime());

        mEventName.setText(mEvent.getName());
        mDescription.setText(mEvent.getDescription());
        mDate.setText(mEvent.getDate());
        mTime.setText(mEvent.getTime());
        downloadImage(mEvent.getHost().getUid());
        hostPic.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {

                Intent i = new Intent(getApplicationContext(), otherProfile.class);
                i.putExtra("UID", mEvent.getHost().getUid());
                i.putExtra("MyObject", mEvent);
                i.putExtra("SUBURB", intentSuburb);
                startActivity(i);



            }

        });


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

                        Toast.makeText(getApplicationContext(), "Already attending the event!" , Toast.LENGTH_SHORT ).show();

                        break;


                    }

                }

                if (!alreadyAttending){



                    attendees.add(thisUserInformation);

                    Log.d(TAG, "ADDING attendeed : " + attendees.get(0).getUid());

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

                    Log.d(TAG, "ADDING attendeed : " + updatedEvent.toString());

                    databaseEvent.setValue(updatedEvent);
                    // Need to place this new event in the subrb/events reference

                    Toast.makeText(getApplicationContext(), "You're now attending the event!" , Toast.LENGTH_SHORT ).show();


                }

            }
        });




    }


    protected void downloadImage(String uid) {
        storageReference.child("profilePics/" + uid).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                // Got the download URL for 'users/me/profile.png' in uri
                Log.d(TAG, "DOWNLOAD URL: " + uri.toString());
                Picasso.get().load(uri).into(hostPic);
                return;


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
                Log.d(TAG, "DOWNLOAD URL: FAILURE");
                storageReference.child("profilePics/" + "default.png").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        // Got the download URL for 'users/me/profile.png' in uri
                        Log.d(TAG, "DOWNLOAD URL: " + uri.toString());
                        Picasso.get().load(uri).into(hostPic);
                        return;




                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle any errors
                        Log.d(TAG, "DOWNLOAD URL: FAILURE");

                    }
                });

            }
        });


    }



    }
