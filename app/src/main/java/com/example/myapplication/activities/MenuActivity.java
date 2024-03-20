package com.example.myapplication.activities;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;

import com.example.myapplication.GlobalState;
import com.example.myapplication.R;
import com.example.myapplication.databinding.ActivityMenuBinding;
import com.example.myapplication.fragments.AccountFragment;
import com.example.myapplication.fragments.EventCreationFragment;
import com.example.myapplication.fragments.EventViewFragment;
import com.example.myapplication.fragments.EventsFragment;
import com.example.myapplication.fragments.LoginFragmentI;
import com.example.myapplication.fragments.ManageEventsFragment;
import com.example.myapplication.fragments.SearchFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MenuActivity extends AppCompatActivity{
    private ActivityMenuBinding binding;
    @Override
    protected void onCreate(Bundle SavedInstanceState){
        super.onCreate(SavedInstanceState);

        GlobalState.init(getApplicationContext());

        setContentView(R.layout.activity_menu);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        binding = ActivityMenuBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        changeFragment(new EventsFragment());
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if(id == R.id.home)
            {
                //pass here Fragments of home
                changeFragment(new EventsFragment());
            } else if(id == R.id.account){
                if(!GlobalState.isLoggedIn()) changeFragment(new LoginFragmentI());
                else changeFragment(new AccountFragment());
                return true;
            } else if(id == R.id.create){
                if(!GlobalState.isLoggedIn()) changeFragment(new LoginFragmentI());
                else changeFragment(new ManageEventsFragment());
                return true;
            } else if(id == R.id.app_bar_search){
                if(GlobalState.isLoggedIn()) changeFragment(new SearchFragment());
                return true;
            }
            return true;
        });

    }

    private void changeFragment(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.LayouttoChange, fragment);
        fragmentTransaction.commit();
    }


    @Override
    public void onBackPressed() {
        FragmentManager fragmentManager = getSupportFragmentManager();

        if (fragmentManager.getBackStackEntryCount() > 0) {
                getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }
}
