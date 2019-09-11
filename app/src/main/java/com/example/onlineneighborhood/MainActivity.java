package com.example.onlineneighborhood;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

// MAIN activity where user chooses to Register or Login
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    // Declare simple variables
    private Button registerBtn;
    private Button loginBtn;
    private Button profileBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Bind simple variables
        registerBtn = (Button) findViewById(R.id.registerBtn);
        loginBtn = (Button) findViewById(R.id.loginBtn);

        // Set on Click Listeners
        registerBtn.setOnClickListener(this);
        loginBtn.setOnClickListener(this);

    }

    // Assigning functions to the two buttons
    @Override
    public void onClick(View view) {

        if (view == registerBtn){
            startActivity(new Intent(this, Register.class));
        }

        if (view == loginBtn){
            startActivity(new Intent(this, Login.class));
        }


    }
}
