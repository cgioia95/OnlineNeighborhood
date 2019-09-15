package com.example.onlineneighborhood;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private static final String TAG = "Event Adapter";

    private ArrayList<Event> eventList;
    private Context mContext;

    public static class EventViewHolder extends RecyclerView.ViewHolder {

        public TextView mEvent;
        public TextView mUserName;
        public TextView mEventTime;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);

           mEvent = itemView.findViewById(R.id.eventName);
           mUserName = itemView.findViewById(R.id.userName);
           mEventTime = itemView.findViewById(R.id.eventTime);

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
        EventViewHolder viewholder = new EventViewHolder(v);
        return viewholder;

    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event currentItem = eventList.get(position);
        holder.mEvent.setText(currentItem.getName());
        holder.mUserName.setText(currentItem.getHost().getName()); //TODO: get the name of the host
        holder.mEventTime.setText(currentItem.getTime());


        Log.d(TAG, "onBindViewHolder: " + currentItem.getHost());


    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }


}
