package com.example.onlineneighborhood;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class EventScreen extends AppCompatActivity {
    private static final String TAG = "EventScreenTAG";
    private static final String TAG2 = "EventScreenList";


    // Declare simple variables

    public TextView mEventName, mDescription, mTime, mDate, mEventAdress, attendingTextView;
    private StorageReference storageReference;
    public CircleImageView hostPic;
    public Button attendBtn, editBtn, deleteBtn;
    public String thisUserString;
    public UserInformation thisUserInformation, currentUser;
    public ArrayList<UserInformation> attendees;
    public boolean attending;

    //Firebase variables
    FirebaseAuth firebaseAuth;
    DatabaseReference databaseUsers, databaseSuburb, databaseEvent, databaseUser;
    private FirebaseStorage storage;


    //Setting up recycler-view and adapter for displaying events
    private RecyclerView mRecyclerView;
    private UserAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArrayList<UserInformation> userList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_screen);
        Intent i = getIntent();

        firebaseAuth = FirebaseAuth.getInstance();
        databaseUsers = FirebaseDatabase.getInstance().getReference("Users");

        Log.d(TAG, "HERE");
        final Event mEvent= (Event) i.getSerializableExtra("MyObject");
        final String intentSuburb = mEvent.getSuburbId();

        Log.d(TAG, intentSuburb);
        Log.d(TAG, "onCreate: " + mEvent);

        //Retrieve views to be populated
        mEventName = findViewById(R.id.eventName);
        mDescription = findViewById(R.id.eventDesc);
        mDate = findViewById(R.id.eventDate);
        mTime = findViewById(R.id.eventTime);
        hostPic = findViewById(R.id.hostImage);

        mEventAdress = findViewById(R.id.eventAddress);

        storage = FirebaseStorage.getInstance();
        storageReference=storage.getReference();
        //attendingTextView = findViewById(R.id.attendingTextView);

        thisUserString = firebaseAuth.getCurrentUser().getUid();
        Log.d(TAG, "This User String found: " + this.thisUserString);

        //Setting toolbar for adding profile icon
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Togather");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        // Retrieves reference to our user information
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
        databaseSuburb =  FirebaseDatabase.getInstance().getReference("suburbs").child(intentSuburb);

        // Cycle through all the events in our suburb, retrieve the event we're inspecting
        // Retrieve all the attendees to this event
        // List these attendees on the event screen
        databaseSuburb.child("events").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot eventSnapshot: dataSnapshot.getChildren()){
                    if (mEvent.getId().equals(eventSnapshot.child("id").getValue().toString())){

                        databaseEvent = eventSnapshot.getRef();
                        Event event = eventSnapshot.getValue(Event.class);
                        attendees = event.getAttendees();
                        attending = false;

                        if (attendees != null) {

                            final int size = attendees.size();

                            try{

                                mRecyclerView = findViewById(R.id.recyclerViewUsers);
                                mLayoutManager = new LinearLayoutManager( getApplicationContext(),
                                        LinearLayoutManager.HORIZONTAL, false);
                                mAdapter = new UserAdapter(attendees, getApplicationContext() );

                                mRecyclerView.setLayoutManager(mLayoutManager);
                                mRecyclerView.setAdapter(mAdapter);

                                mAdapter.setOnUserClickListener(new UserAdapter.onUserClickListener() {


                                    @Override
                                    public void onEventClick(int position) {
                                        UserInformation attendee = attendees.get(position);
                                        Intent i = new Intent( getApplicationContext(), attendeeProfile.class);
                                        i.putExtra("UID", attendee.getUid());
                                        startActivity(i);
                                    }


                                });


                            }catch (NullPointerException e){
                                e.printStackTrace();
                            }

                            // Check if we're attending the event or not
                            for (UserInformation attendee: attendees) {
                                if (attendee.getUid().equals(thisUserString)) {
                                     attending = true;
                                     break;
                                }
                            }
                        }

                        // Change the attend/unattend button based on if attending or not
                        if (attending == true){
                            attendBtn.setText("UNATTEND");
                        } else {
                            attendBtn.setText("ATTEND");
                        }
                        Log.d(TAG, eventSnapshot.toString());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //Get button views
        attendBtn = findViewById(R.id.attendBtn);
        editBtn = findViewById(R.id.editBtn);
        deleteBtn = findViewById(R.id.deleteBtn);
        Log.d(TAG, "onCreate: "+ mEvent);

        //Pre-population
        Log.d(TAG, "POPULATING WITH EVENT INFO ON CREATE");
        mEventName.setText(mEvent.getName());
        mEventAdress.setText(mEvent.getAddress());
        mDescription.setText(mEvent.getDescription());
        mDate.setText(mEvent.getDate());
        mTime.setText(mEvent.getTime());

        // Get host's profile pic
        downloadImage(mEvent.getHost().getUid());

        // Hosts profile pic takes us to their profile screen to see more info abot htem
        hostPic.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), attendeeProfile.class);
                i.putExtra("UID", mEvent.getHost().getUid());
                i.putExtra("MyObject", mEvent);
                i.putExtra("SUBURB", intentSuburb);
                startActivity(i);
            }

        });

        // Get the events host id
        String host = mEvent.getHost().getUid();

        //Logic to run if host is accessing the event page
        //No Attend/Unattend button & Attendance - status is just host
        if (host.equals(thisUserString)){
            attendBtn.setVisibility(View.INVISIBLE);
            editBtn.setVisibility(View.VISIBLE);
            deleteBtn.setVisibility(View.VISIBLE);
            //attendingTextView.setText("HOST");
        }
        else {
            attendBtn.setVisibility(View.VISIBLE);
            editBtn.setVisibility(View.INVISIBLE);
            deleteBtn.setVisibility(View.INVISIBLE);
        }

        // set on click listener for the edit button
        // Starts the edit activity and awaits the resulting changes
        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), editDelete.class);
                intent.putExtra("MyObject", mEvent);
                intent.putExtra("SUBURB", intentSuburb);
                startActivityForResult(intent, 0);
            }
        });



        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                databaseEvent.removeValue();
                DatabaseReference userEvents = databaseUsers.child(firebaseAuth.getCurrentUser().getUid()).child("myEvents");

                userEvents.addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                            String id = eventSnapshot.child("id").getValue().toString();
                            if (mEvent.equals(id)) {
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

                ArrayList<UserInformation> attendees = mEvent.getAttendees();
                for (UserInformation attendee: attendees){

                    String attendeeString = attendee.getUid();
                    DatabaseReference userEventsAttending = databaseUsers.child(attendeeString).child("myEventsAttending");

                    userEventsAttending.addListenerForSingleValueEvent(new ValueEventListener() {

                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot != null ) {
                                if (dataSnapshot.child("id").getValue() != null){

                                    String id = dataSnapshot.child("id").getValue().toString();

                                    if (id == mEvent.getId()) {
                                        dataSnapshot.getRef().removeValue();

                                    }
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
                finish();
            }







        });

        attendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                attending = false;
                if (attendees != null) {
                    for (UserInformation user : attendees) {
                        if (user.getUid().equals(thisUserString)) {
                            Log.d(TAG, "ALREADY ATTENDING");
                            attending = true;
                            break;
                        }
                    }
                }

                Log.d(TAG, "Commencing attendance check");
                if (!attending){
                    Log.d(TAG, "User currently not attending");
                    if (currentUser.getMyEventsAttending()!= null) {
                        for (Event event : currentUser.getMyEventsAttending()) {
                            if (event != null) {
                                Log.d(TAG, event.getId().toString());
                            }
                        }
                    }

                    ArrayList<UserInformation> addList = attendees;
                    addList.add(thisUserInformation);
//                  attendees.add(thisUserInformation);

                    Event updatedEvent = new Event(mEvent.getId(), mEvent.getHost(),
                            mEvent.getAddress(), mEvent.getEventName(), mEvent.getDescription(),
                            mEvent.getTime(), mEvent.getDate(), mEvent.getEndTime(),
                            mEvent.getEndDate(), mEvent.getType(),addList, mEvent.getSuburbId());

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
//                  Need to place this new event in the subrb/events reference

                    attending = true;
                    attendBtn.setText("UNATTEND");
                    //attendingTextView.setText("ATTENDING");


                    //Pop up message for clicking attending an event
                    Toast.makeText(getApplicationContext(), "You're now attending the event!" , Toast.LENGTH_SHORT ).show();
                    Log.d(TAG, "User now attending");
                }

                //Unattend logic
                else {
                    Log.d(TAG, "User is already attending, remove the from attendance ");

                    //Remove from myAttending list
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

                    ArrayList<UserInformation> removeList = attendees;
////                    attendees.remove(thisUserInformation);
//
//                    int i = 0;
//                    for (UserInformation attendee: attendees){
//
//                        if (attendee.getUid().equals(thisUserString)){
//
//
//                            removeList.remove(i);
//
//                            Log.d(TAG, "FOUND ME, THE USER TO BE REMOVED: " + attendee.getUid());
//
//                        }
//
//                        i++;
//                    }

                    Iterator<UserInformation> iter = removeList.iterator();

                    while (iter.hasNext()) {
                        UserInformation user = iter.next();
                        if (user.getUid().equals(thisUserString))
                            iter.remove();
                    }

                    Event updatedEvent = new Event(mEvent.getId(), mEvent.getHost(),
                            mEvent.getAddress(), mEvent.getEventName(), mEvent.getDescription(),
                            mEvent.getTime(), mEvent.getDate(), mEvent.getEndTime(),
                            mEvent.getEndDate(), mEvent.getType(),removeList, mEvent.getSuburbId());

                    databaseEvent.setValue(updatedEvent);


                    // 2.Remove from the attendees list
                    attending = false;
                    attendBtn.setText("ATTEND");
                    //attendingTextView.setText("NOT ATTENDING");

                    //Pop up message for unattending an event
                    Toast.makeText(getApplicationContext(), "You're no longer attending event!" , Toast.LENGTH_SHORT ).show();
                    Log.d(TAG, "User removed from attending");
                }
                Log.d(TAG, "At end of click the attedance status is: " + attending);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "POPULATING WITH EVENT INFO 1");

        if (requestCode == 0){

            Log.d(TAG, "POPULATING WITH EVENT INFO 2");

            if (resultCode == 1) {

                Log.d(TAG, "POPULATING WITH EVENT INFO 2");

                if(data!=null) {

                    Log.d(TAG, "POPULATING WITH EVENT INFO 3");
                    Event resultEvent = (Event) data.getSerializableExtra("MyObject");

                    Log.d(TAG, resultEvent.getName());
                    mEventName.setText(resultEvent.getName());
                    mDescription.setText(resultEvent.getDescription());
                    mDate.setText(resultEvent.getDate());
                    mTime.setText(resultEvent.getTime());



                }
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "START");
        Log.d(TAG2, "Acquiring attendee data");

        if (attendees != null){
            for (UserInformation attendee: attendees){
                Log.d(TAG2, attendee.getUid());
                DatabaseReference user =  databaseUser.child(attendee.getUid());

                user.addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Log.d(TAG2, "USERSNAPSHOT" + dataSnapshot.toString());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        }
        Log.d(TAG2, "No one attending!");
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
