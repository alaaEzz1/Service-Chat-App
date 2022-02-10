package com.elmohandes.serviceschat.Screens;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.elmohandes.serviceschat.Models.UserProfile;
import com.elmohandes.serviceschat.R;
import com.elmohandes.serviceschat.databinding.ContactsFragmentBinding;
import com.elmohandes.serviceschat.databinding.FindFriendsItemBinding;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ContactsScreen extends Fragment {

    ContactsFragmentBinding binding;
    DatabaseReference cantactsRef;
    FirebaseAuth auth;
    String currentUserId;
    DatabaseReference usersRef;

    public ContactsScreen() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.contacts_fragment, container, false);

        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser().getUid();
        cantactsRef = FirebaseDatabase.getInstance().getReference().child("contacts")
                .child(currentUserId);
        usersRef = FirebaseDatabase.getInstance().getReference().child("users");

        binding = ContactsFragmentBinding.bind(view);
        binding.contactsRecycler.setLayoutManager(new LinearLayoutManager(getContext()));

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<UserProfile>()
                .setQuery(cantactsRef,UserProfile.class).build();
        FirebaseRecyclerAdapter<UserProfile,ConHolder> adapter =
                new FirebaseRecyclerAdapter<UserProfile, ConHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ConHolder holder, int position,
                                            @NonNull UserProfile model) {

                //ids in accepted friendship users
                String userIds = getRef(position).getKey();
                //get users infos which we accepted in contacts object in database
                usersRef.child(userIds).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (snapshot.exists()) {

                            //check online ,last seen and time state of user
                            if (snapshot.child("userState").hasChild("state")){

                                String state = snapshot.child("userState").child("state")
                                        .getValue().toString();
                                String date = snapshot.child("userState").child("date")
                                        .getValue().toString();
                                String time = snapshot.child("userState").child("time")
                                        .getValue().toString();

                                if (state.equals("online")){
                                    holder.binding.friendsItemOnline.setVisibility(View.VISIBLE);
                                }else if (state.equals("offline")){
                                    holder.binding.friendsItemOnline.setVisibility(View.INVISIBLE);
                                }

                            }else {

                                holder.binding.friendsItemOnline.setVisibility(View.INVISIBLE);

                            }

                            if (snapshot.hasChild("image")) {

                                String profileImage = snapshot.child("image").getValue().toString();
                                String bio = snapshot.child("bio").getValue().toString();
                                String name = snapshot.child("full_name").getValue().toString();

                                holder.binding.friendsItemName.setText(name);
                                holder.binding.friendsItemBio.setText(bio);
                                Picasso.get().load(profileImage).placeholder(R.drawable.person_or_avatar)
                                        .into(holder.binding.friendsItemImg);

                            } else {
                                String bio = snapshot.child("bio").getValue().toString();
                                String name = snapshot.child("full_name").getValue().toString();

                                holder.binding.friendsItemName.setText(name);
                                holder.binding.friendsItemBio.setText(bio);
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
            public ConHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View v = LayoutInflater.from(parent.getContext()).
                        inflate(R.layout.find_friends_item,parent,false);

                return new ConHolder(v);
            }
        };

        binding.contactsRecycler.setAdapter(adapter);
        adapter.startListening();

    }


    public class ConHolder extends RecyclerView.ViewHolder{

        FindFriendsItemBinding binding;

        public ConHolder(@NonNull View itemView) {
            super(itemView);

            binding = FindFriendsItemBinding.bind(itemView);

        }
    }

}