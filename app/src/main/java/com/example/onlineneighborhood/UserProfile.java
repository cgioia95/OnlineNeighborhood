package com.example.onlineneighborhood;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.Manifest;

import java.io.ByteArrayOutputStream;
import java.util.*;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import java.net.URI;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import id.zelory.compressor.Compressor;

import static androidx.core.graphics.TypefaceCompatUtil.getTempFile;
import static java.util.Calendar.*;

public class UserProfile extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    private TextView textViewName, tvBio, tvDOB;
    private Spinner spinnerPreferences;
    private Button editProfileBtn;
    private EditText editTextdob, editTextBio;
    private ImageButton imageButtonPicture;
    private DatePickerDialog.OnDateSetListener DateSetListener;
    private FirebaseAuth fireBaseAuth;
    private DatabaseReference databaseReference;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private ProgressDialog progressDialog;
    private Bitmap photo;


    private static final int PICK_IMAGE = 1, CAMERA_REQUEST=2;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;
//    private static final int EXTERNAL_STORAGE_PERMISSION_CONSTANT = 120;
//
//    private static final int REQUEST_PERMISSION_SETTING = 101;
//    private boolean sentToSettings = false;
//    private SharedPreferences permissionStatus;

    private static final int  REQUEST_TAKE_PHOTO = 15;
    private static final String TAG = "My Profile";
    private String uid, currentPhotoPath;
    Uri imageuri;
    private List<String> preferenceOptions = Arrays.asList("Sports", "Gigs", "Dating", "Misc.");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_screen);

        textViewName = findViewById(R.id.textViewName);
        tvBio = findViewById(R.id.tvBio);
        tvDOB = findViewById(R.id.tvDob);
        tvDOB.setVisibility(View.INVISIBLE);
        tvBio.setVisibility(View.INVISIBLE);
        editProfileBtn = findViewById(R.id.editProfileBtn);
        spinnerPreferences = (Spinner) findViewById(R.id.spinnerPreferences);
        editTextdob = findViewById(R.id.editTextdob);
        editTextBio = findViewById(R.id.editTextbio);
        imageButtonPicture = findViewById(R.id.imageButtonPicture);
        fireBaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        storage = FirebaseStorage.getInstance();
        storageReference=storage.getReference();

        editTextBio.setEnabled(false);
        editTextdob.setEnabled(false);
        spinnerPreferences.setEnabled(false);
        imageButtonPicture.setAdjustViewBounds(true);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Log.d("user profile", "onCreate: " + toolbar);
        getSupportActionBar().setTitle("Online Neighborhood");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

//        permissionStatus = getSharedPreferences("permissionStatus",MODE_PRIVATE);




        if (fireBaseAuth.getCurrentUser() != null)
            uid = fireBaseAuth.getCurrentUser().getUid();





        imageButtonPicture.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
//                if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
//                {
//                    requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);
//                }
//                else
//                {
//                    Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
//                    startActivityForResult(cameraIntent, CAMERA_REQUEST);
//                }
                CharSequence[] items={"Camera", "Gallery"};
                AlertDialog.Builder builder = new AlertDialog.Builder(UserProfile.this);
                builder.setTitle("Pick an image from").setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                       if (i==0){
                           if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                           {
                               requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);
                           }
                           else
                           {
                               Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                               startActivityForResult(cameraIntent, CAMERA_REQUEST);
                           }

                       }
                       if (i==1){
                           Intent gallery = new Intent();
                            gallery.setType("image/*");
                            gallery.setAction(Intent.ACTION_GET_CONTENT);

                            startActivityForResult(Intent.createChooser(gallery, "Select Picture"), PICK_IMAGE);


                       }
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });


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

                    textViewName.setText(name);
                    spinnerPreferences.setSelection(preferenceOptions.indexOf(preference));
                    editTextdob.setText(dob);
                    editTextBio.setText(bio);
                    downloadImage();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });


    }

    private void setSupportActionBar() {
    }

       //inflate toolbar menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.profile_menu, menu);
        return true;
    }


    //managing toolbar items
    @Override
    public boolean onOptionsItemSelected(final MenuItem item){
        if(item.getItemId() == R.id.logout){

            fireBaseAuth.signOut();
            startActivity(new Intent(getApplicationContext(), Login.class));
        }

        if(item.getItemId() == R.id.edit){
            editProfileBtn.setVisibility(View.VISIBLE);
            editTextBio.setEnabled(true);
            editTextdob.setEnabled(true);
            spinnerPreferences.setEnabled(true);

            editTextdob.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    showDatePickerDialog();

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
                    tvDOB.setText("DOB updated to: " + dob);
                    tvBio.setText("Bio updated to: " + bio);
                    tvDOB.setVisibility(View.VISIBLE);
                    tvBio.setVisibility(View.VISIBLE);
                    Toast.makeText(UserProfile.this, "Saved Succesfully!!", Toast.LENGTH_SHORT).show();


                    editTextBio.setEnabled(false);
                    editTextdob.setEnabled(false);
                    spinnerPreferences.setEnabled(false);
                    editProfileBtn.setVisibility(View.INVISIBLE);
                }

            });



        }
        return super.onOptionsItemSelected(item);
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

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK) {
            imageuri = data.getData();
            try {
                photo = MediaStore.Images.Media.getBitmap(getContentResolver(), imageuri);
                submit();
                imageButtonPicture.setImageBitmap(photo);
            } catch (IOException e) {
                e.printStackTrace();

            }
        }
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK)
        {
            photo = (Bitmap) data.getExtras().get("data");
            submit();
            imageButtonPicture.setImageBitmap(photo);
        }

        }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            } else {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }


    public void submit(){

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        photo.compress(Bitmap.CompressFormat.JPEG, 100, stream);


        byte[] b = stream.toByteArray();
        StorageReference ref = storageReference.child("profilePics/"+ uid.toString());
        //StorageReference filePath = FirebaseStorage.getInstance().getReference().child("profile_images").child(userID);
        ref.putBytes(b).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Uri downloadUrl = taskSnapshot.getUploadSessionUri();
                Toast.makeText(UserProfile.this, "uploaded", Toast.LENGTH_SHORT).show();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(UserProfile.this,"failed",Toast.LENGTH_LONG).show();


            }
        });

    }


    private void uploadImage() throws IOException {
        Log.d(TAG, "uploadImage: "+ imageuri);
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

            storageReference.child("profilePics/" + uid).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    // Got the download URL for 'users/me/profile.png' in uri
                    Log.d(TAG, "DOWNLOAD URL: " + uri.toString());
                    Picasso.get().load(uri).into(imageButtonPicture);
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
                            Picasso.get().load(uri).into(imageButtonPicture);
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