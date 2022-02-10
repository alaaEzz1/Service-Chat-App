package com.elmohandes.serviceschat.Screens;

import android.content.Intent;
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
import com.elmohandes.serviceschat.databinding.FindFriendsItemBinding;
import com.elmohandes.serviceschat.databinding.FragmentChatBinding;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;


public class ChatsFragment extends Fragment {

    FragmentChatBinding binding;
    FirebaseAuth auth;
    DatabaseReference chatRef , usersRef;
    String currentUserId;


    public ChatsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        binding = FragmentChatBinding.bind(view);
        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser().getUid();
        chatRef = FirebaseDatabase.getInstance().getReference().
                child("contacts").child(currentUserId);
        usersRef = FirebaseDatabase.getInstance().getReference().child("users");

        binding.chatRecyclerOnversation.setLayoutManager(new LinearLayoutManager(getContext()));

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<UserProfile>()
                .setQuery(chatRef,UserProfile.class).build();

        FirebaseRecyclerAdapter<UserProfile,CONHolder> adapter = new FirebaseRecyclerAdapter
                <UserProfile, CONHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull CONHolder holder, int position, @NonNull UserProfile model) {

                String usersIds = getRef(position).getKey();

                //you must declare this variable their until not save the last value of image only
                final String[] profileImage = {"default_img"};

                usersRef.child(usersIds).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (snapshot.exists()){

                            if (snapshot.hasChild("image")){

                                profileImage[0] = snapshot.child("image").getValue().toString();

                                Picasso.get().load(profileImage[0]).placeholder(R.drawable.person_or_avatar)
                                        .into(holder.binding.friendsItemImg);

                                holder.binding.friendsItemImg.setOnClickListener(view -> {
                                    Intent intent = new Intent(holder.itemView.getContext()
                                            , ImageviewerActivity.class);
                                    intent.putExtra("imgUrl" , profileImage[0]);
                                    holder.itemView.getContext().startActivity(intent);
                                });

                            }

                            //String bio = snapshot.child("bio").getValue().toString();
                            String name = snapshot.child("full_name").getValue().toString();

                            //check online ,last seen and time state of user
                            if (snapshot.child("userState").hasChild("state")){

                                String state = snapshot.child("userState").child("state")
                                        .getValue().toString();
                                String date = snapshot.child("userState").child("date")
                                        .getValue().toString();
                                String time = snapshot.child("userState").child("time")
                                        .getValue().toString();

                                if (state.equals("online")){
                                    holder.binding.friendsItemBio.setText("online");
                                }else if (state.equals("offline")){
                                    holder.binding.friendsItemBio.
                                            setText("last seen :" + date +" " + time);
                                }

                            }else {

                                holder.binding.friendsItemBio.setText("Not updated");

                            }

                            holder.binding.friendsItemName.setText(name);

                            holder.itemView.setOnClickListener(view -> {
                                Intent intent = new Intent(getContext() , ChatActivity.class);
                                intent.putExtra("conversation_id" , usersIds);
                                intent.putExtra("conversation_name" , name);
                                intent.putExtra("conversation_img" , profileImage[0]);
                                startActivity(intent);
                            });


                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }

            @NonNull
            @Override
            public CONHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View v = LayoutInflater.from(parent.getContext()).
                        inflate(R.layout.find_friends_item , parent ,false);

                return new CONHolder(v);
            }
        };

        binding.chatRecyclerOnversation.setAdapter(adapter);

        adapter.startListening();

    }

    public class CONHolder extends RecyclerView.ViewHolder{

        FindFriendsItemBinding binding;

        public CONHolder(@NonNull View itemView) {
            super(itemView);

            binding = FindFriendsItemBinding.bind(itemView);

        }
    }

}