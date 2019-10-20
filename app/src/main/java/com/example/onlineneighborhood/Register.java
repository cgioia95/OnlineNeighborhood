package com.example.onlineneighborhood;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import static java.util.Calendar.DAY_OF_MONTH;
import static java.util.Calendar.MONTH;
import static java.util.Calendar.YEAR;
import static java.util.Calendar.getInstance;

// REGISTER activity takes some user details, creates a user, authenticating in Firebase and uploading their details to a database
public class Register extends AppCompatActivity implements View.OnClickListener, DatePickerDialog.OnDateSetListener {

    // Declare all simple Variables
    private Button buttonRegister, dateOfBirth;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private EditText editTextName, editBio;
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
        //Please make sure there is no .push() after Users; causes profile crashes
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        // Bind all simple variables
        progressDialog = new ProgressDialog(this);
        buttonRegister = (Button) findViewById(R.id.buttonRegister);
        editTextEmail = (EditText) findViewById(R.id.editTextEmail);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);
        editTextName = (EditText) findViewById(R.id.editTextName);
        editBio = (EditText) findViewById(R.id.etBio);
        dateOfBirth = findViewById(R.id.dateOfBirth);
        spinnerPreferences = (Spinner) findViewById(R.id.spinnerPreferences);
        textViewSignIn = (TextView) findViewById(R.id.textViewSignIn);

        //Setting up the spinner with different types of event
        String[] spinner_array = getApplicationContext().getResources().getStringArray(R.array.preferences);

        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(
                this,R.layout.spinner_item,spinner_array
        );
        spinnerArrayAdapter.setDropDownViewResource(R.layout.spinner_item);
        spinnerPreferences.setAdapter(spinnerArrayAdapter);


        // Assign Click Listeners to Register Button and Link to Sign In Button
        buttonRegister.setOnClickListener(this);
        textViewSignIn.setOnClickListener(this);
        dateOfBirth.setOnClickListener(this);
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
        final String dob = dateOfBirth.getText().toString().trim();

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

                            //getting the specific firebase authentication ID of the user and associating it with the database
                            //creating an instance with the user information and then setting in the firebase database
                            String Uid = task.getResult().getUser().getUid();

                            UserInformation userInformation = new UserInformation(name, preference, dob, bio);

                            databaseReference.child(Uid).setValue(userInformation);

                            Intent i = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(i);


                        } else {

                            progressDialog.dismiss();
                            Toast.makeText(Register.this, "Could not register ... please try again", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
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
        dateOfBirth.setText(date);
    }

    // Assigning functions to the two buttons
    @Override
    public void onClick(View view) {

        if (view == buttonRegister){
            registerUser();
        }

        if (view == textViewSignIn){
            startActivity(new Intent(this, ChooseSuburb.class));
        }

        if(view == dateOfBirth){

            showDatePickerDialog();
        }


    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}
