package com.example.mio.helper

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

fun scheduleMyWorker(context: Context) {
    val constraints = Constraints.Builder()
        .setRequiresCharging(false)
        .setRequiresBatteryNotLow(false)
        .build()

    val workRequest = PeriodicWorkRequestBuilder<MyWorker>(
        repeatInterval = 15, // 최소 주기는 15분
        repeatIntervalTimeUnit = TimeUnit.MINUTES,
    ).setConstraints(constraints).build()

    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        "MyWorker",
        ExistingPeriodicWorkPolicy.REPLACE,
        workRequest
    )
}