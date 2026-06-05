package com.example.study.fragment.activityFragment;

import static com.example.study.fragment.FragmentActivity.FRAGMENT_AND_ACTIVITY_LIFE;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.study.R;
import com.example.study.databinding.FragmentBlankBinding;
import com.example.study.fragment.FragmentManagerActivity;

/**
 * @author 16555
 */
public class BlankFragment extends Fragment {

    private BlankViewModel mViewModel;

    private final String name;

    private FragmentBlankBinding binding;

    public BlankFragment(String name) {
        this.name = name;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        Log.d(FRAGMENT_AND_ACTIVITY_LIFE, getClass().getName().
                substring(getClass().getName().lastIndexOf(".") + 1) + "  :  " + name + "  :  " + "onAttach");
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(FRAGMENT_AND_ACTIVITY_LIFE, getClass().getName().
                substring(getClass().getName().lastIndexOf(".") + 1) + "  :  " + name + "  :  " + "onCreate");
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Log.d(FRAGMENT_AND_ACTIVITY_LIFE, getClass().getName().
                substring(getClass().getName().lastIndexOf(".") + 1) + "  :  " + name + "  :  " + "onCreateView");
        binding = FragmentBlankBinding.inflate(inflater, container, false);

        binding.fragmentName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), FragmentManagerActivity.class));
            }
        });

        mViewModel = new ViewModelProvider(this).get(BlankViewModel.class);

        mViewModel.getFragmentName().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                binding.fragmentName.setText(s);
            }
        });

        mViewModel.setFragmentName(name);

        binding.fragmentName.setText(name);

        binding.button46.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewModel.setFragmentName("viewModel更新数据");
            }
        });

        binding.button47.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getParentFragmentManager();
                fragmentManager.beginTransaction().remove(BlankFragment.this).commitNow();

                /*if (container != null) {
                    fragmentManager.beginTransaction().add(container.getId(), new BlankFragment("手动创建"), "Tag").commitNow();
                }*/
            }
        });

        return binding.getRoot();
    }

    @Override
    public void onPause() {
        Log.d(FRAGMENT_AND_ACTIVITY_LIFE, getClass().getName().
                substring(getClass().getName().lastIndexOf(".") + 1) + "  :  " + name + "  :  " + "onPause");
        super.onPause();
    }

    @Override
    public void onStop() {
        Log.d(FRAGMENT_AND_ACTIVITY_LIFE, getClass().getName().
                substring(getClass().getName().lastIndexOf(".") + 1) + "  :  " + name + "  :  " + "onStop");
        super.onStop();
    }

    @Override
    public void onStart() {
        Log.d(FRAGMENT_AND_ACTIVITY_LIFE, "blank  :  Thread: " + String.valueOf(Thread.currentThread().getId()));
        Log.d(FRAGMENT_AND_ACTIVITY_LIFE, getClass().getName().
                substring(getClass().getName().lastIndexOf(".") + 1) + "  :  " + name + "  :  " + "onStart");
        super.onStart();
    }

    @Override
    public void onResume() {
        Log.d(FRAGMENT_AND_ACTIVITY_LIFE, getClass().getName().
                substring(getClass().getName().lastIndexOf(".") + 1) + "  :  " + name + "  :  " + "onResume");
        super.onResume();
    }

    @Override
    public void onDestroyView() {
        Log.d(FRAGMENT_AND_ACTIVITY_LIFE, getClass().getName().
                substring(getClass().getName().lastIndexOf(".") + 1) + "  :  " + name + "  :  " + "onDestroyView");
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {

        //mViewModel.clear$lifecycle_viewmodel();

        Log.d(FRAGMENT_AND_ACTIVITY_LIFE, getClass().getName().
                substring(getClass().getName().lastIndexOf(".") + 1) + "  :  " + name + "  :  " + "onDestroy");
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        Log.d(FRAGMENT_AND_ACTIVITY_LIFE, getClass().getName().
                substring(getClass().getName().lastIndexOf(".") + 1) + "  :  " + name + "  :  " + "onDetach");
        super.onDetach();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        Log.d(FRAGMENT_AND_ACTIVITY_LIFE, getClass().getName().
                substring(getClass().getName().lastIndexOf(".") + 1) + "  :  " + name + "  :  " + "onSaveInstanceState");
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        Log.d(FRAGMENT_AND_ACTIVITY_LIFE, getClass().getName().
                substring(getClass().getName().lastIndexOf(".") + 1) + "  :  " + name + "  :  " + "onViewStateRestored");
        super.onViewStateRestored(savedInstanceState);
    }


}