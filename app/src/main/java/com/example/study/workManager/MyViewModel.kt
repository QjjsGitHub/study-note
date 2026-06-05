package com.example.study.workManager

import android.content.Context

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkContinuation
import androidx.work.WorkInfo
import androidx.work.WorkManager
import java.util.UUID
import java.util.concurrent.TimeUnit

class MyViewModel(context: Context) : ViewModel() {

    private val DATA_TAG = "DATA"
    private val workManager = WorkManager.getInstance(context)
    private var myWorkInfos: LiveData<List<WorkInfo>> =
        workManager.getWorkInfosByTagLiveData(DATA_TAG)


    fun getMyWorkInfos(): LiveData<List<WorkInfo>> {
        return myWorkInfos
    }


    internal fun getData(string: String): Triple<UUID, UUID, UUID> {

        val workRequestBuilder1 = OneTimeWorkRequestBuilder<MyWorker>()
        workRequestBuilder1.setInputData(Data.Builder().putString("string", string).build())
            .addTag(DATA_TAG)

            //添加约束条件
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .setRequiresCharging(false)
                    .build()
            )
            //设置延迟执行
            //.setInitialDelay(1000, TimeUnit.MILLISECONDS)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,  // 退避策略：指数或线性
                10, TimeUnit.SECONDS         // 初始延迟时间
            )
            //这是 WorkManager 2.7 引入的最接近“高优先级”的机制。
            // 它告诉系统：这个任务非常重要，应该尽快执行，不受系统能耗优化（如 App Standby Buckets）的严格限制。
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)

        val w1 = workRequestBuilder1.build()

        val w2 = OneTimeWorkRequestBuilder<MySecondWorker>().addTag(DATA_TAG).build()

        val workRequestBuilder3 = PeriodicWorkRequestBuilder<MyWorker>(
            1,
            TimeUnit.MINUTES
        )

        workRequestBuilder3.setInputData(Data.Builder().putString("string", string).build())
            .addTag(DATA_TAG)

        val w3 = workRequestBuilder3.build()

        var workContinuation: WorkContinuation = workManager.beginUniqueWork(
            "unique_upload_name",         // 任务的唯一标识名称
            ExistingWorkPolicy.REPLACE, w1
        )


        /**

        ‌单个任务（OneTimeWorkRequest）‌：通过 setInputData() 显式传入的 Data 仅属于该任务实例，其他任务无法访问。
        ‌任务链（如 beginWith(A).then(B).then(C)）‌：‌A 的 outputData 会自动成为 B 的 inputData‌，B 的 output
        成为 C 的 input，但 A 和 C 不直接共享数据；若并行执行多个任务（如 beginWith(A, B).then(C)），则 C 的 inputData 是 A 和 B 的 output 合并结果（需配置 InputMerger 处理键冲突）。
        ‌独立入队的任务‌：彼此完全隔离，即使使用相同 Worker 类，也需各自调用 setInputData()，不会自动共享或复用。
        ‌PeriodicWorkRequest 不支持 output → 下一次 input‌：每次周期执行都是独立的，无上一次的 output 传递。‌‌

         * */

        workContinuation =
            workContinuation.then(w2)
        workContinuation.enqueue()

        workManager.enqueue(w3)


        return Triple(w1.id, w2.id, w3.id)
    }

    fun cancelWork() {
        workManager.cancelAllWorkByTag(DATA_TAG)
    }


}

class MyViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        return MyViewModel(context) as T

    }

}