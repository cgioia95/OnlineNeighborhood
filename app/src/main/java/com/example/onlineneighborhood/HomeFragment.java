package com.example.onlineneighborhood;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class HomeFragment extends Fragment implements View.OnClickListener, Serializable {

    TextView suburbTextView;


    private ImageView addEvent, dateFilter, typeFilter;
    private String suburb;
    private Callbacks mCallbacks;
    private static final String TAG = "HomeScreen";
    static Date calenderDate;
    private Suburb currentSuburb;
    private static String type;
    private Button applyFilterButton, clearFilter;

    private FirebaseAuth fireBaseAuth;
    DatabaseReference databaseEvents;

    //Setting up recyclerview and adapter for displaying events
    private RecyclerView mRecyclerView;
    private EventAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArrayList<Event> eventList = new ArrayList<>();



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.activity_home_screen, null);

        fireBaseAuth = FirebaseAuth.getInstance();
        addEvent = mView.findViewById(R.id.addEvent);
        dateFilter = mView.findViewById(R.id.dateFilterHome);
        typeFilter = mView.findViewById(R.id.typeFilter);
        suburbTextView = mView.findViewById(R.id.textViewSuburb);
        applyFilterButton = mView.findViewById(R.id.applyFilterButton);
        clearFilter = mView.findViewById(R.id.clearFilter);

        suburb = ((OnlineNeighborhood) getActivity().getApplication()).getsuburb();

        addEvent.setOnClickListener(this);
        applyFilterButton.setOnClickListener(this);
        dateFilter.setOnClickListener(this);
        typeFilter.setOnClickListener(this);
        clearFilter.setOnClickListener(this);

        return mView;
    }
    @Override
    public void onStart(){
        super.onStart();

        databaseEvents = FirebaseDatabase.getInstance().getReference("suburbs").child(suburb);

        databaseEvents.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                eventList.clear();
                currentSuburb = dataSnapshot.getValue(Suburb.class);
                suburbTextView.setText(currentSuburb.getSubName());
                if(currentSuburb.getEvents() != null){
                    ArrayList<Event> events = currentSuburb.getEvents();
                    for(Event event: events){
                        if(event != null) {
                            try {
                                filterApplied(calenderDate, type, event);
                            } catch (ParseException e) {
                                eventList.add(event);
                                e.printStackTrace();
                            }
                        }
                    }
                }

                //SET UP EVENTLIST
                //i think when theres no list of events this throws. not sure why but just put an error check on it
                //TODO: NEED TO FIX THIS ASAP. (turns out it still crashes even with this)
                try{
                    mRecyclerView = getActivity().findViewById(R.id.recyclerView);
                    mLayoutManager = new LinearLayoutManager(getActivity());
                    mAdapter = new EventAdapter(eventList, getActivity());

                    mRecyclerView.setLayoutManager(mLayoutManager);
                    mRecyclerView.setAdapter(mAdapter);


                }catch (NullPointerException e){
                    e.printStackTrace();
                }

                if(mAdapter!=null) {
                    mAdapter.setOnEventLongClickListener(new EventAdapter.onEventLongClickListener() {
                        @Override
                        public void onEventLongClick(int position) {

                            Event event = eventList.get(position);

                            String hostId = event.getHost().getUid();

                            String myId = fireBaseAuth.getCurrentUser().getUid();
                            Log.d(TAG, "Long Click");


                            if (myId.equals(hostId)) {
                                Log.d(TAG, "Permission Granted");

                                Intent intent = new Intent(getActivity(), editDelete.class);
                                intent.putExtra("MyObject", event);
                                intent.putExtra("SUBURB", suburb);


                                startActivity(intent);

                            } else {
                                Log.d(TAG, "Permission Denied");
                            }


                        }
                    });

                    mAdapter.setOnEventClickListener(new EventAdapter.onEventClickListener() {
                        @Override
                        public void onEventClick(int position) {
                            Event event = eventList.get(position);

                            Intent intent = new Intent(getActivity(), EventScreen.class);

                            Log.d(TAG, "Single Click");

                            intent.putExtra("MyObject", event);
                            intent.putExtra("SUBURB", suburb);

                            startActivity(intent);
                        }


                    });
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    //@Override
    public void onClick(View view) {

        if (view == addEvent){
            Intent i = new Intent(getActivity(), createEvent.class);
            i.putExtra("SUBURB", suburb);
            startActivity(i);
        }

        if (view == dateFilter){
            DialogFragment newFragment = new SelectDateFragment();
            newFragment.show(getFragmentManager(), "DatePicker");
        }

        if(view == typeFilter){
            openTypeDialog();

        }

        if(view == applyFilterButton){
            mCallbacks.onButtonClicked();
        }

        if(view == clearFilter){
            type = null;
            calenderDate = null;
            mCallbacks.onButtonClicked();
        }

    }

    public void filterApplied(Date date, String type, Event event) throws ParseException {
        String eventStartDate = event.getDate() + " " + event.getTime();
        String eventEndDate = event.getEndDate() + " " + event.getEndTime();
        Date eventStart = new SimpleDateFormat("dd/MM/yyyy").parse(eventStartDate);
        Date eventEnd = new SimpleDateFormat("dd/MM/yyyy").parse(eventEndDate);
        if(date == null && type == null){
            eventList.add(event);
        }else if(date != null && type == null){
                if((eventStart.after(date) || eventStart.equals(date)) && (eventEnd.before(date)|| eventEnd.equals(date))) {
                    eventList.add(event);
                }

        } else if( date == null && type != null){
                if(event.getType().equals(type)){
                 eventList.add(event);
                }
        } else{
            if((eventStart.after(date) || eventStart.equals(date)) && (eventEnd.before(date)|| eventEnd.equals(date)) && event.getType().equals(type)) {
                eventList.add(event);
            }
        }
    }

    // NEED TO REFERENCE THIS
    public static class SelectDateFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar calendar = Calendar.getInstance();
            int yy = calendar.get(Calendar.YEAR);
            int mm = calendar.get(Calendar.MONTH);
            int dd = calendar.get(Calendar.DAY_OF_MONTH);
            return new DatePickerDialog(getActivity(), this, yy, mm, dd);
        }

        public void onDateSet(DatePicker view, int yy, int mm, int dd) {
            populateSetDate(yy, mm+1, dd);
        }
        public Date populateSetDate(int year, int month, int day) {

            calenderDate = new Date(year - 1900, month - 1, day);
            Log.d("CALENDER DATE POPULATE" , calenderDate.toString());

            return calenderDate;
        }

    }

    public void openTypeDialog(){
        TypeDialog typeDialog = new TypeDialog();
        typeDialog.show(getFragmentManager(), "Example Dialog");

    }

    public static class TypeDialog extends AppCompatDialogFragment {
        private Spinner typeSpinner;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View view = inflater.inflate(R.layout.layout_type_dialog, null);

            typeSpinner = view.findViewById(R.id.typeFilterSpinner);
            builder.setTitle("Choose A type")
                    .setView(view)
                    .setMessage("What kind of event are you looking for?")
                    .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    })
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            type = typeSpinner.getSelectedItem().toString();
                        }
                    });

            return builder.create();
        }

    }

    public interface Callbacks {
        //Callback for when button clicked.
        public void onButtonClicked();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Activities containing this fragment must implement its callbacks
        mCallbacks = (Callbacks) activity;

    }




}