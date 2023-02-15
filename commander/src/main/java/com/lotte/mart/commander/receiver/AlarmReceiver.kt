package com.lotte.mart.commander.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.lotte.mart.commander.service.ForegroundService


/**
 * 설정 시간 앱 실행
 */
class AlarmReceiver : BroadcastReceiver() {

    companion object {
        const val TAG = "AlarmReceiver"
        const val NOTIFICATION_ID = 10099
        const val PRIMARY_CHANNEL_ID = "primary_notification_channel"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Received intent : $intent")
        val isRunning = ForegroundService.isServiceRunning(context)
        if(!isRunning) {
            ForegroundService.startService(context, "Commander service is running...")
        }
    }
}