package com.example.onlineneighborhood;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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

public class otherProfile extends AppCompatActivity {

    private TextView textViewName;
    private Button editProfileBtn;
    private TextView editTextdob, editTextBio, editTextPreferences;
    private ImageButton imageButtonPicture;
    private FirebaseAuth fireBaseAuth;
    private DatabaseReference databaseReference;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private static final int PICK_IMAGE = 1;
    private static final String TAG = "Other Profile";
    private String uid;
    Uri imageuri;
    private List<String> preferenceOptions = Arrays.asList("Sports", "Gigs", "Dating", "Misc.");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_profile);

        textViewName = findViewById(R.id.textViewName);
        editTextdob = findViewById(R.id.editTextdob);
        editTextBio = findViewById(R.id.editTextbio);
        editTextPreferences = findViewById(R.id.Preferences);
        imageButtonPicture = findViewById(R.id.imageButtonPicture);
        fireBaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        storage = FirebaseStorage.getInstance();
        storageReference=storage.getReference();


        Intent i = getIntent();
        uid = i.getStringExtra("UID");


        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Intent intent = new Intent();
        Log.d("user profile", "onCreate: " + toolbar);
        getSupportActionBar().setTitle("Online Neighborhood");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);






        databaseReference.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("On DATA CHANGE ", "IN");
                Log.d("On DATA CHANGE", "Snapshot" + dataSnapshot);
                if (dataSnapshot.getValue() != null) {
                    String name = dataSnapshot.child("name").getValue().toString();
                    String preference = dataSnapshot.child("preference").getValue().toString();
                    String dob = dataSnapshot.child("dob").getValue().toString();
                    String bio = dataSnapshot.child("bio").getValue().toString();

                    String[] dates = dob.split("/", 3);
                    LocalDate today             = LocalDate.now();
                    LocalDate birthday          = LocalDate.of(Integer.parseInt(dates[2]), Integer.parseInt(dates[1]), Integer.parseInt(dates[0]));
                    //LocalDate thisYearsBirthday = birthday.with(Year.now());

                    int age = (int) ChronoUnit.YEARS.between(birthday, today);
                    textViewName.setText(name);
                    editTextPreferences.setText(preference);
                    editTextdob.setText(Integer.toString(age));
                    editTextBio.setText(bio);
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
                Picasso.get().load(uri).into(imageButtonPicture);



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
                        Picasso.get().load(uri).into(imageButtonPicture);



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
