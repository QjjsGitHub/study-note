package com.example.study.workManager

import android.content.Context
import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OutOfQuotaPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit


const val MyWorkManagerMYTAG: String = "MyWorkManagerLOG"

class MyWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    override fun doWork(): Result {
        // 执行实际任务（在后台线程运行）
        try {
            // 模拟耗时操作
            val result = handleData(inputData.getString("string"))

            Log.d(TAG, "First HandleData: $result")
            //任务取消
            if (isStopped) {
                Result.failure()
            }

            return Result.success(Data.Builder().putString("string", result).build()) // 任务成功
        } catch (e: Exception) {
            Log.e(MyWorkManagerMYTAG, "任务失败", e);
            return Result.failure(Data.Builder().putString("string", "fail").build()) // 任务失败
        }
        // 注意：不推荐使用 Result.retry()，因为会一直重试
    }


    fun request() {


        // 一次性任务
        var uploadWorkRequest: OneTimeWorkRequest =
            OneTimeWorkRequest.Builder(MyWorker::class.java)
                .build()

        // 周期性任务（最小间隔15分钟）
        var periodicWorkRequest: PeriodicWorkRequest =
            PeriodicWorkRequest.Builder(
                MyWorker::class.java, 15,
                TimeUnit.MINUTES
            )
                .build()

        var delayedWorkRequest: OneTimeWorkRequest =
            OneTimeWorkRequest.Builder(MyWorker::class.java)
                .setInitialDelay(5, TimeUnit.MINUTES) // 延迟5分钟执行
                .build()

        var workRequest: OneTimeWorkRequest = OneTimeWorkRequest.Builder(MyWorker::class.java)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,  // 退避策略：指数或线性
                10, TimeUnit.SECONDS // 初始延迟时间
            )
            .build()


        // 构建请求
        var statsRequest: WorkRequest =
            OneTimeWorkRequest.Builder(workerClass = MyWorker::class.java)
                // 1. 设置初始延迟：注册成功 5 分钟后才真正开始跑
                .setInitialDelay(5, TimeUnit.MINUTES)

                // 2. 打上标签：方便后续管理
                .addTag("user_onboarding_tasks")
                .addTag("priority_low")
                .build()

        // 提交任务
        WorkManager.getInstance(applicationContext).enqueue(statsRequest);

        // --- 稍后在其他地方（如退出登录时）---
        // 3. 通过标签一次性取消所有相关任务
        WorkManager.getInstance(applicationContext).cancelAllWorkByTag("user_onboarding_tasks");

        val constraints: Constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED) // 需要网络连接
            .setRequiresBatteryNotLow(true) // 电量不低
            .setRequiresCharging(false) // 不需要充电
            .setRequiresDeviceIdle(false) // 设备不需要空闲
            .setRequiresStorageNotLow(true) // 存储空间充足
            .build()

        val uploadWorkRequest1: OneTimeWorkRequest =
            OneTimeWorkRequest.Builder(MyWorker::class.java)
                .setConstraints(constraints)
                .build()

        val inputData: Data = Data.Builder()
            .putString("user_id", "12345")
            .putInt("file_count", 10)
            .putStringArray("file_paths", arrayOf<String>("/path/1", "/path/2") as Array<String?>)
            .build()

        val workRequest1: OneTimeWorkRequest =
            OneTimeWorkRequest.Builder(MyWorker::class.java)
                .setInputData(inputData)
                .build()

        val highPriorityWork: OneTimeWorkRequest =
            OneTimeWorkRequest.Builder(MyWorker::class.java)
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .build()

        val workManager = WorkManager.getInstance(applicationContext)
        workManager.enqueue(uploadWorkRequest)

        workManager.enqueueUniqueWork(
            "unique_upload_name",  // 任务的唯一标识名称
            ExistingWorkPolicy.REPLACE,  // 如果任务已存在，怎么处理？（REPLACE, KEEP, APPEND）
            (highPriorityWork as OneTimeWorkRequest?)!!
        )
    }

    fun handleData(string: String?): String {

        Log.d(MyWorkManagerMYTAG, "开始处理数据...");
        try {
            Thread.sleep(2000);
        } catch (e: InterruptedException) {
            e.printStackTrace();
        }

        val s = string ?: "null"


        Log.d(MyWorkManagerMYTAG, "处理完成")
        return "$s: One Handle"
    }


}