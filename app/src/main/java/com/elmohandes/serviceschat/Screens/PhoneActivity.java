package com.elmohandes.serviceschat.Screens;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.elmohandes.serviceschat.R;
import com.elmohandes.serviceschat.databinding.ActivityPhoneBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class PhoneActivity extends AppCompatActivity {

    ActivityPhoneBinding binding;
    PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    String mVerificationId;
    PhoneAuthProvider.ForceResendingToken mResendToken;
    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone);

        binding = ActivityPhoneBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dialog = new ProgressDialog(this);
        dialog.setTitle("phone verification");
        dialog.setMessage("verification loading...");
        dialog.setCancelable(false);

        getSupportActionBar().hide();
        binding.phoneBtnGet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (TextUtils.isEmpty(binding.phoneEtNumber.getText().toString())){
                    Toast.makeText(getApplicationContext(),
                            "phone number required", Toast.LENGTH_SHORT).show();
                }else {

                    dialog.show();

                    PhoneAuthOptions options =
                            PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
                                    .setPhoneNumber(binding.phoneEtNumber.getText().toString())
                                    // Phone number to verify
                                    .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                                    .setActivity(PhoneActivity.this)
                                    // Activity (for callback binding)
                                    .setCallbacks(callbacks)
                                    // OnVerificationStateChangedCallbacks
                                    .build();
                    PhoneAuthProvider.verifyPhoneNumber(options);

                }

            }
        });

        binding.phoneEtVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                binding.phoneBtnGet.setVisibility(View.GONE);
                binding.phoneEtNumber.setVisibility(View.GONE);

                String verficationCode = binding.phoneEtVerify.getText().toString();

                if (verficationCode.isEmpty()){
                    Toast.makeText(getApplicationContext(),
                            "Enter verification code first", Toast.LENGTH_SHORT).show();
                }else {
                    dialog.show();
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential
                            (mVerificationId, verficationCode);
                    signInWithPhoneAuthCredential(credential);
                }

            }
        });

        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

                dialog.dismiss();
                signInWithPhoneAuthCredential(phoneAuthCredential);

            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {

                Toast.makeText(getApplicationContext(),
                        "invalid phone number please enter your country code",
                        Toast.LENGTH_SHORT).show();

                binding.phoneBtnGet.setVisibility(View.VISIBLE);
                binding.phoneEtNumber.setVisibility(View.VISIBLE);
                binding.phoneEtVerify.setVisibility(View.GONE);
                binding.phoneBtnVerify.setVisibility(View.GONE);
                dialog.dismiss();

            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d("TAG", "onCodeSent:" + verificationId);

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;

                dialog.dismiss();
                Toast.makeText(getApplicationContext(),
                        "the code will send soon", Toast.LENGTH_SHORT).show();

                binding.phoneBtnGet.setVisibility(View.GONE);
                binding.phoneEtNumber.setVisibility(View.GONE);
                binding.phoneEtVerify.setVisibility(View.VISIBLE);
                binding.phoneBtnVerify.setVisibility(View.VISIBLE);

            }

        };


    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            dialog.dismiss();
                            Toast.makeText(getApplicationContext(),
                                    "successfully logged in", Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                            startActivity(intent);
                            finish();

                        } else {

                            Toast.makeText(getApplicationContext(),
                                    "error : "+task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    }
                });
    }


}