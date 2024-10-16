package com.example.mio.util

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
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
    //private const val APP_UPDATE_REQUEST_CODE = 1001

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
        // 현재 설치된 앱의 버전 코드 가져오기
        val currentVersion = context.packageManager.getPackageInfo(context.packageName, 0).longVersionCode

        // 마켓에서 가져온 최신 버전 정보
        val availableVersionCode = appUpdateInfo.availableVersionCode() // appUpdateInfo에서 최신 버전 코드 가져옴

        if (currentVersion < availableVersionCode) {
            // 만약 현재 버전이 마켓의 최신 버전보다 낮다면 업데이트를 유도
            if (appUpdateInfo.updatePriority() >= 5) {
                // 우선순위가 높다면 즉시 업데이트
                showImmediateUpdate(appUpdateInfo, activity)
            } else {
                // 그렇지 않다면 유연한 업데이트
                showFlexibleUpdate(appUpdateInfo, context, activity)
            }
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