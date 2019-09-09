package com.example.onlineneighborhood;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private ArrayList<EventCard> eventCardList;
    private Context mContext;

    public static class EventViewHolder extends RecyclerView.ViewHolder {

        public TextView mEvent;
        public TextView mUserName;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);

           mEvent = itemView.findViewById(R.id.eventName);
           mUserName = itemView.findViewById(R.id.userName);

        }
    }

    public EventAdapter(ArrayList<EventCard> eventCardList, Context context) {
        this.eventCardList = eventCardList;
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
        EventCard currentItem = eventCardList.get(position);
        holder.mEvent.setText(currentItem.getmEvent());
        holder.mUserName.setText(currentItem.getmUserName());


    }

    @Override
    public int getItemCount() {
        return eventCardList.size();
    }


}
