package com.example.study.fragment.home;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import com.example.study.R;
import com.example.study.databinding.FragmentHome1Binding;
import com.example.study.fragment.BottomFragmentActivity;
import com.example.study.fragment.notifications.NotificationsFragment;

public class HomeFragment1 extends Fragment {

    private FragmentHome1Binding binding;
    private HomeFragment1 homeFragment1;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        Log.d(BottomFragmentActivity.JAVA_FRAGMENT_AND_ACTIVITY_LIFE, getClass().getName().
                substring(getClass().getName().lastIndexOf(".") + 1) + "  :  " +
                "state" + "  :  " + "onCreateView" + "  Thread : "
                + Thread.currentThread().getId());

        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHome1Binding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textHome;
        homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);


        Fragment tempFragment = new NotificationsFragment();
        homeFragment1 = this;
        binding.textHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                Fragment fragment = fragmentManager.getPrimaryNavigationFragment();
                /*fragmentManager.beginTransaction().add(R.id.nav_fragment, tempFragment, "temp")
                        .commitNow();*/


                fragmentManager.beginTransaction().replace(R.id.nav_fragment, tempFragment).commitNow();


                new Handler().
                        postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                                fragmentManager.beginTransaction().remove(tempFragment).commitNow();
                            }
                        }, 3000);
            }
        });


       /* Button button=new Button(this.getActivity());
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }

            @Override
            public int hashCode() {
                return super.hashCode();
            }
        });*/

        return root;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        Log.d(BottomFragmentActivity.JAVA_FRAGMENT_AND_ACTIVITY_LIFE, getClass().getName().
                substring(getClass().getName().lastIndexOf(".") + 1) + "  :  " + "state" + "  :  " + "onAttach");
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(BottomFragmentActivity.JAVA_FRAGMENT_AND_ACTIVITY_LIFE, getClass().getName().
                substring(getClass().getName().lastIndexOf(".") + 1) + "  :  " + "state" + "  :  " + "onCreate");
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onPause() {
        Log.d(BottomFragmentActivity.JAVA_FRAGMENT_AND_ACTIVITY_LIFE, getClass().getName().
                substring(getClass().getName().lastIndexOf(".") + 1) + "  :  " + "state" + "  :  " + "onPause");
        super.onPause();
    }

    @Override
    public void onStop() {
        Log.d(BottomFragmentActivity.JAVA_FRAGMENT_AND_ACTIVITY_LIFE, getClass().getName().
                substring(getClass().getName().lastIndexOf(".") + 1) + "  :  " + "state"
                + "  :  " + "onStop" + "  Thread : "
                + Thread.currentThread().getId());
        super.onStop();
    }

    @Override
    public void onStart() {
        Log.d(BottomFragmentActivity.JAVA_FRAGMENT_AND_ACTIVITY_LIFE, getClass().getName().
                substring(getClass().getName().lastIndexOf(".") + 1) + "  :  " + "state" + "  :  " + "onStart");
        super.onStart();
    }

    @Override
    public void onResume() {
        Log.d(BottomFragmentActivity.JAVA_FRAGMENT_AND_ACTIVITY_LIFE, getClass().getName().
                substring(getClass().getName().lastIndexOf(".") + 1) + "  :  " + "state" + "  :  " + "onResume");
        super.onResume();
    }

    @Override
    public void onDestroyView() {
        binding = null;
        Log.d(BottomFragmentActivity.JAVA_FRAGMENT_AND_ACTIVITY_LIFE, getClass().getName().
                substring(getClass().getName().lastIndexOf(".") + 1) + "  :  " + "state" + "  :  " + "onDestroyView");
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        Log.d(BottomFragmentActivity.JAVA_FRAGMENT_AND_ACTIVITY_LIFE, getClass().getName().
                substring(getClass().getName().lastIndexOf(".") + 1) + "  :  " + "state" + "  :  " + "onDestroy");
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        Log.d(BottomFragmentActivity.JAVA_FRAGMENT_AND_ACTIVITY_LIFE, getClass().getName().
                substring(getClass().getName().lastIndexOf(".") + 1) + "  :  " + "state" + "  :  " + "onDetach");
        super.onDetach();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        Log.d(BottomFragmentActivity.JAVA_FRAGMENT_AND_ACTIVITY_LIFE, getClass().getName().
                substring(getClass().getName().lastIndexOf(".") + 1) + "  :  " + "state" + "  :  " + "onSaveInstanceState");
        super.onSaveInstanceState(outState);
        Bundle bundle = new Bundle();
        bundle.putString("1", "11");
        outState.putBundle("1", bundle);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        Log.d(BottomFragmentActivity.JAVA_FRAGMENT_AND_ACTIVITY_LIFE, getClass().getName().
                substring(getClass().getName().lastIndexOf(".") + 1) + "  :  " + "state" + "  :  " + "onViewStateRestored");
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null && null != savedInstanceState.getBundle("1")) {
            Log.d(BottomFragmentActivity.JAVA_FRAGMENT_AND_ACTIVITY_LIFE,
                    savedInstanceState.getBundle("1").getString("1", "null"));
        }
    }
}