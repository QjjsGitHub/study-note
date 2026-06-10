package com.example.study.broadcast;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.study.R;
import com.example.study.databinding.ActivityBroadcastBinding;

import java.util.Objects;

/**
 * @author 16555
 */
public class BroadcastActivity extends AppCompatActivity {

    public static final String COM_EXAMPLE_STUDY_BROADCAST_MY_SECOND_BROADCAST = "com.example.study.broadcast.MY_SECOND_BROADCAST";
    private boolean register = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);


        ActivityBroadcastBinding binding = ActivityBroadcastBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        /*MyBroadcastReceiver myBroadcastReceiver = new MyBroadcastReceiver();
        IntentFilter intentFilter4 = new IntentFilter();
        intentFilter4.addAction("com.example.study.broadcast");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(myBroadcastReceiver, intentFilter4, Context.RECEIVER_EXPORTED);
        } else {
            registerReceiver(myBroadcastReceiver, intentFilter4);
        }*/


        /*从Android 8.0 (API级别26) 开始，系统对清单声明的接收者施加了额外的限制。
        如果你的应用程序的目标是Android 8.0 或更高版本，
        你不能使用清单来为大多数隐式广播(广播不是专门针对你的应用程序) 声明一个接收器。
        当用户正在积极使用你的应用程序时，你仍然可以使用上下文注册的接收器

        在当前应用发送广播发送：
        Intent intent = new Intent("com.example.broadcasttest.MY_BROADCAST");

        intent.putExtra("message", "send a message!!!");

        intent.setPackage(getPackageName());

        sendBroadcast(intent);

        发送给其他应用的广播：
        Intent intent = new Intent("com.example.broadcasttest.MY_BROADCAST");

        intent.setComponent(new ComponentName("接收广播的包名", "接收广播的包名.MyBroadcastReceiver"));

        intent.putExtra("message", "send a message!!!");

        sendBroadcast(intent);
        setComponent（）

        两个参数pkg ：发送指定应用的包名cls ：指定发送到广播接收器的类名，必须是全名，带上包名！！！*/


        MySecondBroadcastReceiver mySecondBroadcastReceiver = new MySecondBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(COM_EXAMPLE_STUDY_BROADCAST_MY_SECOND_BROADCAST);
        intentFilter.setPriority(100);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(mySecondBroadcastReceiver, intentFilter, Context.RECEIVER_EXPORTED);
        } else {
            registerReceiver(mySecondBroadcastReceiver, intentFilter);
        }


        MyThirdBroadcastReceiver myThirdBroadcastReceiver = new MyThirdBroadcastReceiver();
        IntentFilter intentFilter3 = new IntentFilter();
        intentFilter3.addAction(COM_EXAMPLE_STUDY_BROADCAST_MY_SECOND_BROADCAST);
        intentFilter3.setPriority(200);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(myThirdBroadcastReceiver, intentFilter3, Context.RECEIVER_EXPORTED);
        } else {
            registerReceiver(myThirdBroadcastReceiver, intentFilter3);
        }


        MyLocalBroadcastReceiver myLocalBroadcastReceiver = new MyLocalBroadcastReceiver();
        IntentFilter intentFilter2 = new IntentFilter();
        intentFilter2.addAction("com.example.study.broadcast.MY_LOCAL_BROADCAST");
        intentFilter2.setPriority(110);
        //注册应用内广播接收器
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.registerReceiver(myLocalBroadcastReceiver, intentFilter2);


        binding.button26.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent("com.example.study.broadcast");
                intent.setPackage("com.example.study");
                intent.putExtra("key", "value");
                sendBroadcast(intent,
                        null);
            }
        });


        binding.button27.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendOrderedBroadcast(new Intent(COM_EXAMPLE_STUDY_BROADCAST_MY_SECOND_BROADCAST).putExtra("key", "order"),
                        null);
            }
        });

        //取消注册应用内广播接收器
        //localBroadcastManager.unregisterReceiver(mBroadcastReceiver);
        binding.button28.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction("com.example.study.broadcast.MY_LOCAL_BROADCAST");
                intent.putExtra("name", "广播内容");
                //发送应用内广播
                localBroadcastManager.sendBroadcast(intent);
            }
        });

        MyFourBroadcastReceiver myFourBroadcastReceiver = new MyFourBroadcastReceiver();
        binding.button29.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!register) {
                    register = true;
                    IntentFilter intentFilter4 = new IntentFilter();
                    intentFilter4.addAction(COM_EXAMPLE_STUDY_BROADCAST_MY_SECOND_BROADCAST);
                    intentFilter4.setPriority(300);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        registerReceiver(myFourBroadcastReceiver, intentFilter4, Context.RECEIVER_EXPORTED);
                    } else {
                        registerReceiver(myFourBroadcastReceiver, intentFilter4);
                    }
                } else {
                    register = false;
                    unregisterReceiver(myFourBroadcastReceiver);
                }

            }
        });
    }


    public static class MyLocalBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(context, "MyLocalBroadcastReceiver Got it: " + intent.getExtras(), Toast.LENGTH_SHORT).show();
            //abortBroadcast();
        }
    }

    public static class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(context, "MyBroadcastReceiver: " + intent.getExtras().getString("key") + "action: " + intent.getAction(),
                    Toast.LENGTH_SHORT).show();
            //abortBroadcast();
        }
    }

    public static class MySecondBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            int code = getResultCode();
            String data = getResultData();

            Toast.makeText(context, "MySecondBroadcastReceiver" + intent.getExtras().getString("key") + ":" + code + ":" + data, Toast.LENGTH_SHORT).show();
            //abortBroadcast();
        }
    }

    public static class MyThirdBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //String s = intent.getExtras().getString("key");
            //intent.getExtras().putString("key", "third:" + s);

            int code = getResultCode();
            String data = getResultData();
            setResultCode(code + 1);
            setResultData(data + "haha");

            Toast.makeText(context, "MyThirdBroadcastReceiver Got it: " + intent.getExtras().getString("key"), Toast.LENGTH_SHORT).show();
            //abortBroadcast();
        }
    }

    public static class MyFourBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (Objects.equals(intent.getAction(), COM_EXAMPLE_STUDY_BROADCAST_MY_SECOND_BROADCAST)) {
                abortBroadcast();
            }

            Toast.makeText(context, "MyFourBroadcastReceiver Got it: " + intent.getExtras().getString("key"), Toast.LENGTH_SHORT).show();
            //abortBroadcast();
        }
    }

}