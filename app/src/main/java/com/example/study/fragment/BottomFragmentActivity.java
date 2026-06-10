package com.example.study.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.study.R;
import com.example.study.databinding.ActivityBottomFragmentBinding;
import com.example.study.fragment.home.HomeFragment1;
import com.example.study.fragment.notifications.NotificationsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class BottomFragmentActivity extends AppCompatActivity {

    public static final String JAVA_FRAGMENT_AND_ACTIVITY_LIFE = "JavaFragmentAndActivityLife";

    public String bottomFragmentActivityId = "BottomFragmentActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(JAVA_FRAGMENT_AND_ACTIVITY_LIFE, getClass().getName().substring(
                getClass().getName().lastIndexOf(".") + 1) + "onCreate");

        ActivityBottomFragmentBinding binding = ActivityBottomFragmentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        /*AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();*/

        /*HashSet<Integer> s = new HashSet<Integer>();
        s.add(R.id.navigation_home);
        s.add(R.id.navigation_dashboard);
        s.add(R.id.navigation_notifications);*/

        int[] a = {R.id.navigation_home,
                R.id.navigation_dashboard,
                R.id.navigation_notifications};

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(a).build();

        NavController navController = Navigation.findNavController(this, R.id.nav_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        binding.floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getSupportFragmentManager();
                Fragment fragment = fragmentManager.getPrimaryNavigationFragment();
                /*fragmentManager.beginTransaction().add(R.id.nav_fragment, tempFragment, "temp")
                        .commitNow();*/
                Fragment tempFragment = new NotificationsFragment();

                fragmentManager.beginTransaction().add(R.id.nav_fragment, new HomeFragment1()).commitNow();
                fragmentManager.beginTransaction().replace(R.id.nav_fragment, tempFragment).commitNow();


                new Handler(Looper.getMainLooper()).
                        postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                FragmentManager fragmentManager = getSupportFragmentManager();
                                fragmentManager.beginTransaction().remove(tempFragment).commitNow();
                            }
                        }, 3000);
            }
        });

    }


    @Override
    public void onPostCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle
            persistentState) {
        Log.d(JAVA_FRAGMENT_AND_ACTIVITY_LIFE, getClass().getName().substring(
                getClass().getName().lastIndexOf(".") + 1) + "onPostCreate persistentState");
        super.onPostCreate(savedInstanceState, persistentState);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        Log.d(JAVA_FRAGMENT_AND_ACTIVITY_LIFE, getClass().getName().substring(
                getClass().getName().lastIndexOf(".") + 1) + "onPostCreate");
        super.onPostCreate(savedInstanceState);
    }

    @Override
    protected void onPostResume() {
        Log.d(JAVA_FRAGMENT_AND_ACTIVITY_LIFE, getClass().getName().substring(
                getClass().getName().lastIndexOf(".") + 1) + "onPostResume");
        super.onPostResume();
    }

    @Override
    protected void onNewIntent(@NonNull Intent intent) {
        Log.d(JAVA_FRAGMENT_AND_ACTIVITY_LIFE, getClass().getName().substring(
                getClass().getName().lastIndexOf(".") + 1) + "onNewIntent");
        super.onNewIntent(intent);
    }

    @Override
    protected void onStart() {
        Log.d(JAVA_FRAGMENT_AND_ACTIVITY_LIFE, getClass().getName().substring(
                getClass().getName().lastIndexOf(".") + 1) + "onStart");
        super.onStart();
    }

    @Override
    protected void onRestart() {
        Log.d(JAVA_FRAGMENT_AND_ACTIVITY_LIFE, getClass().getName().substring(
                getClass().getName().lastIndexOf(".") + 1) + "onRestart");
        super.onRestart();
    }

    @Override
    protected void onResume() {
        Log.d(JAVA_FRAGMENT_AND_ACTIVITY_LIFE, getClass().getName().substring(
                getClass().getName().lastIndexOf(".") + 1) + "onResume");
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.d(JAVA_FRAGMENT_AND_ACTIVITY_LIFE, getClass().getName().substring(
                getClass().getName().lastIndexOf(".") + 1) + "onPause");
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.d(JAVA_FRAGMENT_AND_ACTIVITY_LIFE, getClass().getName().substring(
                getClass().getName().lastIndexOf(".") + 1) + "onStop");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d(JAVA_FRAGMENT_AND_ACTIVITY_LIFE, getClass().getName().substring(
                getClass().getName().lastIndexOf(".") + 1) + "onDestroy");
        super.onDestroy();
        Log.d(JAVA_FRAGMENT_AND_ACTIVITY_LIFE, getClass().getName().substring(
                getClass().getName().lastIndexOf(".") + 1) + "onDestroyOver");
    }
}