package com.example.onlineneighborhood;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.util.Log;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import static java.util.Calendar.*;

public class UserProfile extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    private TextView textViewName;
    private Spinner spinnerPreferences;
    private Button editProfileBtn;
    private Button logoutBtn;
    // private TextView textViewdob;
    private EditText editTextdob;
    private EditText editTextBio;
    private ImageButton imageButtonPicture;
    private DatePickerDialog.OnDateSetListener DateSetListener;
    private FirebaseAuth fireBaseAuth;
    private DatabaseReference databaseReference;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private UserInformation user;
    private static final int PICK_IMAGE = 1;
    private static final String TAG = "My Profile";
    private String uid;
    Uri imageuri;
    UserInformation host;
    private List<String> preferenceOptions = Arrays.asList("Sports", "Gigs", "Dating", "Misc.");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_screen);

        textViewName = findViewById(R.id.textViewName);
        editProfileBtn = findViewById(R.id.editProfileBtn);
        spinnerPreferences = (Spinner) findViewById(R.id.spinnerPreferences);
        editTextdob = findViewById(R.id.editTextdob);
        editTextBio = findViewById(R.id.editTextbio);
        logoutBtn = findViewById(R.id.logOutBtn);
        imageButtonPicture = findViewById(R.id.imageButtonPicture);
        fireBaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        storage = FirebaseStorage.getInstance();
        storageReference=storage.getReference();

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Log.d("user profile", "onCreate: " + toolbar);
        getSupportActionBar().setTitle("Online Neighborhood");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);



        if (fireBaseAuth.getCurrentUser() != null)
            uid = fireBaseAuth.getCurrentUser().getUid();




        editTextdob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                showDatePickerDialog();

            }

        });

        imageButtonPicture.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                Intent gallery = new Intent();
                gallery.setType("image/*");
                gallery.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(gallery, "Select Picture"), PICK_IMAGE);
            }
        });






        databaseReference.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("On DATA CHANGE ", "IN");
                Log.d("On DATA CHANGE", "Snapshot" + dataSnapshot);
                String name = dataSnapshot.child("name").getValue().toString();
                String preference = dataSnapshot.child("preference").getValue().toString();
                String dob = dataSnapshot.child("dob").getValue().toString();
                String bio = dataSnapshot.child("bio").getValue().toString();

                textViewName.setText(name);
                spinnerPreferences.setSelection(preferenceOptions.indexOf(preference));
                editTextdob.setText(dob);
                editTextBio.setText(bio);
                downloadImage();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        editProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String selectedPreference = spinnerPreferences.getSelectedItem().toString();
                String dob = editTextdob.getText().toString();
                String bio = editTextBio.getText().toString();
                Log.d("ON PREFERENCE CHANGE ", "to" + selectedPreference);
                databaseReference.child(uid).child("preference").setValue(selectedPreference);
                databaseReference.child(uid).child("dob").setValue(dob);
                databaseReference.child(uid).child("bio").setValue(bio);
                Toast.makeText(UserProfile.this, "Saved Succesfully!!", Toast.LENGTH_SHORT).show();

            }

        });
    }

    private void setSupportActionBar() {
    }

    private void showDatePickerDialog(){
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, this,
                getInstance().get(YEAR),
                getInstance().get(MONTH),
                getInstance().get(DAY_OF_MONTH)
        );

        datePickerDialog.show();

    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
        month++;
        String date = dayOfMonth +"/" + month + "/" + year;
        editTextdob.setText(date);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == PICK_IMAGE && resultCode == RESULT_OK){
            imageuri = data.getData();
            try{
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageuri);
                imageButtonPicture.setImageBitmap(bitmap);


                uploadImage();
            }catch(IOException e){
                e.printStackTrace();

            }
        }
    }

    private void uploadImage() {

        if(imageuri != null)
        {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            StorageReference ref = storageReference.child("profilePics/"+ uid.toString());
            ref.putFile(imageuri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            Toast.makeText(UserProfile.this, "Uploaded", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(UserProfile.this, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                    .getTotalByteCount());
                            progressDialog.setMessage("Uploaded "+(int)progress+"%");
                        }
                    });
        }
    }

    protected void downloadImage(){
        storageReference.child("profilePics/"+uid).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                // Got the download URL for 'users/me/profile.png' in uri
                Log.d(TAG, "DOWNLOAD URL: "+uri.toString());
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

}