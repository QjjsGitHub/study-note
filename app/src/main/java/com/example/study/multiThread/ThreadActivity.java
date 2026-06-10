package com.example.study.multiThread;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.study.Handle.HandleActivity;
import com.example.study.R;
import com.example.study.databinding.ActivityThreadBinding;

import java.lang.ref.WeakReference;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

public class ThreadActivity extends AppCompatActivity {

    private static final String TAG = "ThreadActivityTest";

    private ThreadLocal<String> threadLocalName = new ThreadLocal<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        ActivityThreadBinding binding = ActivityThreadBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.button20.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyThreadPool.getInstance().ReentrantLockTest();
            }
        });


        binding.button21.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyThreadPool.getInstance().testArrayBlockingQueueThreadPoolExecutor();
            }
        });

        binding.button22.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyThreadPool.getInstance().testLinkedBlockingQueueThreadPoolExecutor();
            }
        });

        binding.button23.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyThreadPool.getInstance().testsPriorityBlockingQueueThreadPoolExecutor();
            }
        });

        binding.button24.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyThreadPool.getInstance().testsSynchronousQueueThreadPoolExecutor();
            }
        });

        binding.button30.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createThread();
            }
        });

    }

    private void createThread() {

        Thread firstThread = new Thread(new Runnable() {
            @Override
            public void run() {

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ThreadActivity.this, "firstThread", Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });
        firstThread.start();


        FutureTask<String> futureTask = new FutureTask(new Callable<String>() {
            @Override
            public String call() throws Exception {

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ThreadActivity.this, "secondThread", Toast.LENGTH_SHORT).show();
                    }
                });

                Log.d(TAG, "Thread: " + Thread.currentThread().getName() + "  secondThread: start ");
                Thread.sleep(3000);
                Log.d(TAG, "Thread: " + Thread.currentThread().getName() + "  secondThread: stop ");

                return "secondThread result";
            }
        });

        Thread secondThread = new Thread(futureTask);
        secondThread.start();


        try {
            Log.d(TAG, "Thread: " + Thread.currentThread().getName() + "  secondThread: " + futureTask.get());
        } catch (ExecutionException | InterruptedException e) {
            //ThreadActivityTestthrow new RuntimeException(e);
        }


        FutureTask futureTask1 = new FutureTask(Executors.callable(new Runnable() {
            @Override
            public void run() {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ThreadActivity.this, "thirdThread", Toast.LENGTH_SHORT).show();
                    }
                });

                Log.d(TAG, "Thread: " + Thread.currentThread().getName() + "  thirdThread: start ");
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                Log.d(TAG, "Thread: " + Thread.currentThread().getName() + "  thirdThread: stop ");

            }
        }, "thirdThread result"));

        Thread thirdThread = new Thread(futureTask1);
        thirdThread.start();


        try {
            Log.d(TAG, "Thread: " + Thread.currentThread().getName() + "  thirdThread: " + futureTask1.get());
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        new Thread() {
            @Override
            public void run() {
                super.run();

                threadLocalName.set("888");

                extracted(threadLocalName);

                System.gc();

                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                extracted(threadLocalName);

                threadLocalName.remove();

                System.gc();

                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                extracted(threadLocalName);

            }
        }.start();


        new Thread() {
            @Override
            public void run() {
                super.run();

                Object s = new ThreadActivity.B();

                WeakReference<Object> s1 = new WeakReference<Object>(s);

                extracted(s1);

                System.gc();

                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                extracted(s1);

                System.gc();

                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                extracted(s1);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }

                        extracted(s1);

                    }
                }).start();

            }
        }.start();

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    private static void extracted(ThreadLocal<String> s1) {
        if (null != s1 && null != s1.get()) {
            Log.d(TAG, s1.get().toString());
        } else {
            Log.d(TAG, "null");
        }
    }

    private static void extracted(WeakReference<Object> s1) {
        if (null != s1 && null != s1.get()) {
            Log.d(TAG, s1.get().toString());
        } else {
            Log.d(TAG, "null");
        }
    }

    public class B {
        String s = "1111";

        B() {

        }

        @NonNull
        @Override
        public String toString() {
            return s;
        }
    }
}