package com.example.study.service;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

/**
 * @author 16555
 */
public class StartService extends Service {

    private static final String TAG = "AidlStartService";

    //private myBinder

    private final IMyAidlInterface.Stub myBinder = new IMyAidlInterface.Stub() {

        @Override
        public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {

        }

        @Override
        public String sayHi(String message) throws RemoteException {
            Log.d(ServiceActivity.TAG, "Thread:" + Thread.currentThread().getName() + "  state :sayHi ");
            return "hihi" + message;
        }

        @Override
        public void savePeople(Bundle bundle) throws RemoteException {
            bundle.setClassLoader(getClass().getClassLoader());
            People people = bundle.getParcelable("people");
            Log.d(ServiceActivity.TAG, "Thread:" + Thread.currentThread().getName() + "  state :savePeople " + people.number + ":" + people.name);// Do more with the parcelable.
        }


    };

    public StartService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(ServiceActivity.TAG, "Thread:" + Thread.currentThread().getName() + "  state :onStartCommand ");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        Log.d(ServiceActivity.TAG, "Thread:" + Thread.currentThread().getName() + "  state :onCreate ");
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(ServiceActivity.TAG, "Thread:" + Thread.currentThread().getName() + "  state :onBind ");
        return myBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(ServiceActivity.TAG, "Thread:" + Thread.currentThread().getName() + "  state :onUnbind ");
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        Log.d(ServiceActivity.TAG, "Thread:" + Thread.currentThread().getName() + "  state :onDestroy ");
        super.onDestroy();
    }
}