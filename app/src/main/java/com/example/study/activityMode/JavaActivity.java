package com.example.study.activityMode;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

import com.example.study.internet.FirstFragment.User;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.study.R;
import com.example.study.Tools;
import com.example.study.databinding.ActivityJavaBinding;
import com.example.study.internet.FirstFragment;
import com.example.study.internet.InternetActivity;
import com.example.study.ui.study.ConfirmDialog;
import com.example.study.ui.study.MyViewModel;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * @author 16555
 */
public class JavaActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String JAVA_ACTIVITY_THREAD = "JavaActivity Thread";

    public static final String JAVA_ACTIVITY_TASK = "JavaActivity Task";

    private Handler handler;

    private ActivityJavaBinding binding;

    private ConfirmDialog showCancelDialog;


    static final int PICK_REQUEST = 1337;
    Button viewButton = null;
    Uri contact = null;

    private MyViewModel myViewModel;

    private MutableLiveData<Uri> contactUris = new MutableLiveData<Uri>();

    private ActivityResultLauncher<Intent> activityLauncher;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityJavaBinding.inflate(getLayoutInflater());
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

        binding.textView3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCancelDialog("aaa", "bbb");
            }
        });


        handler =
                new Handler(Objects.requireNonNull(Looper.myLooper()), new Handler.Callback() {
                    @Override
                    public boolean handleMessage(@NonNull Message msg) {
                        return false;
                    }
                }) {
                    @Override
                    public void handleMessage(@NonNull Message msg) {

                    }
                };

        handler.sendMessage(handler.obtainMessage());

        handler.postDelayed(() -> {

        }, 10000);

        handler.dispatchMessage(handler.obtainMessage());

        Thread thread = new MyThread(new WeakReference<Activity>(this));
        thread.start();

        Log.e(JAVA_ACTIVITY_THREAD, Thread.currentThread().getName() + "主线程loop" + Thread.currentThread().getName());

        //thread.interrupt();


        myViewModel =
                new ViewModelProvider(this).get(MyViewModel.class);
        myViewModel.getContact()
                .observe(this, new Observer<String>() {
                    @Override
                    public void onChanged(String contact) {
                        Log.d("myViewModel", "myViewModel onChanged");
                        binding.textView4.setText(contact);
                    }
                });
        contactUris.observe(this, new Observer<Uri>() {
            @Override
            public void onChanged(Uri s) {
                Log.d("contactUris", "onChanged");
            }
        });

        activityLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),

                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult o) {
                        if (o.getData() == null) {
                            return;
                        }
                        if (o.getData().getData() == null) {
                            return;
                        }
                        contact = o.getData().getData();
                        myViewModel.setContact(contact.toString());
                        contactUris.postValue(contact);
                    }
                });
        getContact();


        Log.d("ActivityLife", getClass().getName() + "onCreate");

        //ActivityManager myActivity;

    }

    private void getContact() {
        Button btn = (Button) findViewById(R.id.button7);
        btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI
                        /*Contacts.People.CONTENT_URI*/);
                activityLauncher.launch(i);
            }
        });
        viewButton = findViewById(R.id.button8);
        viewButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (contact != null) {
                    startActivity(new Intent(Intent.ACTION_VIEW, contact));
                }
            }
        });
        //restoreMe();
    }


    public void showCancelDialog(String reportOrderNo, String storeCode) {
        if (showCancelDialog == null) {
            showCancelDialog = new ConfirmDialog(new WeakReference<Context>(this), "取消提醒", "取消后将不再对已添加的内容进行保存", "暂不取消", "确认取消");
            showCancelDialog.setOnViewClickLiatener(new ConfirmDialog.OnViewClickLiatener() {
                @Override
                public void sureClick() {
                    if (TextUtils.isEmpty(reportOrderNo)) {
                        finish();
                    } else {
                        //httpReportOrderCancel(reportOrderNo, storeCode);
                    }
                }

                @Override
                public void cancelClick() {

                }
            });
        }

        if (!showCancelDialog.isShowing()) {
            showCancelDialog.show();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("ActivityLife", getClass().getName() + "onDestroy");
        handler.removeCallbacksAndMessages(null);
        /*handler1.removeCallbacksAndMessages(null);
        thread.interrupt();
        thread = null;*/
        if (showCancelDialog != null && showCancelDialog.isShowing()) {
            showCancelDialog.dismiss();
        }

    }

    static class MyThread extends Thread {

        private WeakReference<Activity> weakReferenceActivity;

        MyThread(WeakReference<Activity> activityWeakReference) {
            weakReferenceActivity = activityWeakReference;
        }

        @Override
        public void run() {
            Looper.prepare();

            Handler handler1 = new Handler(Objects.requireNonNull(Looper.myLooper()));
            handler1.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (Thread.interrupted()) {
                        return;
                    }
                    Log.e(JAVA_ACTIVITY_THREAD, Thread.currentThread().getName() + ":子线程loop:" + Thread.currentThread().getName());

                    if (weakReferenceActivity.get() != null) {
                        Toast.makeText(weakReferenceActivity.get().getApplicationContext(), ":子线程loop:", Toast.LENGTH_SHORT).show();
                    }
                }
            }, 1000);

            try {
                Thread.sleep(1000);
                TimeUnit.SECONDS.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            Looper.loop();
        }
    }


    @Override
    public void onClick(View v) {


        int id = v.getId();
        if (R.id.button2 == id) {
            startActivity(new Intent(getApplicationContext(), singleInstancePerTaskActivity.class));
        } else if (R.id.button3 == id) {
            startActivity(new Intent(this, singleTaskActivity.class));
        } else if (R.id.button4 == id) {
            startActivity(new Intent(this, SingleTopActivity.class));
        } else if (R.id.button5 == id) {
            startActivity(new Intent(this, SingleInstanceActivity.class));
        } else {
            /*Intent intent = new Intent();
            intent.setFlags(FLAG_ACTIVITY_NEW_TASK);*/
            startActivity(new Intent(this, JavaActivity.class));
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_REQUEST) {
            if (resultCode == RESULT_OK) {
                contact = data.getData();
                viewButton.setEnabled(true);
            }
        }
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.d("InstanceState", savedInstanceState.getBundle("contact").getString("contact", "contact is null"));
        Log.d("ActivityLife", getClass().getName() + "onRestoreInstanceState");
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Bundle bundle = new Bundle();
        bundle.putString("contact", contact != null ? contact.toString() : "null");
        //outState = bundle;
        outState.putBundle("contact", bundle);
        Log.d("ActivityLife", getClass().getName() + "onSaveInstanceState");
    }

    Observable<List<User>> getLocalUser() {

        List list = new ArrayList<User>(10);

        list.add(new User(10, "one1"));
        list.add(new User(11, "one2"));
        list.add(new User(12, "one3"));
        list.add(new User(13, "one4"));
        list.add(new User(14, "one5"));

        return Observable.create(new ObservableOnSubscribe() {
            @Override
            public void subscribe(ObservableEmitter emitter) throws Exception {
                emitter.onNext(list); // 发送值
                emitter.onComplete(); // 完成发射
            }
        });
    }

    void testJava() {

        Disposable disposable = getLocalUser()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        users -> {
                            StringBuilder string = new StringBuilder();

                            var iterator = users.iterator();

                            while (iterator.hasNext()) {
                                String s = iterator.next().getName();
                                string.append(s);
                            }
                            Log.d(InternetActivity.INTERNET_ACTIVITY_TAG, "on next: " + string);
                        }
                        ,
                        new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {

                            }
                        }
                );

    }


}
