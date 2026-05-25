package com.example.study.fragment.dashboard;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.study.databinding.FragmentDashboardBinding;
import com.example.study.fragment.BottomFragmentActivity;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Log.d(BottomFragmentActivity.JAVA_FRAGMENT_AND_ACTIVITY_LIFE, getClass().getName().
                substring(getClass().getName().lastIndexOf(".") + 1) + "  :  " + "state" + "  :  " + "onCreateView");

        DashboardViewModel dashboardViewModel = new ViewModelProvider(this).get(DashboardViewModel.class);

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textDashboard;
        dashboardViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
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
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        Log.d(BottomFragmentActivity.JAVA_FRAGMENT_AND_ACTIVITY_LIFE, getClass().getName().
                substring(getClass().getName().lastIndexOf(".") + 1) + "  :  " + "state" + "  :  " + "onViewStateRestored");
        super.onViewStateRestored(savedInstanceState);
    }
}