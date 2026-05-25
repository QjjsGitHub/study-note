package com.example.study;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.WindowMetrics;

import java.lang.ref.WeakReference;
import java.util.List;

public class Tools {

    private static volatile Tools tools;

    public static Tools getInstance() {
        if (tools == null) {
            synchronized (Tools.class) {
                if (tools == null) {
                    tools = new Tools();
                }
            }
        }
        return tools;
    }

    public void getTaskInfo(WeakReference<Activity> weakReferenceActivity) {

        String JAVA_ACTIVITY_TASK = "JAVA_ACTIVITY_TASK";

        if (weakReferenceActivity.get() == null) {
            return;
        }
        ActivityManager activityManager = (ActivityManager) weakReferenceActivity.get().getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.AppTask> appTaskList = activityManager.getAppTasks();

        Log.d(JAVA_ACTIVITY_TASK, "类名" + weakReferenceActivity.get().getClass().getName());

        for (ActivityManager.AppTask appTask : appTaskList) {
            ActivityManager.RecentTaskInfo taskInfo = appTask.getTaskInfo();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                Log.d(JAVA_ACTIVITY_TASK, "栈名称" + taskInfo.taskId);
            }
            Log.d(JAVA_ACTIVITY_TASK, "栈大小" + taskInfo.numActivities);
            if (taskInfo.numActivities >= 1) {
                Log.d(JAVA_ACTIVITY_TASK, "栈底" + taskInfo.baseActivity.getClassName().toString());
                Log.d(JAVA_ACTIVITY_TASK, "栈顶" + taskInfo.topActivity.getClassName().toString());
            }
        }
    }

    private Tools() {
    }

    public int getScreenWidth(Activity context) {

        int width = 1080;

        WindowMetrics windowMetrics;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            windowMetrics = context.getWindowManager().getCurrentWindowMetrics();
            width = windowMetrics.getBounds().width();
        }
        return width;


    }
}
