package com.example.onlineneighborhood;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

public class EventScreen extends AppCompatActivity {
    private static final String TAG = "EventScreen";

    public TextView mEventName, mDescription, mTime, mDate;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_screen);

        Event mEvent= (Event) getIntent().getSerializableExtra("MyObject");

        mEventName = findViewById(R.id.eventName);
        mDescription = findViewById(R.id.eventDesc);
        mDate = findViewById(R.id.eventDate);
        mTime = findViewById(R.id.eventTime);

        Log.d(TAG, "onCreate: eventName " + mEvent.getName());
        Log.d(TAG, "onCreate: eventDesc " + mEvent.getDescription());
        Log.d(TAG, "onCreate: eventTime" + mEvent.getTime());

        mEventName.setText(mEvent.getName());
        mDescription.setText(mEvent.getDescription());
        mDate.setText(mEvent.getDate());
        mTime.setText(mEvent.getTime());

    }
}
