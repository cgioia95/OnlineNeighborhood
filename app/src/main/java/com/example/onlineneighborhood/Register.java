package com.example.onlineneighborhood;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.Console;

// REGISTER activity takes some user details, creates a user, authenticating in Firebase and uploading their details to a database
public class Register extends AppCompatActivity implements View.OnClickListener {

    // Declare all simple Variables
    private Button buttonRegister;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private EditText editTextName, editBio, editDob;
    private Spinner spinnerPreferences;
    private TextView textViewSignIn;

    // Simple loading screen while authentication is being processed
    private ProgressDialog progressDialog;

    // The FireBase Authentication & Database Classes
    private FirebaseAuth fireBaseAuth;
    private DatabaseReference databaseReference;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // DatabaseReference pointing to a sub-reference Users where User Information is stored
        fireBaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").push();

        // Bind all simple variables

        progressDialog = new ProgressDialog(this);
        buttonRegister = (Button) findViewById(R.id.buttonRegister);
        editTextEmail = (EditText) findViewById(R.id.editTextEmail);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);
        editTextName = (EditText) findViewById(R.id.editTextName);
        editBio = (EditText) findViewById(R.id.etBio);
        editDob = (EditText) findViewById(R.id.etDOB);
        spinnerPreferences = (Spinner) findViewById(R.id.spinnerPreferences);
        textViewSignIn = (TextView) findViewById(R.id.textViewSignIn);

        // Assign Click Listeners to Register Button and Link to Sign In Button
        buttonRegister.setOnClickListener(this);
        textViewSignIn.setOnClickListener(this);
    }

    // Parses editText inputs, registers the user with FireBase Authentication
    // If successful registration, the User's Information immediatley uploaded to Database too
    // Finally, redirects the user to the Sign In Page
    // If any fields missing, prompts user to enter before proceeding
    // TODO: Currently no way I can find to neatly streamline it so user immediately signs in following a login
    private void registerUser(){
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        final String name = editTextName.getText().toString().trim();
        final String preference = spinnerPreferences.getSelectedItem().toString();
        final String bio = editBio.getText().toString().trim();
        final String dob = editDob.getText().toString().trim();

        if (TextUtils.isEmpty(email)){
            Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show();
            // Stopping this function executive further
            return;
            // email is empty
        }

        if (TextUtils.isEmpty(password)){
            // password  is empty
            Toast.makeText(this, "Please enter a password", Toast.LENGTH_SHORT).show();
            // Stopping this function executive further
            return;
        }

        if (TextUtils.isEmpty(name)){
            // password  is empty
            Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show();
            // Stopping this function executive further
            return;
        }

        if (TextUtils.isEmpty(preference)){
            // password  is empty
            Toast.makeText(this, "Please choose preference", Toast.LENGTH_SHORT).show();
            // Stopping this function executive further
            return;
        }

        if (TextUtils.isEmpty(bio)){
            // password  is empty
            Toast.makeText(this, "Please enter bio", Toast.LENGTH_SHORT).show();
            // Stopping this function executive further
            return;
        }

        if (TextUtils.isEmpty(dob)){
            // password  is empty
            Toast.makeText(this, "Please enter Date of Birth", Toast.LENGTH_SHORT).show();
            // Stopping this function executive further
            return;
        }

        progressDialog.setMessage("Registering User");
        progressDialog.show();

        fireBaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if(task.isSuccessful()){
                            progressDialog.dismiss();

                            Toast.makeText(Register.this, "Registered Succesfully", Toast.LENGTH_SHORT).show();

                            String Uid = task.getResult().getUser().getUid();

                            UserInformation userInformation = new UserInformation(name, preference, dob, bio);

                            databaseReference.child(Uid).setValue(userInformation);

                            Intent i = new Intent(getApplicationContext(), Login.class);
                            startActivity(i);


                        } else {
                            progressDialog.dismiss();

                            Toast.makeText(Register.this, "Could not register ... please try again", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // Assigning functions to the two buttons
    @Override
    public void onClick(View view) {

        if (view == buttonRegister){
            registerUser();
        }

        if (view == textViewSignIn){
            startActivity(new Intent(this, Login.class));
        }


    }
}
