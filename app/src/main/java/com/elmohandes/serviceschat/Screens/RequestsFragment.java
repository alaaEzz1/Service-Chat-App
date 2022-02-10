
    package com.elmohandes.serviceschat.Screens;

import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.elmohandes.serviceschat.Models.UserProfile;
import com.elmohandes.serviceschat.R;
import com.elmohandes.serviceschat.databinding.FindFriendsItemBinding;
import com.elmohandes.serviceschat.databinding.FragmentRequestsBinding;
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


    public class RequestsFragment extends Fragment {

    FragmentRequestsBinding binding;
    DatabaseReference requestsReference , usersRef , contactsRef;
    FirebaseAuth auth;
    String currentUserId;

    public RequestsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_requests, container, false);

        binding = FragmentRequestsBinding.bind(view);
        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser().getUid();
        requestsReference = FirebaseDatabase.getInstance().getReference().child("chat requests");
        usersRef = FirebaseDatabase.getInstance().getReference().child("users");
        contactsRef = FirebaseDatabase.getInstance().getReference().child("contacts");

        binding.requestsRecycler.setLayoutManager(new LinearLayoutManager(getContext()));


        return view;
    }

        @Override
        public void onStart() {
            super.onStart();


            FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<UserProfile>()
                    .setQuery(requestsReference.child(currentUserId),UserProfile.class).build();

            FirebaseRecyclerAdapter<UserProfile , RQHolder> adapter = new FirebaseRecyclerAdapter
                    <UserProfile, RQHolder>(options) {
                @Override
                protected void onBindViewHolder(@NonNull RQHolder holder, int position,
                                                @NonNull UserProfile model) {

                    holder.binding.friendsAccept.setVisibility(View.VISIBLE);
                    holder.binding.friendsCancel.setVisibility(View.VISIBLE);

                    String requestsIds = getRef(position).getKey();
                    DatabaseReference getType =getRef(position).child("request_type");
                    getType.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()){
                                //get received requests
                                String type = snapshot.getValue().toString();
                                if (type.equals("received")){
                                    getInfoReceivedUsers(holder , requestsIds);
                                }
                                else if (type.equals("sent")){

                                    holder.binding.friendsCancel.setText(R.string.cancel_sent_request);
                                    holder.binding.friendsAccept.setVisibility(View.INVISIBLE);

                                    getInfoSentUsers(holder , requestsIds);

                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                }

                @NonNull
                @Override
                public RQHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                    View v = LayoutInflater.from(parent.getContext()).
                            inflate(R.layout.find_friends_item,parent , false);

                    return new RQHolder(v);
                }
            };

            binding.requestsRecycler.setAdapter(adapter);
            adapter.startListening();

        }

        private void getInfoSentUsers(RQHolder holder, String requestsIds) {

            usersRef.child(requestsIds).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    if (snapshot.hasChild("image")){

                        String profileImage = snapshot.child("image").getValue().toString();

                        Picasso.get().load(profileImage).placeholder(R.drawable.person_or_avatar)
                                .into(holder.binding.friendsItemImg);

                    }

                    //String bio = snapshot.child("bio").getValue().toString();
                    String name = snapshot.child("full_name").getValue().toString();

                    holder.binding.friendsItemName.setText(name);
                    holder.binding.friendsItemBio.setText(getString(R.string.
                            you_already_sent_friend_request) + name);

                    holder.binding.friendsCancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            removeValuesFromChatRequest(requestsIds,"Contact Deleted");
                        }
                    });

                    holder.itemView.setOnClickListener(view -> {
                        CharSequence sequence[] = new CharSequence[]{R.string.cancel_sent_request+""};

                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                        //show dialog for accept or cancel
                        builder.setItems(sequence, (dialogInterface, i) -> {

                            if (i == 0){
                                //user clicked cancel
                                removeValuesFromChatRequest(requestsIds,"Contact Deleted");
                            }



                        });


                        builder.show();
                    });

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }

        private void getInfoReceivedUsers(RQHolder holder, String requestsIds) {

        usersRef.child(requestsIds).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.hasChild("image")){

                    String profileImage = snapshot.child("image").getValue().toString();

                    Picasso.get().load(profileImage).placeholder(R.drawable.person_or_avatar)
                            .into(holder.binding.friendsItemImg);

                }

                //String bio = snapshot.child("bio").getValue().toString();
                String name = snapshot.child("full_name").getValue().toString();

                holder.binding.friendsItemName.setText(name);
                holder.binding.friendsItemBio.setText(R.string.i_want_to_connect_with_you);

                holder.binding.friendsAccept.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        acceptChatRequest(requestsIds);
                    }
                });

                holder.binding.friendsCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        removeValuesFromChatRequest(requestsIds,getString(R.string.contact_deleted));
                    }
                });

                holder.itemView.setOnClickListener(view -> {
                    CharSequence sequence[] = new CharSequence[]{"Accept" , "Cancel"};

                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                    //show dialog for accept or cancel
                    builder.setItems(sequence, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            if (i ==0){
                                //user clicked Accept
                                acceptChatRequest(requestsIds);

                            }
                            if (i == 1){
                                //user clicked cancel
                                removeValuesFromChatRequest(requestsIds,"Contact Deleted");
                            }



                        }
                    });


                    builder.show();
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        }

        private void acceptChatRequest(String requestsIds) {

            contactsRef.child(currentUserId).child(requestsIds).child("contacts")
                    .setValue("saved").addOnCompleteListener(task -> {
                        if (task.isSuccessful()){

                            contactsRef.child(requestsIds).child(currentUserId).child("contacts")
                                    .setValue("saved").addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()){

                                            removeValuesFromChatRequest(requestsIds ,
                                                    "New Contact Added");

                                        }
                                    });

                        }
                    });

        }

        private void removeValuesFromChatRequest(String requestsIds , String message) {

        requestsReference.child(currentUserId).child(requestsIds).removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        requestsReference.child(requestsIds).child(currentUserId).removeValue()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()){
                                            Toast.makeText(getContext(),
                                                    message, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    }
                });

        }


        public class RQHolder extends RecyclerView.ViewHolder{

        FindFriendsItemBinding binding;

            public RQHolder(@NonNull View itemView) {
                super(itemView);

                binding = FindFriendsItemBinding.bind(itemView);

            }
        }

    }