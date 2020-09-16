package com.example.videocall;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Notification;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class NotificationActivity extends AppCompatActivity {

    private RecyclerView Notification_list;
    private DatabaseReference friendRequestRef, contactRef, userRef;
    private FirebaseAuth mAuth;
    private String currentUserID;
    private String listUserId =  "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        friendRequestRef = FirebaseDatabase.getInstance().getReference().child("Friend Request");
        contactRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        userRef = FirebaseDatabase.getInstance().getReference().child("users");

        Notification_list = findViewById(R.id.Notification_list);
        Notification_list.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

    }


    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Contacts>().setQuery(friendRequestRef.child(currentUserID), Contacts.class).build();

        FirebaseRecyclerAdapter<Contacts, NotificationViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Contacts, NotificationViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final NotificationViewHolder holder, int i, @NonNull Contacts contacts) {
                holder.acceptBtn.setVisibility(View.VISIBLE);
                holder.cancelBtn.setVisibility(View.VISIBLE);

                final String listUserId = getRef(i).getKey();

                DatabaseReference requestTypeRef = getRef(i).child("request_type").getRef();
                requestTypeRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            String type = dataSnapshot.getValue().toString();

                            if (type.equals("received")){
                                holder.cardView.setVisibility(View.VISIBLE);

                                userRef.child(listUserId).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.hasChild("image")){
                                            final String imageStr = dataSnapshot.child("image").getValue().toString();

                                            Picasso.get().load(imageStr).into(holder.profileImage);
                                        }

                                        final String nameStr = dataSnapshot.child("name").getValue().toString();
                                        holder.userNameTxt.setText(nameStr);

                                        holder.acceptBtn.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                AcceptFriendRequest();

                                            }
                                        });

                                        holder.cancelBtn.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                cancelFriendRequest();

                                            }
                                        });
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });

                            }
                            else{
                                holder.cardView.setVisibility(View.GONE);
                            }
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });



            }

            @NonNull
            @Override
            public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.find_friend_design, parent, false);
                NotificationViewHolder viewHolder = new NotificationViewHolder(view);
                return viewHolder;
            }
        };

        Notification_list.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();

    }

    public static class NotificationViewHolder extends RecyclerView.ViewHolder {

        TextView userNameTxt;
        Button acceptBtn, cancelBtn;
        ImageView profileImage;
        RelativeLayout cardView;


        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);

            userNameTxt = itemView.findViewById(R.id.name_notification);
            acceptBtn = itemView.findViewById(R.id.request_accept_btn);
            cancelBtn = itemView.findViewById(R.id.request_decline_btn);
            profileImage = itemView.findViewById(R.id.image_notification);
            cardView = itemView.findViewById(R.id.card_view);

        }
    }

    private void cancelFriendRequest() {
        friendRequestRef.child(currentUserID).child(listUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if(task.isSuccessful()){
                    friendRequestRef.child(currentUserID).child(listUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if(task.isSuccessful()){
                                Toast.makeText(NotificationActivity.this, "Friend Request Cancelled.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }

    private void AcceptFriendRequest() {

        contactRef.child(currentUserID).child(listUserId).child("Contact").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    contactRef.child(listUserId).child(currentUserID).child("Contact").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                friendRequestRef.child(currentUserID).child(listUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        if(task.isSuccessful()){
                                            friendRequestRef.child(listUserId).child(currentUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {

                                                    if(task.isSuccessful()){
                                                        Toast.makeText(NotificationActivity.this, "New Contact Saved.", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                        }
                                    }
                                });

                            }
                        }
                    });
                }
            }
        });
    }
}