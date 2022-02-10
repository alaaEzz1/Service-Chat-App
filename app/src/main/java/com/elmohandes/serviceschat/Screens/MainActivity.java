package com.elmohandes.serviceschat.Screens;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.widget.ViewPager2;

import android.content.DialogInterface;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;


import com.elmohandes.serviceschat.R;
import com.elmohandes.serviceschat.Adapters.ViewPagerAdapter;
import com.elmohandes.serviceschat.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    FragmentManager fm;
    ViewPagerAdapter pagerAdapter;
    FirebaseAuth auth;

    String currentUserId ;
    DatabaseReference reference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        binding=ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser().getUid();
        reference = FirebaseDatabase.getInstance().getReference().child("users");

        fm = getSupportFragmentManager();
        pagerAdapter = new ViewPagerAdapter(fm , getLifecycle());
        binding.mainPager2.setAdapter(pagerAdapter);
        //when you select an item from the tab
        binding.mainTab.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                binding.mainPager2.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        //to make swipe and select tab item in the same time
        binding.mainPager2.registerOnPageChangeCallback
                (new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                binding.mainTab.selectTab(binding.mainTab.getTabAt(position));
            }
        });

    }

    private void sendUserToLoginActivity() {

        Intent intent = new Intent(MainActivity.this , LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

    }

    private void sendUserToSettingsActivity() {

        Intent intent = new Intent(MainActivity.this , SettingsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

    }

    private void requestNewGroup() {

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Enter Group Name...");
        //put view in AlertDialog
        final EditText groupName = new EditText(MainActivity.this);
        groupName.setHint("e.g Chat Cafe");
        builder.setView(groupName);
        //AlertDialog listeners
        builder.setPositiveButton("create", (dialogInterface, i) -> {
            String nameOfGroup = groupName.getText().toString();


            if (TextUtils.isEmpty(nameOfGroup)) {

                groupName.setError("group name required");
                groupName.requestFocus();
                return;
            }else {
                createNewGroup(nameOfGroup);
            }
        });

        builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        builder.show();

    }

    private void createNewGroup(String nameOfGroup) {

        FirebaseDatabase.getInstance().getReference().child("groups").child(nameOfGroup)
                .setValue("").addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        Toast.makeText(getApplicationContext(),
                                nameOfGroup+" group is created successfully",
                                Toast.LENGTH_SHORT).show();
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

        FirebaseDatabase.getInstance().getReference().child("users").child(currentUserId)
                .child("userState").updateChildren(onlineState);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_options,menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.menu_logout:
                auth.signOut();
                sendUserToLoginActivity();
                finish();
                break;

            case R.id.menu_settings:
                startActivity(new Intent(MainActivity.this,SettingsActivity.class));
                break;

            case R.id.menu_create_group:

                createValidationToControlGroups();
                break;

            case  R.id.menu_find_friends:
                startActivity(new Intent(MainActivity.this , FindFriendsActivity.class));
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void createValidationToControlGroups() {

        reference.child(currentUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            String name = snapshot.child("full_name").getValue().toString();
                            if (name.equals("Alaa omda")){
                                requestNewGroup();
                            }else {
                                Toast.makeText(getApplicationContext(),
                                        "admin only can create groups",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null){
            sendUserToLoginActivity();
        }

        FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().
                getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child("bio").exists()){
                    String name = snapshot.child("full_name").getValue().toString();
                    Log.d("name" , name);
                }else{
                    sendUserToSettingsActivity();
                }
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