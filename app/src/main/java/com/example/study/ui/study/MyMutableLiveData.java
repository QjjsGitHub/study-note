package com.example.study.ui.study;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import java.util.concurrent.atomic.AtomicBoolean;

public class MyMutableLiveData<T> extends MutableLiveData {

    private final AtomicBoolean mPending = new AtomicBoolean(false);

    public MyMutableLiveData(T aNull) {
        super(aNull);
    }


    @Override
    public void observe(@NonNull LifecycleOwner owner, @NonNull Observer observer) {
        if (hasActiveObservers()) {
            Log.w("SingleLiveEvent", "Multiple observers registered but only one " +
                    "will be notified of changes.");
        }
        super.observe(owner, new Observer() {
            @Override
            public void onChanged(Object o) {
                if (mPending.compareAndSet(true, false)) {
                    observer.onChanged(o);
                }
            }
        });

    }

    @Override
    public void setValue(Object value) {
        mPending.set(true);
        super.setValue(value);
    }


}
