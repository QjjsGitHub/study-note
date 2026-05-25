package com.example.study.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Px;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.viewpager2.widget.ViewPager2;

import com.example.study.R;
import com.example.study.Tools;
import com.example.study.databinding.ActivityFragmentBinding;
import com.example.study.ui.adapter.MyFragmentStateAdapter;
import com.example.study.fragment.activityFragment.BlankFragment;
import com.google.android.material.navigation.NavigationBarView;

import java.lang.ref.WeakReference;
import java.util.Objects;

public class FragmentActivity extends AppCompatActivity {

    public static final String FRAGMENT_AND_ACTIVITY_LIFE = "FragmentAndActivityLife";
    private AppBarConfiguration appBarConfiguration;
    private ActivityFragmentBinding binding;

    BlankFragment blankFragment;

    private MyFragmentStateAdapter mFragmentStateAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);


        binding = ActivityFragmentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        //NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_bottom_fragment);

        String[] fragmentNameS = {"left", "middle", "right"};

        FragmentManager fragmentManager = getSupportFragmentManager();

        mFragmentStateAdapter = new MyFragmentStateAdapter(fragmentManager, getLifecycle(), fragmentNameS);

        binding.viewPager2.setAdapter(mFragmentStateAdapter);

        binding.viewPager2.setOffscreenPageLimit(1);


        //binding.viewPager2.setAdapter(new MyFragmentStateAdapter(fragmentManager, getLifecycle(), s));

        binding.viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {

            @Override
            public void onPageScrolled(int position, float positionOffset,
                                       @Px int positionOffsetPixels) {
                Log.d("OnPageChangeCallback", "onPageScrolled " +
                        "position: " + position + " positionOffset: " + positionOffset
                        + " positionOffsetPixels: " + positionOffsetPixels);
            }

            @Override
            public void onPageSelected(int position) {
                Log.d("OnPageChangeCallback",
                        "onPageSelected " + "position: " + position);
                if (binding.bottomNavigationView.getMenu().size() - 1 >= position) {
                    binding.bottomNavigationView.getMenu().getItem(position).setChecked(true);
                }
            }


            @Override
            public void onPageScrollStateChanged(@ViewPager2.ScrollState int state) {
                Log.d("OnPageChangeCallback",
                        "onPageScrollStateChanged: " + "state: " + state);
            }

        });

        binding.bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Menu menu = binding.bottomNavigationView.getMenu();

                for (int i = 0; i < menu.size(); i++) {
                    if (menu.getItem(i) == item) {
                        binding.viewPager2.setCurrentItem(i);
                        return true;
                    }
                }
                return true;
            }
        });


        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String fragmentName = mFragmentStateAdapter.getItemCount() + 1 + "Fragment";
                ((MyFragmentStateAdapter) Objects.requireNonNull(binding.viewPager2.getAdapter()))
                        .addFragment(fragmentName);

                //MenuInflater inflater = getMenuInflater();
                Menu menu = binding.bottomNavigationView.getMenu();
                if (menu.size() < 5) {
                    menu.add(0, menu.size(), 0, fragmentName).setIcon(R.drawable.ic_notifications_black_24dp);
                } else if (menu.size() == 5) {
                    menu.add(0, 5, 0, "other").setIcon(R.drawable.icon);
                }
                int i = menu.size();
                menu.getItem((i < 6 ? i - 1 : 5)).setChecked(true);
                //inflater.inflate(R.menu.activity_bottom_nav_menu, menu); // 重新加载现有菜单项，如果你想添加新项则需要手动添加MenuItem到menu中。
                binding.viewPager2.setCurrentItem(mFragmentStateAdapter.getItemCount() - 1);
            }
        });

        binding.fab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                //MenuInflater inflater = getMenuInflater();
                Menu menu = binding.bottomNavigationView.getMenu();
                if (mFragmentStateAdapter.blankFragmentNames().length <= 6 && menu.size() > 3) {
                    menu.removeItem(menu.size() - 1);
                }

                mFragmentStateAdapter.removeFragment();
                binding.viewPager2.setCurrentItem(mFragmentStateAdapter.getItemCount() - 1);

            }
        });


        /*binding.viewPager2.setOnTouchListener(new ViewPager2.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });*/
        binding.bottomNavigationView.getMenu().getItem(0).setChecked(true);



        Log.d(FRAGMENT_AND_ACTIVITY_LIFE, getClass().getName().substring(getClass().getName().lastIndexOf(".") + 1) + "onCreate");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Tools.getInstance().getTaskInfo(new WeakReference<>(this));
        Log.d(FRAGMENT_AND_ACTIVITY_LIFE, "Thread: " + String.valueOf(Thread.currentThread().getId()));
        Log.d(FRAGMENT_AND_ACTIVITY_LIFE, getClass().getName().substring(getClass().getName().lastIndexOf(".") + 1) + "onStart");
    }

    @Override
    protected void onNewIntent(@NonNull Intent intent) {
        super.onNewIntent(intent);
        Log.d(FRAGMENT_AND_ACTIVITY_LIFE, getClass().getName().substring(getClass().getName().lastIndexOf(".") + 1) + "onNewIntent");
    }

    @Override
    protected void onRestart() {
        Log.d(FRAGMENT_AND_ACTIVITY_LIFE, getClass().getName().substring(getClass().getName().lastIndexOf(".") + 1) + "onRestart");
        super.onRestart();
    }

    @Override
    protected void onResume() {
        Log.d(FRAGMENT_AND_ACTIVITY_LIFE, getClass().getName().substring(getClass().getName().lastIndexOf(".") + 1) + "onResume");
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.d(FRAGMENT_AND_ACTIVITY_LIFE, getClass().getName().substring(getClass().getName().lastIndexOf(".") + 1) + "onPause");
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.d(FRAGMENT_AND_ACTIVITY_LIFE, getClass().getName().substring(getClass().getName().lastIndexOf(".") + 1) + "onStop");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mFragmentStateAdapter = null;

        Log.d(FRAGMENT_AND_ACTIVITY_LIFE, getClass().getName().substring(getClass().getName().lastIndexOf(".") + 1) + "onDestroy");
    }

}