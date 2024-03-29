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

import de.hdodenhof.circleimageview.CircleImageView;



// Class for listing and displaying users who are attending/hosting events
public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private static final String TAG = "User Adapter";

    // declare simple variables
    private static ArrayList<UserInformation> userList;
    private static Context mContext;
    private onUserClickListener mListener;

    // Click listener that brings us to the user's profile page
    public interface onUserClickListener {
        void onEventClick(int position);
    }

    // Method to set this listener
    public void setOnUserClickListener(onUserClickListener listener) {
        mListener = listener;
    }


    // The actual view of the user we see
    public static class UserViewHolder extends RecyclerView.ViewHolder {

        public TextView mUserName;
        public CircleImageView hostPic;
        private FirebaseStorage storage;
        private StorageReference storageReference;
        private DatabaseReference databaseReference;



        public UserViewHolder(@NonNull View itemView, final onUserClickListener listener) {
            super(itemView);

            // Bind variables and get database references
            mUserName = itemView.findViewById(R.id.userNameU);


            hostPic = itemView.findViewById(R.id.imageViewU);

            storage = FirebaseStorage.getInstance();
            databaseReference = FirebaseDatabase.getInstance().getReference("Users");
            storageReference=storage.getReference();

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


        // Retrieves the user's image from firebase data storage
        // Returns a simple default image if unavailable
        protected void downloadImage(String uid) {

            Log.d(TAG, "Attempting to download image of  user with id: " + uid);

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

    public UserAdapter(ArrayList<UserInformation> userList, Context context) {
        this.userList = userList;
        this.mContext = context;
    }


    // Binds the image/text of the user to a predefined layout
    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.attending_user, parent, false);
        UserViewHolder viewholder = new UserViewHolder(v, mListener);
        return viewholder;

    }

    // Retrieve user's information and binds it
    @Override
    public void onBindViewHolder(@NonNull final UserViewHolder holder, int position) {

        final UserInformation currentItem = userList.get(position);

        final String uid = currentItem.getUid();

        Log.d(TAG, "BIND VIEW ID IS: " +  uid);

        Log.d(TAG, "user id " + currentItem.getUid());


        DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("Users").child(uid);

        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                UserInformation user = dataSnapshot.getValue(UserInformation.class);

                String name = user.getName();
                String bio = user.getBio();


                holder.mUserName.setText(name);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });




//
        holder.downloadImage(currentItem.getUid());







    }

    @Override
    public int getItemCount() {
        return userList.size();
    }
}
