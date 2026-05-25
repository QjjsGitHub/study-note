package com.example.study.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;


public class MyFirstBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String name = "MyFirstBroadcastReceiver";
        Toast.makeText(context, name, Toast.LENGTH_SHORT).show();
    }
}
