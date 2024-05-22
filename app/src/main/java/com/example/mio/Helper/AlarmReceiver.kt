package com.example.mio.Helper


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.Configuration
import androidx.work.WorkManager

class AlarmReceiver : BroadcastReceiver() {
    object WorkManagerInitializer {
        private var initialized = false

        fun isInitialized(): Boolean {
            return initialized
        }

        fun setInitialized(value: Boolean) {
            initialized = value
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent != null && intent.action == Intent.ACTION_BOOT_COMPLETED) {
            if (context != null) {
                initializeWorkManager(context)
                scheduleMyWorker(context)
            }
        }
    }

    private fun initializeWorkManager(context: Context) {
        if (!WorkManagerInitializer.isInitialized()) {
            WorkManager.initialize(context, Configuration.Builder().build())
            WorkManagerInitializer.setInitialized(true)
        } else {
            Log.d("AlarmReceiver", "WorkManager is already initialized")
        }
    }
}
