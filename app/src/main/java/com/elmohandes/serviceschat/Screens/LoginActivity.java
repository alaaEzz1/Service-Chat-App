package com.elmohandes.serviceschat.Screens;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.elmohandes.serviceschat.R;
import com.elmohandes.serviceschat.databinding.ActivityLoginBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

public class LoginActivity extends AppCompatActivity {

    FirebaseUser currentUser;
    ActivityLoginBinding binding;
    FirebaseAuth auth;
    ProgressDialog dialog;
    DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().hide();

        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        usersRef = FirebaseDatabase.getInstance().getReference().child("users");

        dialog = new ProgressDialog(this);
        dialog.setTitle("singin in...");
        dialog.setMessage("loading...");
        dialog.setCancelable(false);

        binding.loginRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendUserToRegisterActivity();
            }
        });

        binding.loginLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                allowUsersToLogin();
            }
        });

        binding.loginPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),PhoneActivity.class);
                startActivity(intent);
            }
        });

    }

    private void allowUsersToLogin() {
        String email = binding.loginEmail.getText().toString();
        String password = binding.loginPassword.getText().toString();

        if (TextUtils.isEmpty(email)){
            binding.loginEmail.setError("Email required");
            binding.loginEmail.requestFocus();
            return;
        }else if(TextUtils.isEmpty(password)){
            binding.loginPassword.setError("Password required");
            binding.loginPassword.requestFocus();
            return;
        }else{

            dialog.show();
            auth.signInWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){

                                String currentUserId = auth.getCurrentUser().getUid();
                                String deviceToken = FirebaseMessaging.getInstance().getToken().toString();

                                usersRef.child(currentUserId).child("token").setValue(deviceToken)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()){
                                                    sendUserToMainActivity();
                                                    Toast.makeText(getApplicationContext(),
                                                            "login successfully",
                                                            Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            }else {
                                Toast.makeText(getApplicationContext(),
                                        "email or password s not correct", Toast.LENGTH_SHORT).show();
                            }
                            dialog.dismiss();
                        }
                    });

        }

    }

    private void sendUserToMainActivity() {

        Intent intent = new Intent(LoginActivity.this , MainActivity.class);
        //this flag to make user not go back when pressing back button
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();

    }

    private void sendUserToRegisterActivity() {

        Intent intent = new Intent(LoginActivity.this , RegisterActivity.class);
        startActivity(intent);

    }

    @Override
    protected void onStart() {
        super.onStart();

        if (currentUser != null){
            sendUserToMainActivity();
        }

    }

}