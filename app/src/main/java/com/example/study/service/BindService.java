package com.example.study.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.graphics.drawable.IconCompat;

import com.example.study.MainActivity;
import com.example.study.R;

public class BindService extends Service {

    private final MyBinder mBinder = new MyBinder(this);

    private int count = 1;
    private boolean quit = false;

    public BindService() {
    }

    @Override
    public void onCreate() {
        Log.d(ServiceActivity.TAG, "Thread:" + Thread.currentThread().getName() + "  state :onCreate ");
        super.onCreate();

        setForegroundService();

        quit = true;

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (quit) {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        Log.d("BindService error", e.toString());
                    }
                    count++;

                    //getTopActivityPackageName(getApplicationContext());

                    Log.d(ServiceActivity.TAG, "Thread:" + Thread.currentThread().getName() + "  state :count++ " + count);
                }
            }
        }).start();

    }

    /*public void getTopActivityPackageName(Context context) {
        final int PROCESS_STATE_TOP = 2;
        try {
            Field processStateField = ActivityManager.RunningAppProcessInfo.class.getDeclaredField("processState");
            List<ActivityManager.RunningAppProcessInfo> processes = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo process : processes) {
                if (process.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    int state = processStateField.getInt(process);
                    if (state == PROCESS_STATE_TOP) {
                        String[] packageName = process.pkgList;
                        for (String name : packageName) {
                            Log.d("processState", name);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/

    @Nullable
    @Override
    public ComponentName startForegroundService(Intent service) {
        return super.startForegroundService(service);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(ServiceActivity.TAG, "Thread:" + Thread.currentThread().getName() + "  state :onBind ");
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(ServiceActivity.TAG, "Thread:" + Thread.currentThread().getName() + "  state :onStartCommand ");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(ServiceActivity.TAG, "Thread:" + Thread.currentThread().getName() + "  state :onUnbind ");
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        Log.d(ServiceActivity.TAG, "Thread:" + Thread.currentThread().getName() + "  state :onRebind ");
        super.onRebind(intent);
    }

    @Override
    public void onDestroy() {
        quit = false;
        Log.d(ServiceActivity.TAG, "Thread:" + Thread.currentThread().getName() + "  state :onDestroy ");
        super.onDestroy();
    }

    void setForegroundService() {

        String channelId = "channelId";
        String name = "channelName";
        int importance = NotificationManager.IMPORTANCE_LOW;
        NotificationChannel channel;

        channel = new NotificationChannel(channelId, name, importance);


        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(channel);

        /**
         *
         * 没有则不创建
         FLAG_NO_CREATE
         先点击那个那个生效，其余失效
         FLAG_ONE_SHOT
         取消旧的
         FLAG_CANCEL_CURRENT
         全部替换成新的
         FLAG_UPDATE_CURRENT
         */


        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(this, channelId)
                .setContentIntent(pendingIntent)
                .setContentTitle("标题")
                .setContentText("内容")
                .setSmallIcon(IconCompat.createWithResource(this, R.drawable.icon))
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.icon))
                .setStyle(new NotificationCompat.BigPictureStyle().bigPicture(
                        BitmapFactory.decodeResource(getResources(), R.drawable.icon)))
                .setTicker("哈哈")
                .setAutoCancel(true)
                .build();

        //notificationManager.notify("tag", 1, notification);

        startForeground(1, notification);
    }

    public int getCount() {
        return count;
    }

    static class MyBinder extends Binder {

        private final BindService bindService;

        MyBinder(BindService bindService) {
            this.bindService = bindService;
        }

        public void getCount() {
            Log.d(ServiceActivity.TAG, "Thread:" + Thread.currentThread().getName() +
                    "  执行service_bind_get_count: " + bindService.getCount());
        }

    }

}