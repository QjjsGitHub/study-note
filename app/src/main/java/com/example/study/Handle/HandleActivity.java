package com.example.study.Handle;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.compose.foundation.text.Handle;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.study.R;
import com.example.study.databinding.ActivityHandleBinding;
import com.example.study.databinding.ActivityThreadBinding;

import java.lang.ref.WeakReference;

public class HandleActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "HandleActivityLog";

    private MyHandler myHandler;

    private Handler handle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);


        ActivityHandleBinding binding = ActivityHandleBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.button36.setOnClickListener(this);
        binding.button37.setOnClickListener(this);
        binding.button38.setOnClickListener(this);
        binding.button39.setOnClickListener(this);
        binding.button40.setOnClickListener(this);

        //通过Message.callback实现runnable
        Message message = Message.obtain(new Handler(), new Runnable() {
            @Override
            public void run() {

            }
        });

        message.what = 1;
        message.arg1 = 1;
        message.arg2 = 2;
        message.obj = "string";

        myHandler = new MyHandler(new WeakReference<>(this));
        myHandler.sendMessage(message);


        //通过Handler.callback实现runnable
        handle = new Handler(Looper.getMainLooper(), new Handler.Callback() {
            @Override
            public boolean handleMessage(@NonNull Message msg) {
                switch (msg.what) {
                    case 1: {
                        Log.d(TAG, "handler post message1");
                    }
                    break;
                    case 2: {
                        Log.d(TAG, "handler post message2");
                    }
                    break;
                    default:
                        Log.d(TAG, "handler post message default");
                        break;
                }

                //如果返回真，将不再处理handleMessage方法
                return false;
            }
        });


    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.button36) {

            //通过Message.callback实现runnable
            myHandler.post(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "myHandler post message");
                }
            });

        } else if (id == R.id.button37) {

            Message message =
                    Message.obtain();
            message.what = 1;
            myHandler.sendMessage(message);

        } else if (id == R.id.button38) {

            Message message =
                    Message.obtain();
            message.what = 2;
            myHandler.sendMessage(message);

        } else if (id == R.id.button39) {

            Message message =
                    Message.obtain();
            message.what = 1;
            handle.sendMessage(message);

        } else if (id == R.id.button40) {

            Message message = handle.obtainMessage();
                    Message.obtain();
            message.what = 2;
            handle.sendMessage(message);

        }
    }

    //通过重写handleMessage方法实现
    static class MyHandler extends Handler {

        private final WeakReference<HandleActivity> mHandleActivityWeakReference;

        MyHandler(WeakReference<HandleActivity> handleActivityWeakReference) {
            super(handleActivityWeakReference.get().getMainLooper());
            mHandleActivityWeakReference = handleActivityWeakReference;
        }

        @Override
        public void handleMessage(@NonNull Message msg) {

            HandleActivity handleActivity = mHandleActivityWeakReference.get();
            if (handleActivity != null) {

                switch (msg.what) {
                    case 1: {
                        Log.d(TAG, "myHandler post message1");
                        break;
                    }
                    case 2: {
                        Log.d(TAG, "myHandler post message2");
                    }
                    break;
                    default:
                        Log.d(TAG, "myHandler post message default");
                }

            }

            super.handleMessage(msg);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        myHandler.removeCallbacksAndMessages(null);
    }
}