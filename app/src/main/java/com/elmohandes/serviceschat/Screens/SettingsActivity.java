package com.elmohandes.serviceschat.Screens;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.elmohandes.serviceschat.R;
import com.elmohandes.serviceschat.databinding.ActivitySettingsBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.HashMap;


public class SettingsActivity extends AppCompatActivity {
    ActivitySettingsBinding binding;
    ProgressDialog dialog;
    String id , oldName ,bio;
    StorageReference uploadProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().hide();

        uploadProfile = FirebaseStorage.getInstance().getReference().child("profile images");

        dialog = new ProgressDialog(this);
        dialog.setTitle(getString(R.string.update_information));
        dialog.setMessage("updating...");
        dialog.setCancelable(false);

        id = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseDatabase.getInstance().getReference().child("users").child(id)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            oldName = snapshot.child("full_name").getValue().toString();
                            binding.setName.setText(oldName);
                            if (snapshot.child("bio").exists()){
                                bio = snapshot.child("bio").getValue().toString();
                                binding.setBio.setText(bio);
                            }

                            if (snapshot.child("image").exists()){
                                String url = snapshot.child("image").getValue().toString();
                                Picasso.get().load(url).into(binding.setImage);
                            }

                        }else {
                            Toast.makeText(getApplicationContext(),
                                    "data not exist", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                        Toast.makeText(getApplicationContext(),
                                error.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });

        binding.setUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setUpdateSettings();
            }
        });

        binding.setImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                launcher.launch(intent);

            }
        });

    }

    ActivityResultLauncher<Intent> launcher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    Intent data = result.getData();
                    if (data != null){
                        if (data.getData() != null) {

                            Uri uri = data.getData();
                            //binding.setImage.setImageURI(uri);

                           imageToFirebase(uri);
                        }


                    }

                }
            }
    );

    private void imageToFirebase(Uri uri) {

        dialog.show();
        uploadProfile.child(id).putFile(uri)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        uploadProfile.child(id).getDownloadUrl().
                                addOnSuccessListener(uri1 -> {
                                    String url = uri1.toString();
                                    saveToDatabase(url);

                                });

                    }else {
                        Toast.makeText(getApplicationContext(),
                                task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void saveToDatabase(String url) {

        FirebaseDatabase.getInstance().getReference().child("users").
                child(id).child("image").setValue(url)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            dialog.dismiss();
                            Toast.makeText(getApplicationContext(),
                                    "your image is updated successfully",
                                    Toast.LENGTH_SHORT).show();
                        }else {
                            dialog.dismiss();
                        }
                    }
                });

    }

    private void setUpdateSettings() {

        String name = binding.setName.getText().toString();
        String bio = binding.setBio.getText().toString();


        if (TextUtils.isEmpty(name)){
            binding.setName.setError("Name required");
            binding.setName.requestFocus();
            return;
        }else if(TextUtils.isEmpty(bio)){
            binding.setBio.setError("bio or short cv required");
            binding.setBio.requestFocus();
            return;
        }else {
            dialog.show();
            FirebaseDatabase.getInstance().getReference().child("users").child(id).
                    child("full_name").setValue(name).addOnCompleteListener(task ->

                    FirebaseDatabase.getInstance().getReference().child("users").child(id).
                            child("bio").setValue(bio).addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()){
                                    Toast.makeText(getApplicationContext(),
                                            "Profile Updated", Toast.LENGTH_SHORT).show();
                                    sendUserToMainActivity();
                                }

                                dialog.dismiss();

                            }));


        }

    }

    private void sendUserToMainActivity() {

        Intent intent = new Intent(SettingsActivity.this , MainActivity.class);
        startActivity(intent);
        finish();

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

        FirebaseDatabase.getInstance().getReference().child("users").child(id)
                .child("userState").updateChildren(onlineState);

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