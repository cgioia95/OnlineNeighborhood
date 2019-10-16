package com.example.onlineneighborhood;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Calendar;
import android.icu.text.SimpleDateFormat;

import de.hdodenhof.circleimageview.CircleImageView;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private static final String TAG = "Event Adapter";

    private static ArrayList<Event> eventList;
    private static Context mContext;
    private onEventClickListener mListener;


//    private onEventLongClickListener mListener2;






    public interface onEventClickListener {
        void onEventClick(int position);
    }


//    public interface onEventLongClickListener {
//        void onEventLongClick(int position);
//    }



    public void setOnEventClickListener(onEventClickListener listener) {
        mListener = listener;
    }


//    public void setOnEventLongClickListener(onEventLongClickListener listener) {
//        mListener2 = listener;
//    }


    public static class EventViewHolder extends RecyclerView.ViewHolder {

        public TextView mEvent, mUserName, mEventTime, mEventAddress, mEventAttending, mEventDate;
        public CircleImageView hostPic;
        private FirebaseStorage storage;
        private StorageReference storageReference;
        private DatabaseReference databaseReference;


        public EventViewHolder(@NonNull View itemView, final onEventClickListener listener) {
            super(itemView);

            mEvent = itemView.findViewById(R.id.eventName);
            mUserName = itemView.findViewById(R.id.userName);
            mEventTime = itemView.findViewById(R.id.eventTime);
            mEventDate = itemView.findViewById(R.id.eventDate);
            mEventAddress = itemView.findViewById(R.id.eventAddress);
            mEventAttending = itemView.findViewById(R.id.eventAttending);
            hostPic = itemView.findViewById(R.id.imageView);
            storage = FirebaseStorage.getInstance();
            databaseReference = FirebaseDatabase.getInstance().getReference("Users");
            storageReference=storage.getReference();

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(listener!=null) {
                        int position = getAdapterPosition();
                        Log.d(TAG, "In onclick in eventadapter: position:" + position);
                        if(position!= RecyclerView.NO_POSITION) {
                            listener.onEventClick(position);
                        }

                    }

                }
            });


//            itemView.setOnLongClickListener(new View.OnLongClickListener() {
//                @Override
//                public boolean onLongClick(View view) {
//
//                    if(listener2!=null) {
//                        int position = getAdapterPosition();
//                        Log.d(TAG, "In onclick in eventadapter: position:" + position);
//                        if(position!= RecyclerView.NO_POSITION) {
//                            listener2.onEventLongClick(position);
//                        }
//
//                    }
//
//                    return true;
//
//                }
//            });


        }

        private void getUsername(String uid){
            databaseReference.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Log.d("On DATA CHANGE ", "IN");
                    Log.d("On DATA CHANGE", "Snapshot" + dataSnapshot);
                    if (dataSnapshot.getValue() != null) {
                        String name = dataSnapshot.child("name").getValue().toString();

                        mUserName.setText(name);

                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });

        }



        protected void downloadImage(String uid) {
            storageReference.child("profilePics/" + uid).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    // Got the download URL for 'users/me/profile.png' in uri
                    Log.d(TAG, "DOWNLOAD URL: " + uri.toString());
                    Picasso.get().load(uri).into(hostPic);
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
                            Picasso.get().load(uri).into(hostPic);
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

    public EventAdapter(ArrayList<Event> eventList, Context context) {
        this.eventList = eventList;
        this.mContext = context;
    }


    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.event_item, parent, false);
        EventViewHolder viewholder = new EventViewHolder(v, mListener);
        return viewholder;


    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event currentItem = eventList.get(position);

        //FILTER LOGIC HERE
        Log.d(TAG, "user id " + currentItem.getHost().getUid());
        holder.mEvent.setText(currentItem.getName());
        //TODO: get the name of the hostID - DONE
        holder.getUsername(currentItem.getHost().getUid());
        holder.mEventTime.setText(currentItem.getTime());
        holder.mEventAddress.setText(currentItem.getAddress());


        //Change the date format to display the date and day of the week
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Date date = null;
        try {
            date = sdf.parse(currentItem.getDate());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        sdf.applyPattern("EEE d MMM"); //new cal pattern
        String newDate = sdf.format(date);
        holder.mEventDate.setText(newDate);

        //Get number of attendees
        String size = "" +  currentItem.getAttendees().size();
        holder.mEventAttending.setText(size);
        holder.downloadImage(currentItem.getHost().getUid());

    }


    @Override
    public int getItemCount() {
        return eventList.size();
    }


}
