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

import java.util.ArrayList;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private static final String TAG = "User Adapter";

    private static ArrayList<UserInformation> userList;
    private static Context mContext;
    private onUserClickListener mListener;


    public interface onUserClickListener {
        void onEventClick(int position);
    }


    public void setOnUserClickListener(onUserClickListener listener) {
        mListener = listener;
    }


    public static class UserViewHolder extends RecyclerView.ViewHolder {

        public TextView mUserName;
        public TextView mUserBio;
//        public ImageView hostPic;
//        private FirebaseStorage storage;
//        private StorageReference storageReference;
        private DatabaseReference databaseReference;


        public UserViewHolder(@NonNull View itemView, final onUserClickListener listener) {
            super(itemView);

            mUserName = itemView.findViewById(R.id.userNameU);
            mUserBio = itemView.findViewById(R.id.userBioU);


//            hostPic = itemView.findViewById(R.id.imageViewU);

//            storage = FirebaseStorage.getInstance();
            databaseReference = FirebaseDatabase.getInstance().getReference("Users");
//            storageReference=storage.getReference();

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(listener!=null) {
                        int position = getAdapterPosition();
                        Log.d(TAG, "In onclick in useradapter: position:" + position);
                        if(position!= RecyclerView.NO_POSITION) {
                            listener.onEventClick(position);
                        }

                    }

                }
            });


        }

//        private void getUsername(String uid){
//            databaseReference.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(DataSnapshot dataSnapshot) {
//                    Log.d("On DATA CHANGE ", "IN");
//                    Log.d("On DATA CHANGE", "Snapshot" + dataSnapshot);
//                    if (dataSnapshot.getValue() != null) {
//                        String name = dataSnapshot.child("name").getValue().toString();
//
//                        mUserName.setText(name);
//
//                    }
//
//                }
//
//                @Override
//                public void onCancelled(DatabaseError databaseError) {
//                }
//            });
//
//        }



//        protected void downloadImage(String uid) {
//            storageReference.child("profilePics/" + uid).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
//                @Override
//                public void onSuccess(Uri uri) {
//                    // Got the download URL for 'users/me/profile.png' in uri
//                    Log.d(TAG, "DOWNLOAD URL: " + uri.toString());
//                    Picasso.get().load(uri).into(hostPic);
//                    return;
//
//
//                }
//            }).addOnFailureListener(new OnFailureListener() {
//                @Override
//                public void onFailure(@NonNull Exception exception) {
//                    // Handle any errors
//                    Log.d(TAG, "DOWNLOAD URL: FAILURE");
//                    storageReference.child("profilePics/" + "default.png").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
//                        @Override
//                        public void onSuccess(Uri uri) {
//                            // Got the download URL for 'users/me/profile.png' in uri
//                            Log.d(TAG, "DOWNLOAD URL: " + uri.toString());
//                            Picasso.get().load(uri).into(hostPic);
//                            return;
//
//
//                        }
//                    }).addOnFailureListener(new OnFailureListener() {
//                        @Override
//                        public void onFailure(@NonNull Exception exception) {
//                            // Handle any errors
//                            Log.d(TAG, "DOWNLOAD URL: FAILURE");
//
//                        }
//                    });
//
//                }
//            });
//
//
//
//
//        }
    }

    public UserAdapter(ArrayList<UserInformation> userList, Context context) {
        this.userList = userList;
        this.mContext = context;
    }



    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item, parent, false);
        UserViewHolder viewholder = new UserViewHolder(v, mListener);
        return viewholder;

    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {

        UserInformation currentItem = userList.get(position);

        Log.d(TAG, "user id " + currentItem.getUid());

        holder.mUserName.setText(currentItem.getName());

        holder.mUserBio.setText(currentItem.getBio());

//        holder.downloadImage(currentItem.getUid());

//        holder.getUsername(currentItem.getUid());



    }

    @Override
    public int getItemCount() {
        return userList.size();
    }
}
