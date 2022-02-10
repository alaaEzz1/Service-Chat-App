package com.elmohandes.serviceschat.Screens;


import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.elmohandes.serviceschat.Adapters.MessageAdapter;
import com.elmohandes.serviceschat.Models.Messages;
import com.elmohandes.serviceschat.R;
import com.elmohandes.serviceschat.Tools.NotificationVolley;
import com.elmohandes.serviceschat.databinding.ActivityChatBinding;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    ActivityChatBinding binding;
    FirebaseAuth auth;
    String receiverUserId ,senderUserId , receiverName, receiverImg;
    Toolbar customBar;
    private DatabaseReference rootRef;
    List<Messages> messagesList;
    MessageAdapter adapter;
    SimpleDateFormat currentTime , currentDate;
    String saveCurrentTime , saveCurrentDate;
    Calendar calendar;
    private String checker = "" , downloadUrl = "";
    private Uri fileUri;
    private StorageTask uploadTask;
    private ProgressDialog imageDialog;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        customBar = findViewById(R.id.chat_custom_bar);

        auth = FirebaseAuth.getInstance();
        senderUserId = auth.getCurrentUser().getUid();
        receiverUserId = getIntent().getStringExtra("conversation_id");
        receiverName = getIntent().getStringExtra("conversation_name");
        receiverImg = getIntent().getStringExtra("conversation_img");
        rootRef = FirebaseDatabase.getInstance().getReference();
        calendar = Calendar.getInstance();
        currentDate = new SimpleDateFormat("yyyy:MM:dd");
        saveCurrentDate = currentDate.format(calendar.getTime());
        currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());
        imageDialog = new ProgressDialog(this);

        messagesList = new ArrayList<>();
        adapter = new MessageAdapter(messagesList);
        binding.chatRecyclerMessages.setLayoutManager(new LinearLayoutManager(this));
        binding.chatRecyclerMessages.setAdapter(adapter);

        //actionbar operations
        setSupportActionBar(customBar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = inflater.inflate(R.layout.custom_chat_bar,null);
        actionBar.setCustomView(actionBarView);

        lastSeen();

        binding.chatCustomBar.customChatBarName.setText(receiverName);
        Picasso.get().load(receiverImg).placeholder(R.drawable.person_or_avatar)
                .into(binding.chatCustomBar.customChatBarImg);

        binding.chatCustomBar.customChatBarBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChatActivity.this , MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        binding.chatImgSend.setOnClickListener(view -> sendMessage());

        binding.chatEtMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.chatRecyclerMessages.smoothScrollToPosition
                        (binding.chatRecyclerMessages.getAdapter().getItemCount());
            }
        });

        binding.chatImgAttachment.setOnClickListener(view -> {
            CharSequence options[] = new CharSequence[]{
                    getString(R.string.images ), getString(R.string.pdf_files),
                    getString(R.string.ms_word_files)
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
            builder.setTitle(getString(R.string.select_file));
            builder.setItems(options, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                    if (i == 0){
                        checker="image";

                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        intent.setType("image/*");
                        Launcher.launch(intent);
                    }
                    if (i == 1){
                        checker="pdf";

                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        intent.setType("application/pdf");
                        Launcher.launch(intent);
                    }
                    if (i == 2){
                        checker="docx";

                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        intent.setType("application/msword");
                        Launcher.launch(intent);
                    }

                }
            });
            builder.show();
        });

    }

    ActivityResultLauncher<Intent> Launcher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
            result -> {
                Intent data = result.getData();
                Uri fileUri = data.getData();
                StorageReference reference = FirebaseStorage.getInstance().getReference();
                if (data != null && data.getData() != null){

                    imageDialog.setTitle("upload file to server...");
                    imageDialog.setMessage("uploading...");
                    imageDialog.setCancelable(false);
                    imageDialog.show();

                    sendImageToFirebase(fileUri);

                }

            }
    );

    private void sendImageToFirebase(Uri fileUri) {

        if (checker.equals("image")){

            StorageReference storageReference = FirebaseStorage.getInstance()
                    .getReference().child("image files");
            String messageSenderRef = "messages/" + senderUserId + "/" + receiverUserId;
            String messageReceiverRef = "messages/" + receiverUserId + "/" + senderUserId;

            DatabaseReference userMessageKeyRef = rootRef.child("messages").
                    child(senderUserId).child(receiverUserId).push();
            String messagePushId = userMessageKeyRef.getKey();

            StorageReference filePath = storageReference.
                    child(messagePushId+"."+"jpg");
            uploadTask = filePath.putFile(fileUri);
            uploadTask.continueWithTask(task -> {

                if (!task.isSuccessful()){

                    throw task.getException();

                }

                return filePath.getDownloadUrl();
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()){
                        Uri downloadUri = task.getResult();
                        downloadUrl = downloadUri.toString();

                        //first map for the value of message in database
                        Map messageTextBody = new HashMap();
                        messageTextBody.put("message" , downloadUrl);
                        messageTextBody.put("name" , fileUri.getLastPathSegment());
                        messageTextBody.put("type" , checker);
                        messageTextBody.put("to" , receiverUserId);
                        messageTextBody.put("from" , senderUserId);
                        messageTextBody.put("messageId" , messagePushId);
                        messageTextBody.put("time" , saveCurrentTime);
                        messageTextBody.put("date" , saveCurrentDate);

                        //the second map for the path of the message
                        Map messageBodyDetails = new HashMap();
                        messageBodyDetails.put(messageSenderRef + "/" + messagePushId , messageTextBody);
                        messageBodyDetails.put(messageReceiverRef + "/" + messagePushId , messageTextBody);

                        String senderMessage = binding.chatEtMessage.getText().toString();
                        rootRef.updateChildren(messageBodyDetails).addOnCompleteListener(task1 -> {

                            if (task1.isSuccessful()){

                                imageDialog.dismiss();
                                Toast.makeText(getApplicationContext(),
                                        "message send successfully",
                                        Toast.LENGTH_SHORT).show();
                                binding.chatRecyclerMessages.smoothScrollToPosition
                                        (binding.chatRecyclerMessages.getAdapter().getItemCount());
                                rootRef.child("users").child(senderUserId).child("full_name")
                                        .addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                if (snapshot.exists()){
                                                    String name = snapshot.getValue().toString();
                                                    NotificationVolley volley = new NotificationVolley
                                                            (ChatActivity.this);
                                                    volley.sendNotification(name , senderMessage,receiverUserId);
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });

                            }else {
                                imageDialog.dismiss();
                                Toast.makeText(getApplicationContext(),
                                        "error : ", Toast.LENGTH_SHORT).show();
                            }
                            binding.chatEtMessage.setText("");

                        });

                    }
                }
            });


        }

        else if (!checker.equals("image")){
            StorageReference storageReference = FirebaseStorage.getInstance()
                    .getReference().child("document files");
            String messageSenderRef = "messages/" + senderUserId + "/" + receiverUserId;
            String messageReceiverRef = "messages/" + receiverUserId + "/" + senderUserId;

            DatabaseReference userMessageKeyRef = rootRef.child("messages").
                    child(senderUserId).child(receiverUserId).push();
            String messagePushId = userMessageKeyRef.getKey();

            StorageReference filePath = storageReference.
                    child(messagePushId+"."+checker);
            filePath.putFile(fileUri).addOnCompleteListener(task -> {
                if (task.isSuccessful()){

                    filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            downloadUrl = uri.toString();

                            //first map for the value of message in database
                            Map messageTextBody = new HashMap();
                            messageTextBody.put("message" , downloadUrl);
                            messageTextBody.put("name" , fileUri.getLastPathSegment());
                            messageTextBody.put("type" , checker);
                            messageTextBody.put("to" , receiverUserId);
                            messageTextBody.put("from" , senderUserId);
                            messageTextBody.put("messageId" , messagePushId);
                            messageTextBody.put("time" , saveCurrentTime);
                            messageTextBody.put("date" , saveCurrentDate);

                            //the second map for the path of the message
                            Map messageBodyDetails = new HashMap();
                            messageBodyDetails.put(messageSenderRef + "/" + messagePushId , messageTextBody);
                            messageBodyDetails.put(messageReceiverRef + "/" + messagePushId , messageTextBody);

                            String senderMessage = binding.chatEtMessage.getText().toString();

                            rootRef.updateChildren(messageBodyDetails);
                            imageDialog.dismiss();
                        }
                    });

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    imageDialog.dismiss();
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {

                    //calculate the bytes sent to firebase
                    double p = (100.0*snapshot.getBytesTransferred())
                            /snapshot.getTotalByteCount();
                    imageDialog.setMessage((int)p + " % uploading...");

                    imageDialog.show();

                    Toast.makeText(getApplicationContext(),
                            "message send successfully",
                            Toast.LENGTH_SHORT).show();

                    //auto scroll and notification
                    String senderMessage = binding.chatEtMessage.getText().toString();
                    binding.chatRecyclerMessages.smoothScrollToPosition
                            (binding.chatRecyclerMessages.getAdapter().getItemCount());
                    rootRef.child("users").child(senderUserId).child("full_name")
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()){
                                        String name = snapshot.getValue().toString();
                                        NotificationVolley volley = new NotificationVolley
                                                (ChatActivity.this);
                                        volley.sendNotification(name , senderMessage,receiverUserId);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                }
            });
        }

    }

    private void sendMessage() {

        String senderMessage = binding.chatEtMessage.getText().toString();
        if (senderMessage.isEmpty()){
            Toast.makeText(getApplicationContext(),
                    getString(R.string.write_your_message_first), Toast.LENGTH_SHORT).show();
        }else {

            String messageSenderRef = "messages/" + senderUserId + "/" + receiverUserId;
            String messageReceiverRef = "messages/" + receiverUserId + "/" + senderUserId;

            DatabaseReference userMessageKeyRef = rootRef.child("messages").
                    child(senderUserId).child(receiverUserId).push();
            String messagePushId = userMessageKeyRef.getKey();

            //first map for the value of message in database
            Map messageTextBody = new HashMap();
            messageTextBody.put("message" , senderMessage);
            messageTextBody.put("type" , "text");
            messageTextBody.put("to" , receiverUserId);
            messageTextBody.put("from" , senderUserId);
            messageTextBody.put("messageId" , messagePushId);
            messageTextBody.put("time" , saveCurrentTime);
            messageTextBody.put("date" , saveCurrentDate);

            //the second map for the path of the message
            Map messageBodyDetails = new HashMap();
            messageBodyDetails.put(messageSenderRef + "/" + messagePushId , messageTextBody);
            messageBodyDetails.put(messageReceiverRef + "/" + messagePushId , messageTextBody);

            rootRef.updateChildren(messageBodyDetails).addOnCompleteListener(task -> {

                if (task.isSuccessful()){

                    binding.chatRecyclerMessages.smoothScrollToPosition
                            (binding.chatRecyclerMessages.getAdapter().getItemCount());
                    rootRef.child("users").child(senderUserId).child("full_name")
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()){
                                        String name = snapshot.getValue().toString();
                                        NotificationVolley volley = new NotificationVolley
                                                (ChatActivity.this);
                                        volley.sendNotification(name , senderMessage,receiverUserId);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                }else {
                    Toast.makeText(getApplicationContext(),
                            "error : ", Toast.LENGTH_SHORT).show();
                }
                binding.chatEtMessage.setText("");

            });

        }

    }

    private void lastSeen(){
        rootRef.child("users").child(receiverUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                //check online ,last seen and time state of user
                if (snapshot.child("userState").hasChild("state")){

                    String state = snapshot.child("userState").child("state")
                            .getValue().toString();
                    String date = snapshot.child("userState").child("date")
                            .getValue().toString();
                    String time = snapshot.child("userState").child("time")
                            .getValue().toString();

                    if (state.equals("online")){
                        binding.chatCustomBar.customChatBarState.setText("online");
                        binding.chatCustomBar.customChatBarState.setVisibility(View.VISIBLE);
                    }else if (state.equals("offline")){
                        binding.chatCustomBar.customChatBarState.
                                setText("last seen :" + date +" " + time);
                    }

                }else {

                    binding.chatCustomBar.customChatBarState.setText("Not updated");

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
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

        FirebaseDatabase.getInstance().getReference().child("users").child(senderUserId)
                .child("userState").updateChildren(onlineState);

    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onStart() {
        super.onStart();

        lastSeen();

        messagesList.clear();
        rootRef.child("messages").child(senderUserId).child(receiverUserId)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                        Messages messages = snapshot.getValue(Messages.class);
                        messagesList.add(messages);
                        adapter.notifyDataSetChanged();
                        binding.chatRecyclerMessages.smoothScrollToPosition
                                (binding.chatRecyclerMessages.getAdapter().getItemCount());

                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                        adapter.notifyDataSetChanged();
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