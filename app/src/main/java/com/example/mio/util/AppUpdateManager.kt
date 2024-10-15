package com.example.mio.util

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.IntentSender
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.annotation.RequiresApi
import com.example.mio.R
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability

object AppUpdateManager {
    private var appUpdateManager: com.google.android.play.core.appupdate.AppUpdateManager? = null
    private const val APP_UPDATE_REQUEST_CODE = 1001

    // 앱 업데이트 매니저 초기화
    fun init(context: Context) {
        if (appUpdateManager == null) {
            appUpdateManager = AppUpdateManagerFactory.create(context)
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    fun checkUpdate(context: Context, activity: Activity) {
        appUpdateManager?.appUpdateInfo?.addOnSuccessListener { appUpdateInfo ->
            // 업데이트 정보가 있는 경우
            triggerAppUpdate(appUpdateInfo, context, activity)
        }?.addOnFailureListener {
            // 실패 시 로그 또는 처리 추가
            Log.e("AppUpdateManager", "Update check failed", it)
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun triggerAppUpdate(appUpdateInfo: AppUpdateInfo, context: Context, activity: Activity) {
        val currentVersion = context.packageManager.getPackageInfo(context.packageName, 0).longVersionCode
        val minSupportedVersion = 1L // 첫 번째 출시 버전

        if (currentVersion < minSupportedVersion) {
            // 즉시 업데이트 수행
            showImmediateUpdate(appUpdateInfo, activity)
        } else {
            // 유연한 업데이트 수행
            showFlexibleUpdate(appUpdateInfo, context, activity)
        }
    }

    // 즉시 업데이트
    private fun showImmediateUpdate(appUpdateInfo: AppUpdateInfo, activity: Activity) {
        if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE ||
            appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
        ) {
            appUpdateManager?.startUpdateFlow(
                appUpdateInfo,
                activity,
                AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build()
            )
        }
    }

    // 유연한 업데이트
    private fun showFlexibleUpdate(appUpdateInfo: AppUpdateInfo, context: Context, activity: Activity) {
        if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
            // 다운로드 완료된 경우 설치 대화 상자 표시
            showInstallAlert(context) {
                appUpdateManager?.completeUpdate()
            }
        } else if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE ||
            appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
        ) {
            appUpdateManager?.startUpdateFlow(
                appUpdateInfo,
                activity,
                AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build()
            )
        }
    }

    private fun showInstallAlert(context: Context, onOKClick: () -> Unit) {
        val builder = AlertDialog.Builder(context)

        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_install_update, null)
        builder.setView(dialogView)

        val iconImageView = dialogView.findViewById<ImageView>(R.id.iconImageView)
        iconImageView.setImageResource(R.drawable.top_icon_vector)

        val titleTextView = dialogView.findViewById<TextView>(R.id.titleTextView)
        titleTextView.text = "업데이트 설치"

        val messageTextView = dialogView.findViewById<TextView>(R.id.messageTextView)
        messageTextView.text = "새로운 업데이트가 다운로드되었습니다. 지금 설치하시겠습니까?"

        builder.setPositiveButton("설치") { _, _ ->
            onOKClick()
        }

        builder.setNegativeButton("취소") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }
}