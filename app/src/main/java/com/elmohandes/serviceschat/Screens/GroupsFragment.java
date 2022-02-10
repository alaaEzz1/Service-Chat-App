package com.elmohandes.serviceschat.Screens;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.elmohandes.serviceschat.R;
import com.elmohandes.serviceschat.databinding.FragmentGroupsBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class GroupsFragment extends Fragment {

    private FragmentGroupsBinding binding;
    private ArrayList<String> groupsList;
    private ArrayAdapter<String> adapter;
    private DatabaseReference reference;

    public GroupsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_groups, container, false);;

        binding = FragmentGroupsBinding.bind(v);
        reference = FirebaseDatabase.getInstance().getReference().child("groups");

        groupsList = new ArrayList<>();
        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_expandable_list_item_1
                ,groupsList);
        binding.groupsList.setSoundEffectsEnabled(true);
        binding.groupsList.setDividerHeight(4);
        binding.groupsList.setFooterDividersEnabled(true);
        binding.groupsList.setAdapter(adapter);

        retrieveAndDisplayGroups();

        binding.groupsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                String groupName = adapterView.getItemAtPosition(position).toString();

                Intent intent = new Intent(getContext(),GroupChatActivity.class);
                intent.putExtra("groupName" , groupName);
                startActivity(intent);

            }
        });


        return v;
    }

    private void retrieveAndDisplayGroups() {

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                groupsList.clear();
                Set<String> set = new HashSet<>();
                if (snapshot.exists()){

                    Iterator iterator = snapshot.getChildren().iterator();

                    while (iterator.hasNext()){
                        set.add(((DataSnapshot)iterator.next()).getKey());
                    }
                    groupsList.addAll(set);
                    adapter.notifyDataSetChanged();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}