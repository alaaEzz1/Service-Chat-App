package com.elmohandes.serviceschat.Screens;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ScrollView;
import android.widget.Toast;

import com.elmohandes.serviceschat.R;
import com.elmohandes.serviceschat.databinding.ActivityGroupChatBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

public class GroupChatActivity extends AppCompatActivity {

    private ActivityGroupChatBinding binding;
    private String groupName;
    private String currentUserId , currentUserName;
    private FirebaseAuth auth;
    private DatabaseReference reference;
    private String currentDate , currentTime;
    private  DatabaseReference groupReference , groupMessageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        binding = ActivityGroupChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        groupName = getIntent().getExtras().get("groupName").toString();
        getSupportActionBar().setTitle(groupName);

        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser().getUid();
        reference = FirebaseDatabase.getInstance().getReference().child("users");
        groupReference = FirebaseDatabase.getInstance().getReference().child("groups").child(groupName);

        getUserInfo();

        binding.groupsMessageText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.groupChatScroll.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });

        binding.groupImgSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                saveMessageInfoToDatabase();

                binding.groupEtSend.setText("");

                //auto scroll down in new messages
                binding.groupChatScroll.fullScroll(ScrollView.FOCUS_DOWN);

            }
        });

    }

    private void getUserInfo() {

        reference.child(currentUserId).child("full_name").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()){
                    currentUserName = snapshot.getValue().toString();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void saveMessageInfoToDatabase() {

        String message = binding.groupEtSend.getText().toString();
        String messageKey = groupReference.push().getKey();

        if (TextUtils.isEmpty(message)){
            binding.groupEtSend.setError("Enter the Message First");
            binding.groupEtSend.requestFocus();
            return;
        }else {
            Calendar dateCalendar = Calendar.getInstance();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy:MM:dd");
            currentDate = dateFormat.format(dateCalendar.getTime());

            Calendar timeCalendar = Calendar.getInstance();
            SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");
            currentTime = timeFormat.format(timeCalendar.getTime());

            HashMap<String ,Object> groupMessageKey = new HashMap<>();
            groupReference.updateChildren(groupMessageKey);

            groupMessageRef = groupReference.child(messageKey);
            HashMap<String ,Object> messageInfoMap = new HashMap<>();
            messageInfoMap.put("name" , currentUserName);
            messageInfoMap.put("message" , message);
            messageInfoMap.put("date" , currentDate);
            messageInfoMap.put("time" , currentTime);
            groupMessageRef.updateChildren(messageInfoMap);

        }

    }

    private void displayMessages(DataSnapshot snapshot) {

        Iterator iterator = snapshot.getChildren().iterator();

        while (iterator.hasNext()){

            //in iterator order variables as its ordered in database ex.date then message then name then time
            String chatDate = (String) ((DataSnapshot)iterator.next()).getValue();
            String chatMessage = (String) ((DataSnapshot)iterator.next()).getValue();
            String chatName = (String) ((DataSnapshot)iterator.next()).getValue();
            String chatTime = (String) ((DataSnapshot)iterator.next()).getValue();

            binding.groupsMessageText.append(chatName + " :\n" + chatMessage + "\n"
                    + chatTime+ "       "+chatDate + "\n\n");
            //auto scroll down in new messages
            binding.groupChatScroll.fullScroll(ScrollView.FOCUS_DOWN);

        }

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void updateCurrentStatus(String state){

        String saveCurrentTime , saveCurrentDate;
        Calendar calendar = Calendar.getInstance();
        android.icu.text.SimpleDateFormat currentDate = new android.icu.text.SimpleDateFormat("yyyy:MM:dd");
        saveCurrentDate = currentDate.format(calendar.getTime());
        android.icu.text.SimpleDateFormat currentTime = new android.icu.text.SimpleDateFormat("hh:mm a");
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

        groupReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                if (snapshot.exists()){

                    displayMessages(snapshot);

                }

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                if (snapshot.exists()){

                    displayMessages(snapshot);

                }

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


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