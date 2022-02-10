package com.elmohandes.serviceschat.Screens;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.elmohandes.serviceschat.Models.UserProfile;
import com.elmohandes.serviceschat.R;
import com.elmohandes.serviceschat.Tools.FindFriendsHolder;
import com.elmohandes.serviceschat.databinding.ActivityFindFriendsBinding;
import com.firebase.ui.database.FirebaseListOptions;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.HashMap;

public class FindFriendsActivity extends AppCompatActivity {


    ActivityFindFriendsBinding binding;
    DatabaseReference reference;
    String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        binding = ActivityFindFriendsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().setTitle("Find Friends");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        reference = FirebaseDatabase.getInstance().getReference().child("users");
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void updateCurrentStatus(String state){

        String saveCurrentTime , saveCurrentDate;
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("yyyy:MM:dd");
        saveCurrentDate = currentDate.format(calendar.getTime());
        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());

        HashMap<String , Object> onlineState = new HashMap<>();
        onlineState.put("time" , saveCurrentTime);
        onlineState.put("date" , saveCurrentDate);
        onlineState.put("state" , state);

        FirebaseDatabase.getInstance().getReference().child("users").child(currentUserId)
                .child("userState").updateChildren(onlineState);

    }


    @Override
    protected void onStart() {
        super.onStart();


        FirebaseRecyclerOptions<UserProfile> options = new FirebaseRecyclerOptions.Builder<UserProfile>()
                .setQuery(reference,UserProfile.class)
                .build();

        FirebaseRecyclerAdapter<UserProfile, FindFriendsHolder> adapter =
                new FirebaseRecyclerAdapter<UserProfile, FindFriendsHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull FindFriendsHolder holder, @SuppressLint
                            ("RecyclerView") int position, @NonNull UserProfile model) {

                        holder.binding.friendsItemName.setText(model.getFull_name());
                        holder.binding.friendsItemBio.setText(model.getBio());
                        Picasso.get().load(model.getImage()).placeholder(R.drawable.person_or_avatar).
                                into(holder.binding.friendsItemImg);

                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                String profile_visit_id = getRef(position).getKey();
                                Intent intent = new Intent(FindFriendsActivity.this ,
                                        ProfileActivity.class) ;
                                intent.putExtra("profile_visit_id" ,profile_visit_id);
                                startActivity(intent);
                            }
                        });

                    }

                    @NonNull
                    @Override
                    public FindFriendsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                        View view = LayoutInflater.from(parent.getContext()).
                                inflate(R.layout.find_friends_item,parent,false);

                        return new FindFriendsHolder(view);
                    }
                };

        binding.findFriendsRecycler.setAdapter(adapter);
        binding.findFriendsRecycler.setLayoutManager(new LinearLayoutManager(this));
        adapter.startListening();

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onPause() {
        super.onPause();

        updateCurrentStatus("offline");

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onResume() {
        super.onResume();

        updateCurrentStatus("online");

    }

}