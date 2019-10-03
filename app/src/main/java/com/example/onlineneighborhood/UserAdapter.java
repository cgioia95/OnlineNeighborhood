package com.example.onlineneighborhood;

import android.content.Context;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class UserAdapter extends RecyclerView.Adapter {

    private static final String TAG = "Event Adapter";

    private static ArrayList<Event> eventList;
    private static Context mContext;
    private onUserClickListener mListener;


    public interface onUserClickListener {
        void onEventClick(int position);
    }


    public void setOnEventClickListener(onUserClickListener listener) {
        mListener = listener;
    }



    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }
}
