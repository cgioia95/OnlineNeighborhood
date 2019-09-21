package com.example.onlineneighborhood;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.onlineneighborhood.BottomNavigationActivity;
import com.example.onlineneighborhood.R;
import com.example.onlineneighborhood.UserInformation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static android.app.Activity.RESULT_OK;
import static java.util.Calendar.DAY_OF_MONTH;
import static java.util.Calendar.MONTH;
import static java.util.Calendar.YEAR;
import static java.util.Calendar.getInstance;

public class ProfileFragment extends Fragment implements DatePickerDialog.OnDateSetListener {

    private TextView textViewName;
    private Spinner spinnerPreferences;
    private Button editProfileBtn;
    // private TextView textViewdob;
    private EditText editTextdob;
    private EditText editTextBio;
    private ImageButton imageButtonPicture;
    private DatePickerDialog.OnDateSetListener DateSetListener;
    private FirebaseAuth fireBaseAuth;
    private DatabaseReference databaseReference;
    private DatabaseReference databaseReferenceUser;

    private FirebaseStorage storage;
    private StorageReference storageReference;
    private UserInformation user;
    private static final int PICK_IMAGE = 1;
    private static final String TAG = "My Profile";
    private String uid;

    Uri imageuri;
    UserInformation host;
    Context applicationContext = BottomNavigationActivity.getContextOfApplication();

    private List<String> preferenceOptions = Arrays.asList("Sports", "Gigs", "Dating", "Misc.");

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.activity_profile_screen, null);

        textViewName = mView.findViewById(R.id.textViewName);
        editProfileBtn = mView.findViewById(R.id.editProfileBtn);
        spinnerPreferences = (Spinner) mView.findViewById(R.id.spinnerPreferences);
        editTextdob = mView.findViewById(R.id.editTextdob);
        editTextBio = mView.findViewById(R.id.editTextbio);
        imageButtonPicture = mView.findViewById(R.id.imageButtonPicture);
        fireBaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        if (fireBaseAuth.getCurrentUser() != null)
            uid = fireBaseAuth.getCurrentUser().getUid();

//        StorageReference pathToFile = storageReference.child("profilePics/" + uid+".jpg");


        editTextdob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                showDatePickerDialog();

            }

        });

        imageButtonPicture.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent gallery = new Intent();
                gallery.setType("image/*");
                gallery.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(gallery, "Select Picture"), PICK_IMAGE);
            }
        });



        // Getting the user info reference databaseReferenceUser and grabbing its info
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshotI: dataSnapshot.getChildren()){

                    if (dataSnapshotI.hasChild(uid)){

                        DataSnapshot userSnapshot = dataSnapshotI.child(uid);

                        databaseReferenceUser = userSnapshot.getRef();

                        Log.d("PROFILE", String.valueOf(userSnapshot));

                        String name = userSnapshot.child("name").getValue().toString();
                        String preference = userSnapshot.child("preference").getValue().toString();
                        String dob = userSnapshot.child("dob").getValue().toString();
                        String bio = userSnapshot.child("bio").getValue().toString();



                        textViewName.setText(name);
                        spinnerPreferences.setSelection(preferenceOptions.indexOf(preference));
                        editTextdob.setText(dob);
                        editTextBio.setText(bio);
                        downloadImage();
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });





        editProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String selectedPreference = spinnerPreferences.getSelectedItem().toString();
                String dob = editTextdob.getText().toString();
                String bio = editTextBio.getText().toString();
                Log.d("ON PREFERENCE CHANGE ", "to" + selectedPreference);
                databaseReferenceUser.child("preference").setValue(selectedPreference);
                databaseReferenceUser.child("dob").setValue(dob);
                databaseReferenceUser.child("bio").setValue(bio);





                Toast.makeText(getActivity(), "Saved Succesfully!!", Toast.LENGTH_SHORT).show();

            }

        });

        return mView;

    }


        private void showDatePickerDialog() {
            DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), this,
                    getInstance().get(YEAR),
                    getInstance().get(MONTH),
                    getInstance().get(DAY_OF_MONTH)
            );

            datePickerDialog.show();
            // return super.onCreateView(inflater, container, savedInstanceState);
        }


    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
        month++;
        String date = dayOfMonth +"/" + month + "/" + year;
        editTextdob.setText(date);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE && resultCode == RESULT_OK){
            imageuri = data.getData();
            try{
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(applicationContext.getContentResolver(),imageuri);
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
            final ProgressDialog progressDialog = new ProgressDialog(getActivity());
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            StorageReference ref = storageReference.child("profilePics/"+ uid.toString());
            ref.putFile(imageuri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            Toast.makeText(getActivity(), "Uploaded", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(getActivity(), "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
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
