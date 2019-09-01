package com.example.onlineneighborhood;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

// LOGIN Activity takes user's email + password, checks these details against Firebase Authenticator
// If successful takes them to Choose screen

public class Login extends AppCompatActivity implements View.OnClickListener {


    // Declare simple variables
    private Button buttonSignIn;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private TextView textViewSignup;

    // FireBase Authentication
    private FirebaseAuth firebaseAuth;

    // Simple loading screen while authentication is being processed
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Bind Simple Variables
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonSignIn = findViewById(R.id.buttonSignIn);
        textViewSignup = findViewById(R.id.textViewSignup);

        // Set on Click Listeners
        buttonSignIn.setOnClickListener(this);
        textViewSignup.setOnClickListener(this);

        // Initialize the Progress Dialog and the Firebase Authenticator
        progressDialog = new ProgressDialog(this);
        firebaseAuth = FirebaseAuth.getInstance();

        // Checks if the User's Logged in already, if so bypasses the Login Screen and takes them to Choose Screen
        if (firebaseAuth.getCurrentUser() != null){
            finish();
            startActivity(new Intent(getApplicationContext(), Choose.class));

        }

    }

    // Parses user information from EditTexts, checks all fields are filled
    // If all fields filled, attempts user Login
    // If successful, takes user to Choose Screen
    private void userLogin(){
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)){
            Toast.makeText(this, "Please enter email", Toast.LENGTH_SHORT).show();
            // Stopping this function executive further
            return;
            // email is empty
        }

        if (TextUtils.isEmpty(password)){
            // password  is empty
            Toast.makeText(this, "Please enter password", Toast.LENGTH_SHORT).show();
            // Stopping this function executive further
            return;
        }

        progressDialog.setMessage("Logging in User User");
        progressDialog.show();

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.dismiss();

                        if (task.isSuccessful()){
                            //start the profile activity
                            finish();
                            startActivity(new Intent(getApplicationContext(), Choose.class));
                        }

                        else {
                            Toast.makeText(Login.this, "Could not login ... please try again", Toast.LENGTH_SHORT).show();

                        }
                    }
                });

    }

    // Assigning functions to the two buttons
    @Override
    public void onClick(View view) {
        if (view == buttonSignIn){
            userLogin();
        }

        if (view == textViewSignup){
            startActivity(new Intent(this, MainActivity.class));
        }
    }
}
