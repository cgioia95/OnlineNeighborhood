package com.example.onlineneighborhood;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_screen);

        final Event mEvent= (Event) getIntent().getSerializableExtra("MyObject");
        final String suburb = getIntent().getStringExtra("SUBURB");

        mEventName = findViewById(R.id.eventName);
        mDescription = findViewById(R.id.eventDesc);
        mDate = findViewById(R.id.eventDate);
        mTime = findViewById(R.id.eventTime);
        hostPic = findViewById(R.id.imageView);

        storage = FirebaseStorage.getInstance();
        storageReference=storage.getReference();

        Log.d(TAG, "onCreate: eventName " + mEvent.getName());
        Log.d(TAG, "onCreate: eventDesc " + mEvent.getDescription());
        Log.d(TAG, "onCreate: eventTime" + mEvent.getTime());
        //Log.d("UID", "onCreate: hostid" + mEvent.getHost().getUid());
        //mEvent.getDescription()
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
                i.putExtra("SUBURB", suburb);
                startActivity(i);



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
