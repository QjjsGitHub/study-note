package com.example.study.workManager

import android.content.Context
import android.util.Log
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters

class MySecondWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {


    override fun doWork(): Result {

        val d = inputData.getString("string") + " : Second Handle"
        val data =
            Data.Builder().putString("string", d)
                .build()

        Log.d(TAG, "Second HandleData: $d")

        return Result.success(data)

    }


}