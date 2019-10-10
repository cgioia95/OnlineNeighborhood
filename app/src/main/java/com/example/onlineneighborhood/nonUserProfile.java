package com.example.onlineneighborhood;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.time.LocalDate;
import java.time.Year;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import de.hdodenhof.circleimageview.CircleImageView;

/*
This class is displayed when a user clicks on a user profile which is not their own
 */
public class nonUserProfile extends AppCompatActivity {

    private TextView textViewName;
    private TextView dateOfBirth, bio, Preferences;
    private CircleImageView profileImage;
    private FirebaseAuth fireBaseAuth;
    private DatabaseReference databaseReference;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private static final String TAG = "Non user profile";
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_non_user_profile);

        textViewName = findViewById(R.id.userName);
        dateOfBirth = findViewById(R.id.dateOfBirth);
        bio = findViewById(R.id.bio);
        Preferences = findViewById(R.id.preferences);
        profileImage = findViewById(R.id.profileImage);
        fireBaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        storage = FirebaseStorage.getInstance();
        storageReference=storage.getReference();

        //Get user ID from intent
        Intent i = getIntent();
        uid = i.getStringExtra("UID");

        //Set toolbar text to display UserProfile to show user where they are in the app
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("User Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        //Get user information from DB based on their user ID
        databaseReference.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("On DATA CHANGE ", "IN");
                Log.d("On DATA CHANGE", "Snapshot" + dataSnapshot);
                if (dataSnapshot.getValue() != null) {
                    String name = dataSnapshot.child("name").getValue().toString();
                    String preference = dataSnapshot.child("preference").getValue().toString();
                    String dob = dataSnapshot.child("dob").getValue().toString();
                    String bioText = dataSnapshot.child("bio").getValue().toString();

                    String[] dates = dob.split("/", 3);
                    LocalDate today             = LocalDate.now();
                    LocalDate birthday          = LocalDate.of(Integer.parseInt(dates[2]), Integer.parseInt(dates[1]), Integer.parseInt(dates[0]));
                    //LocalDate thisYearsBirthday = birthday.with(Year.now());

                    int age = (int) ChronoUnit.YEARS.between(birthday, today);
                    textViewName.setText(name);
                    Preferences.setText(preference);
                    dateOfBirth.setText(Integer.toString(age));
                    bio.setText(bioText);
                    downloadImage();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void downloadImage(){
        storageReference.child("profilePics/" + uid).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                // Got the download URL for 'users/me/profile.png' in uri
                Log.d(TAG, "DOWNLOAD URL: " + uri.toString());
                Picasso.get().load(uri).into(profileImage);
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
                        Picasso.get().load(uri).into(profileImage);
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
