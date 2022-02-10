package com.elmohandes.serviceschat.Adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.elmohandes.serviceschat.Screens.ContactsScreen;
import com.elmohandes.serviceschat.Screens.ChatsFragment;
import com.elmohandes.serviceschat.Screens.GroupsFragment;
import com.elmohandes.serviceschat.Screens.RequestsFragment;

public class ViewPagerAdapter extends FragmentStateAdapter {
    public ViewPagerAdapter(@NonNull FragmentManager fragmentManager,
                            @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    @Override
    public Fragment createFragment(int position) {

        switch (position){

            case 1:
                return new GroupsFragment();
            case 2:
                return new ContactsScreen();
            case 3:
                return new RequestsFragment();
            default:
                return new ChatsFragment();

        }
    }

    @Override
    public int getItemCount() {
        return 4;
    }
}
