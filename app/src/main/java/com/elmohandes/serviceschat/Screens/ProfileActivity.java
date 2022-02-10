package com.elmohandes.serviceschat.Screens;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.elmohandes.serviceschat.R;
import com.elmohandes.serviceschat.databinding.ActivityProfileBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity {

    private String recievedUserId;
    private ActivityProfileBinding binding;
    private DatabaseReference reference;
    private String currentState , currentUserId;
    private DatabaseReference chatRequestRef;
    private DatabaseReference contactRef;
    private DatabaseReference notificationsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().hide();

        reference = FirebaseDatabase.getInstance().getReference().child("users");
        recievedUserId = getIntent().getExtras().get("profile_visit_id").toString();

        currentState = "new";
        //sender user id
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        chatRequestRef = FirebaseDatabase.getInstance().getReference().child("chat requests");
        contactRef = FirebaseDatabase.getInstance().getReference().child("contacts");
        notificationsRef = FirebaseDatabase.getInstance().getReference().child("notifications");

        retrieveUserInformation();

    }

    private void retrieveUserInformation() {

        reference.child(recievedUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.hasChild("image")){

                    String img = snapshot.child("image").getValue().toString();
                    String name = snapshot.child("full_name").getValue().toString();
                    String bio = snapshot.child("bio").getValue().toString();

                    Picasso.get().load(img).placeholder(R.drawable.person_or_avatar)
                            .into(binding.profileImg);
                    binding.profileName.setText(name);
                    binding.profileBio.setText(bio);

                    manageChatRequest();

                }else {

                    String name = snapshot.child("full_name").getValue().toString();
                    String bio = snapshot.child("bio").getValue().toString();
                    binding.profileName.setText(name);
                    binding.profileBio.setText(bio);

                    Toast.makeText(getApplicationContext(),
                            R.string.no_profile_image, Toast.LENGTH_SHORT).show();

                    manageChatRequest();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                Toast.makeText(getApplicationContext(),
                        "there is error please login again", Toast.LENGTH_SHORT).show();

            }
        });

    }

    private void manageChatRequest() {

        contactRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.hasChild(recievedUserId)){
                    String accepted = snapshot.child(recievedUserId).child("contacts").getValue().toString();
                    if (accepted.equals("saved")){
                        binding.profileSendMessage.setText("Remove friendship");
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //show cancel chat request when you enter again to the chat
        chatRequestRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.hasChild(recievedUserId)){
                    String request_type = snapshot.child(recievedUserId).child("request_type")
                            .getValue().toString();
                    if (request_type.equals("sent")){
                        currentState ="request_sent";
                        binding.profileSendMessage.setText("cancel chat request");
                    }else if (request_type.equals("received")){
                        currentState = "request_received";
                        binding.profileSendMessage.setText("Accept Friend Request");
                        binding.profileRequestDecline.setVisibility(View.VISIBLE);
                        binding.profileRequestDecline.setEnabled(true);
                        binding.profileRequestDecline.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                cancelChatRequest();
                            }
                        });
                    }
                }else {
                    chatRequestRef.child(currentUserId).
                            addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.hasChild(recievedUserId)){
                                        currentState = "friends";
                                        binding.profileSendMessage.setText("Remove friendship");
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        if (!currentUserId.equals(recievedUserId)){

            binding.profileSendMessage.setOnClickListener(view -> {
                binding.profileSendMessage.setEnabled(false);
                //if it first chat and new
                if (currentState.equals("new")){
                    sendChatRequest();
                }

                if (currentState.equals("request_sent")){
                    cancelChatRequest();
                }

                if (currentState.equals("request_received")){
                    acceptChatRequest();

                }

                if (currentState.equals("friends")){

                    removeSpecificContact();

                }

            });

        }else {
            binding.profileSendMessage.setVisibility(View.INVISIBLE);
        }

    }

    private void removeSpecificContact() {

        contactRef.child(currentUserId).child(recievedUserId).removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){

                        contactRef.child(recievedUserId).child(currentUserId).removeValue()
                                .addOnCompleteListener(task1 -> {

                                    if (task1.isSuccessful()){

                                        currentState = "new";
                                        binding.profileSendMessage.setEnabled(true);
                                        binding.profileSendMessage.setText("Send Chat Request");
                                        Toast.makeText(getApplicationContext(),
                                                "removed Contact successfully",
                                                Toast.LENGTH_SHORT).show();
                                        binding.profileRequestDecline.setEnabled(false);
                                        binding.profileRequestDecline.setVisibility(View.INVISIBLE);

                                    }

                                });

                    }
                });

    }

    private void acceptChatRequest() {

        contactRef.child(currentUserId).child(recievedUserId).child("contacts").setValue("saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            contactRef.child(recievedUserId).child(currentUserId)
                                    .child("contacts").setValue("saved")
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()){

                                            //you have to remove records between to uses in chat request
                                            removeRecordsFromChatRequest();


                                        }

                                    });
                        }

                    }
                });

    }

    private void removeRecordsFromChatRequest() {


        chatRequestRef.child(currentUserId).child(recievedUserId).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            chatRequestRef.child(recievedUserId).child(currentUserId)
                                    .removeValue().addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()){

                                            binding.profileSendMessage.setEnabled(true);
                                            currentState = "friends";
                                            binding.profileSendMessage.setText("Remove friendship");

                                            binding.profileRequestDecline.setVisibility(View.INVISIBLE);
                                            binding.profileRequestDecline.setEnabled(false);

                                        }

                                    });
                        }

                    }
                });



    }

    private void cancelChatRequest() {

        chatRequestRef.child(currentUserId).child(recievedUserId).removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){

                        chatRequestRef.child(recievedUserId).child(currentUserId).removeValue()
                                .addOnCompleteListener(task1 -> {

                                    if (task1.isSuccessful()){

                                        currentState = "new";
                                        binding.profileSendMessage.setEnabled(true);
                                        binding.profileSendMessage.setText("Send Chat Request");
                                        Toast.makeText(getApplicationContext(),
                                                "cancelled request successfully",
                                                Toast.LENGTH_SHORT).show();
                                        binding.profileRequestDecline.setEnabled(false);
                                        binding.profileRequestDecline.setVisibility(View.INVISIBLE);

                                    }

                                });

                    }
                });

    }

    private void sendChatRequest() {

        chatRequestRef.child(currentUserId).child(recievedUserId).child("request_type")
                .setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){

                    chatRequestRef.child(recievedUserId).child(currentUserId).child("request_type")
                            .setValue("received").addOnCompleteListener(task1 -> {

                                if (task1.isSuccessful()){

                                    HashMap<String , String> chatNotificationsMap = new HashMap<>();
                                    chatNotificationsMap.put("from" , currentUserId);
                                    chatNotificationsMap.put("type" , "request");

                                    notificationsRef.child(recievedUserId).push().setValue(chatNotificationsMap)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {

                                                    if (task.isSuccessful()){

                                                        binding.profileSendMessage.setEnabled(true);
                                                        currentState = "request_sent";
                                                        binding.profileSendMessage.setText("cancel Chat request");

                                                    }

                                                }
                                            });

                                }

                            });

                }

            }
        });


    }
}