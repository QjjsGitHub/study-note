package com.example.study.activityMode;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.study.R;
import com.example.study.Tools;
import com.example.study.databinding.ActivitySingleInstancePerTaskBinding;

import java.lang.ref.WeakReference;
import java.util.List;

public class singleInstancePerTaskActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        ActivitySingleInstancePerTaskBinding binding = ActivitySingleInstancePerTaskBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, new OnApplyWindowInsetsListener() {
                    @NonNull
                    @Override
                    public WindowInsetsCompat onApplyWindowInsets(@NonNull View v, @NonNull WindowInsetsCompat insets) {
                        Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                        v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                        return insets;
                    }
                }
        );

        binding.button2.setOnClickListener(this);
        binding.button3.setOnClickListener(this);
        binding.button4.setOnClickListener(this);
        binding.button5.setOnClickListener(this);
        binding.button6.setOnClickListener(this);

        Log.d("ActivityLife", getClass().getName() + "onCreate");

    }

    @Override
    public void onClick(View v) {
        if (R.id.button2 == v.getId()) {
            startActivity(new Intent(this, SingleInstanceActivity.class));
        } else if (R.id.button3 == v.getId()) {
            startActivity(new Intent(this, singleTaskActivity.class));
        } else if (R.id.button4 == v.getId()) {
            startActivity(new Intent(this, SingleTopActivity.class));
        } else if (R.id.button5 == v.getId()) {
            startActivity(new Intent(this, JavaActivity.class));
        } else {
            startActivity(new Intent(this, singleInstancePerTaskActivity.class));
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Tools.getInstance().getTaskInfo(new WeakReference<>(this));
        Log.d("ActivityLife", getClass().getName() + "onStart");
    }

    @Override
    protected void onNewIntent(@NonNull Intent intent) {
        super.onNewIntent(intent);
        Log.d("ActivityLife", getClass().getName() + "onNewIntent");
    }

    @Override
    protected void onRestart() {
        Log.d("ActivityLife", getClass().getName() + "onRestart");
        super.onRestart();
    }

    @Override
    protected void onResume() {
        Log.d("ActivityLife", getClass().getName() + "onResume");
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.d("ActivityLife", getClass().getName() + "onPause");
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.d("ActivityLife", getClass().getName() + "onStop");
        super.onStop();
    }


}