package com.elmohandes.serviceschat.Screens;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.elmohandes.serviceschat.R;
import com.elmohandes.serviceschat.databinding.ActivityRegisterBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    ActivityRegisterBinding binding;
    FirebaseAuth auth;
    ProgressDialog dialog;
    DatabaseReference reference;
    String id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().hide();

        auth = FirebaseAuth.getInstance();

        dialog = new ProgressDialog(this);
        dialog.setTitle("loading...");
        dialog.setCancelable(false);

        reference = FirebaseDatabase.getInstance().getReference();

        binding.registerLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendUserToLoginActivity();
            }
        });

        binding.redisterRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createNewAccount();
            }
        });

    }

    private void createNewAccount() {

        String email = binding.registrEmail.getText().toString();
        String password = binding.registerPassword.getText().toString();
        String confirm = binding.registerConfirm.getText().toString();
        String fname = binding.registerFname.getText().toString();

        if (TextUtils.isEmpty(email)){
            binding.registrEmail.setError("Email required");
            binding.registrEmail.requestFocus();
            return;
        }else if(TextUtils.isEmpty(password)){
            binding.registerPassword.setError("Password required");
            binding.registerPassword.requestFocus();
            return;
        }else if (TextUtils.isEmpty(confirm)){
            binding.registerConfirm.setError("Confirm Password required");
            binding.registerConfirm.requestFocus();
            return;
        }else if (TextUtils.isEmpty(fname)){
            binding.registerFname.setError("Full Name required");
            binding.registerFname.requestFocus();
            return;
        }else{

            dialog.show();
            auth.createUserWithEmailAndPassword(email , password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            id = auth.getCurrentUser().getUid();

                            String deviceToken = FirebaseMessaging.getInstance().getToken().toString();
                            reference.child("users").child(id).child("token").setValue(deviceToken);

                            if (task.isSuccessful()){
                                dialog.dismiss();
                                Toast.makeText(getApplicationContext(),
                                        "Registered successfully", Toast.LENGTH_SHORT).show();
                                HashMap<String , String> map = new HashMap<>();
                                map.put("Email" , email);
                                map.put("password" , password);
                                map.put("full_name" , fname);
                                map.put("userId" , id);

                                reference.child("users").child(id).setValue(map);
                                sendUserToMainActivity();



                            }else {
                                dialog.dismiss();
                                Toast.makeText(getApplicationContext(),
                                        task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }

                        }
                    });

        }

    }

    private void sendUserToLoginActivity() {

        Intent intent = new Intent(RegisterActivity.this , LoginActivity.class);
        //this flag to make user not go back when pressing back button
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

    }

    private void sendUserToMainActivity() {

        Intent intent = new Intent(RegisterActivity.this , MainActivity.class);
        //this flag to make user not go back when pressing back button
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();

    }

}