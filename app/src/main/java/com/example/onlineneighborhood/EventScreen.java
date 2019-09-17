package com.example.onlineneighborhood;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class EventScreen extends AppCompatActivity {

    public TextView mEventName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_screen);


        Intent i = getIntent();
        Event mEvent = (Event) i.getParcelableExtra("eventObject");
        mEventName = findViewById(R.id.sEventName);

        mEventName.setText(mEvent.getName());

    }
}
