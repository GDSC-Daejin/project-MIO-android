package com.example.mio.Helper

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

// WorkManager를 사용하여 주기적 작업을 예약
fun scheduleMyWorker(context: Context) {
    val constraints = Constraints.Builder()
        .setRequiresCharging(false) // 충전 상태가 아닐 때도 작동한다.
        .setRequiresBatteryNotLow(false) // 배터리 부족상태가 아닐 때도 작동한다
        .build()

    val workRequest = PeriodicWorkRequestBuilder<MyWorker>(
        repeatInterval = 5, // 주기 (분)
        repeatIntervalTimeUnit = TimeUnit.MINUTES,
    ).setConstraints(constraints).build()

    //val workManager = WorkManager.getInstance(context)
    /*workManager?.let {
        it.enqueue(workRequest)

        *//** WorkManager의 getStatusById()에 WorkRequest의 UUID 객체를 인자로 전달 하면
         *  인자값으로 주어진 ID에 해당하는 작업을 추적할 수 있도록 LiveData 객체를 반환한다
         *//*
        val statusLiveData = it.getStatusById(workRequest.id)
        *//* statusLiveData에 Observer를 걸어서 작업의 상태를 추적 *//*
        statusLiveData.observe(this, Observer { workState ->
            Log.d("exmaple", "state: ${workState?.state}")
        })
    }*/
    WorkManager.getInstance(context).enqueue(workRequest)
}