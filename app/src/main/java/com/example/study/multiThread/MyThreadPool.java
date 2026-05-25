package com.example.study.multiThread;

import android.util.Log;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author 16555
 */
public class MyThreadPool {

    private final ThreadPoolExecutor arrayBlockingQueueThreadPoolExecutor;
    private final ThreadPoolExecutor synchronousQueueThreadPoolExecutor;
    private final ThreadPoolExecutor linkBlockBlockingQueueThreadPoolExecutor;


    //代表实现：PriorityBlockingQueue
//特点：队列中的元素会根据其优先级进行排序，优先级高的元素会先被取出执行。
//适用场景：适用于需要按照任务优先级顺序执行的场景。通过调整任务的优先级，可以确保重要任务得到优先处理。
    private final ThreadPoolExecutor priorityBlockingQueueThreadPoolExecutor = new ThreadPoolExecutor(1, 1, 30, TimeUnit.SECONDS,
            new PriorityBlockingQueue<Runnable>(11, new Comparator<Runnable>() {
                @Override
                public int compare(Runnable o1, Runnable o2) {
                    if (o1 instanceof PriorityRunnable && o2 instanceof PriorityRunnable) {
                        return ((PriorityRunnable) o2).compareTo((PriorityRunnable) o1);
                    }
                    return 0;
                }
            }),
            new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, "priorityBlockingQueueThreadPoolExecutor: ");
                }
            },
            new RejectedExecutionHandler() {
                @Override
                public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {

                }
            });

    /**
     * 防止重排序
     */
    private static volatile MyThreadPool myThreadPool;

    /**
     * 创建公平锁
     */
    private static final ReentrantLock fairLock = new ReentrantLock(true);

    private int fairNumber = 0;

    /**
     * 创建非公平锁 默认非公平锁
     */
    private static final ReentrantLock nonFairLock = new ReentrantLock(false);

    private int unFairNumber = 0;

    private static final String TAG = "MyThreadPool";


    public void getRunnable(ReentrantLock reentrantLock, String name, int num, boolean fair) {
        long time = System.currentTimeMillis();
        for (int i = 100; i > 0; i--) {
            reentrantLock.lock();  // 获取公平锁
            try {
                System.out.println(name + " - 线程" + num + "获取到锁");
                if (fair) {
                    fairNumber++;
                } else {
                    unFairNumber++;
                }
                //Thread.sleep(10);
            } finally {
                System.out.println(name + " - 线程" + num + "释放锁" + " number:" + (fair ? fairNumber : unFairNumber));
                reentrantLock.unlock();  // 释放公平锁
            }
        }
        System.out.println(name + " - 线程" + num + " 耗时：" + (System.currentTimeMillis() - time));
    }

    public void ReentrantLockTest() {

        // 公平锁示例
        Thread fairThread1 = new Thread(() -> {
            getRunnable(fairLock, "真公平锁", 1, true);
        });


        Thread fairThread2 = new Thread(() -> {
            getRunnable(fairLock, "真公平锁", 2, true);
        });

        // 非公平锁示例
        Thread nonFairThread1 = new Thread(() -> {
            getRunnable(nonFairLock, "非公平锁", 1, false);
        });

        Thread nonFairThread2 = new Thread(() -> {
            getRunnable(nonFairLock, "非公平锁", 2, false);
        });


        fairThread1.start();

        fairThread2.start();

        nonFairThread1.start();

        nonFairThread2.start();

    }

    public void testPriorityBlockQueue() {
        PriorityBlockingQueue<Integer> p = new PriorityBlockingQueue<Integer>(10, new Comparator<Integer>() {

            @Override
            public int compare(Integer o1, Integer o2) {
                return o1 - o2;
            }
        });

        for (int i = 0; i < 10; i++) {
            StringBuilder s = new StringBuilder("priority");
            int d = (int) Math.round((Math.random() * 100));
            Log.d(TAG, "put: " + d);
            p.put(d);
            for (Object o : p.toArray()) {
                s = s.append(" : ").append(o);
            }
            Log.d(TAG, s.toString());
        }


        for (int i = 0; i < 10; i++) {
            StringBuilder s = new StringBuilder("priority");
            int d = -1;
            try {
                d = (int) p.poll();
            } catch (NullPointerException e) {
                d = -3;
            }
            Log.d(TAG, "get: " + d);
            for (Object o : p.toArray()) {
                s = s.append(" : ").append(o);
            }
            Log.d(TAG, s.toString());
        }
    }

    private MyThreadPool() {

        //testPriorityBlockQueue();

        //List list = new ArrayList();

        //有界队列
        //代表实现：ArrayBlockingQueue、LinkedBlockingQueue（指定具体容量）
        //特点：队列有一个固定的容量限制，当队列满时，尝试添加新任务的操作会被阻塞，直到队列中有空间可用。
        //适用场景：适用于需要控制任务数量，防止资源耗尽的场景。通过调整队列大小和线程池大小，可以灵活控制任务的并发执行
        arrayBlockingQueueThreadPoolExecutor = new ThreadPoolExecutor(3, 6, 30, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(10, true),
                new ThreadFactory() {
                    int i = 1;

                    @Override
                    public Thread newThread(Runnable r) {
                        return new Thread(r, "arrayBlockingQueueThreadPoolExecutor: " + i++);
                    }
                },
                new RejectedExecutionHandler() {
                    @Override
                    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                        if (executor.isTerminated()) {
                            return;
                        }
                        executor.execute(r);
                    }
                });

        //代表实现：LinkedBlockingQueue（当不指定容量或指定容量为Integer.MAX_VALUE时）
        //特点：队列容量理论上是无限的，但受限于JVM的内存。当内存耗尽时，会抛出OutOfMemoryError。
        //适用场景：适用于任务量非常大，且任务执行时间较长，生产者生成任务的速度不会超过消费者处理任务的速度的场景。但需注意内存溢出的风险。
        linkBlockBlockingQueueThreadPoolExecutor = new ThreadPoolExecutor(3, 6, 10, TimeUnit.MINUTES,
                new LinkedBlockingDeque<>(999),
                new ThreadFactory() {
                    int i = 1;

                    @Override
                    public Thread newThread(Runnable r) {
                        return new Thread(r, "linkBlockBlockingQueueThreadPoolExecutor: " + i++);
                    }
                },
                new RejectedExecutionHandler() {
                    @Override
                    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {

                    }
                });


        //特点：这种队列实际上并不存储任何元素，每个插入操作必须等待另一个线程的相应删除操作(也就是：要添加新任务必须得有空闲的线程才能添加)，
        // 反之亦然。即一个线程尝试向队列中添加元素时，必须有另一个线程正在等待接收这个元素。
        //适用场景：适用于任务处理时间较短，且生产者和消费者速度大致匹配的场景。它可以有效减少任务在队列中的等待时间，提高系统的响应速度。
        synchronousQueueThreadPoolExecutor = new ThreadPoolExecutor(3, 6, 30, TimeUnit.SECONDS,
                new SynchronousQueue<>(),
                new ThreadFactory() {
                    int i = 1;

                    @Override
                    public Thread newThread(Runnable r) {
                        return new Thread(r, "synchronousQueueThreadPoolExecutor: " + i++);
                    }
                },
                new RejectedExecutionHandler() {
                    @Override
                    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {

                    }
                });

    }

    static class PriorityRunnable implements Runnable, Comparable<PriorityRunnable> {

        int priority;
        Runnable runnable;

        public PriorityRunnable(int priority, Runnable runnable) {
            this.priority = priority;
            this.runnable = runnable;
        }

        @Override
        public void run() {
            runnable.run();
        }

        @Override
        public int compareTo(PriorityRunnable o) {
            return Integer.compare(priority, o.priority);
        }

    }


    public void testArrayBlockingQueueThreadPoolExecutor() {
        Future<String> a = arrayBlockingQueueThreadPoolExecutor.submit(new Callable<String>() {
            @Override
            public String call() throws Exception {
                Thread.sleep(1000);
                Log.d(TAG, "Thread: " + Thread.currentThread().getId() + "  arrayBlockingQueueThreadPoolExecutor: call");
                return "callable : aaaaa";
            }
        });


        arrayBlockingQueueThreadPoolExecutor.execute(new Runnable() {
            @Override
            public void run() {

                try {
                    //会造成当前线程阻塞
                    String a1 = a.get(1, TimeUnit.MINUTES);
                    Log.d(TAG, "Thread: " + Thread.currentThread().getId() + "  Future: " + a1);
                } catch (InterruptedException e) {
                    Log.d(TAG, "Thread: " + Thread.currentThread().getId() + "  InterruptedException ");
                    //throw new RuntimeException(e);
                } catch (TimeoutException e) {
                    //identical to 等同于
                    Log.d(TAG, "Thread: " + Thread.currentThread().getId() + "  TimeoutException ");
                } catch (ExecutionException e) {
                    //identical to 等同于！
                    //饭！
                    Log.d(TAG, "Thread: " + Thread.currentThread().getId() + "  ExecutionException ");
                }
            }
        });


    }

    public void testsSynchronousQueueThreadPoolExecutor() {
        Future<?> a = synchronousQueueThreadPoolExecutor.submit(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Thread: " + Thread.currentThread().getId() + "  synchronousQueueThreadPoolExecutor: call ");
            }
        });

        try {
            String a1 = (String) a.get(1, TimeUnit.MINUTES);
            Log.d(TAG, "Thread: " + Thread.currentThread().getId() + "  Future: " + a1);
        } catch (InterruptedException e) {
            Log.d(TAG, "Thread: " + Thread.currentThread().getId() + "  InterruptedException ");
            //throw new RuntimeException(e);
        } catch (TimeoutException e) {
            //identical to 等同于
            Log.d(TAG, "Thread: " + Thread.currentThread().getId() + "  TimeoutException ");
        } catch (ExecutionException e) {
            //identical to 等同于！
            //饭！
            Log.d(TAG, "Thread: " + Thread.currentThread().getId() + "  ExecutionException ");
        }

    }

    public void testLinkedBlockingQueueThreadPoolExecutor() {
        Future<String> a = linkBlockBlockingQueueThreadPoolExecutor.submit(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Thread: " + Thread.currentThread().getId() + "  testLinkedBlockingQueueThreadPoolExecutor:  run");
            }
        }, "testLinkedBlockingQueueThreadPoolExecutor");


        try {
            String a1 = (String) a.get(1, TimeUnit.MINUTES);
            Log.d(TAG, "Thread: " + Thread.currentThread().getId() + "  Future: " + a1);
        } catch (InterruptedException e) {
            Log.d(TAG, "Thread: " + Thread.currentThread().getId() + "  InterruptedException ");
            //throw new RuntimeException(e);
        } catch (TimeoutException e) {
            //identical to 等同于
            Log.d(TAG, "Thread: " + Thread.currentThread().getId() + "  TimeoutException ");
        } catch (ExecutionException e) {
            //identical to 等同于！
            //饭！
            Log.d(TAG, "Thread: " + Thread.currentThread().getId() + "  ExecutionException ");
        }

    }

    public void testsPriorityBlockingQueueThreadPoolExecutor() {
        /*priorityBlockingQueueThreadPoolExecutor.execute(new PriorityRunable(1, new Runnable() {
            @Override
            public void run() {

                Log.d(TAG, "Thread: " + Thread.currentThread().getId() + "  testsPriorityBlockingQueueThreadPoolExecutor:run ");
            }
        }).runnable);*/

        for (int i = 0; i < 100; i++) {
            int finalI = i;
            priorityBlockingQueueThreadPoolExecutor.execute(new PriorityRunnable(i, () -> {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                Log.d(TAG, "Thread: " + Thread.currentThread().getId() + "  testsPriorityBlockingQueueThreadPoolExecutor:run priority:  " + finalI);
            }));
        }

    }

    public static MyThreadPool getInstance() {
        //加快通行效率
        if (myThreadPool == null) {

            synchronized (MyThreadPool.class) {
                //第一个if只是加快速度，不能确定是否初始化
                if (myThreadPool == null) {
                    myThreadPool = new MyThreadPool();
                }
            }

        }
        return myThreadPool;
    }


}
