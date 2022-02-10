package com.elmohandes.serviceschat.Tools;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.elmohandes.serviceschat.databinding.FindFriendsItemBinding;

public class FindFriendsHolder extends RecyclerView.ViewHolder {

    public FindFriendsItemBinding binding;

    public FindFriendsHolder(@NonNull View itemView) {
        super(itemView);

        binding = FindFriendsItemBinding.bind(itemView);

    }
}
