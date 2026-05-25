package com.example.study.service;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.MainThread;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.study.R;
import com.example.study.databinding.ActivityServiceBinding;

public class ServiceActivity extends AppCompatActivity {

    private ActivityServiceBinding binding;

    public static final String TAG = "ServiceActivityLog";

    private boolean isBound = false;
    private boolean isAidlBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityServiceBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent serviceIntent = new Intent(ServiceActivity.this, BindService.class);
        binding.button13.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Looper
                //Log.d(ServiceActivity.TAG, "MainThread:" +   "  state :onClick startService ");
                Log.d(ServiceActivity.TAG, "Thread:" + Thread.currentThread().getId() + "  state :onClick startService ");
                startService(serviceIntent);
            }
        });

        binding.button15.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopService(serviceIntent);
            }
        });

        binding.button18.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // 包名一般有命名空间决定 namespace = "com.example.study"
                Intent intent = new Intent();
                intent.setComponent(new ComponentName("com.example.study", "com.example.study.service.StartService"));

                isAidlBound = bindService(intent, myAidlServiceConnection, BIND_AUTO_CREATE);

            }
        });


        binding.button14.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent bindServiceIntent = new Intent(ServiceActivity.this, BindService.class);
                isBound = bindService(bindServiceIntent, myServiceConnection, BIND_AUTO_CREATE);
            }
        });

        binding.button16.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isBound) {
                    unbindService(myServiceConnection);
                    myBinder = null;
                    isBound = false;
                }
            }
        });

        binding.button17.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (myBinder != null) {
                    myBinder.getCount();
                }
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isBound) {
            unbindService(myServiceConnection);
            isBound = false;
        }
        if (isAidlBound) {
            unbindService(myAidlServiceConnection);
            isAidlBound = false;
        }
    }


    private final ServiceConnection myAidlServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            IMyAidlInterface iMyAidlInterface = IMyAidlInterface.Stub.asInterface(service);
            Log.d(ServiceActivity.TAG, "Thread:" + Thread.currentThread().getId() + "  state :onServiceConnected ");
            try {
                Log.d(ServiceActivity.TAG, "Thread:" + Thread.currentThread().getId() + "  state :sayHi "
                        + iMyAidlInterface.sayHi("11"));

                Bundle bundle = new Bundle();
                bundle.putParcelable("people", new People());

                Log.d(ServiceActivity.TAG, "Thread:" + Thread.currentThread().getId() + "  state :savePeople ");
                iMyAidlInterface.savePeople(bundle);

            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(ServiceActivity.TAG, "Thread:" + Thread.currentThread().getId() + "  state :onServiceDisconnected ");
            //myBinder.getCount();
        }
    };

    private BindService.MyBinder myBinder = null;
    private final ServiceConnection myServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            myBinder = (BindService.MyBinder) service;
            Log.d(ServiceActivity.TAG, "Thread:" + Thread.currentThread().getId() + "  state :onServiceConnected ");
            //在Activity调用Service类的方法
            //myBinder.getCount();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            myBinder = null;
            Log.d(ServiceActivity.TAG, "Thread:" + Thread.currentThread().getId() + "  state :onServiceDisconnected ");
            //myBinder.getCount();
        }
    };

}