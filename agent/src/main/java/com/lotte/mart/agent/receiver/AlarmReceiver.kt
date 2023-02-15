package com.lotte.mart.agent.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.lotte.mart.agent.service.ForegroundService
import com.lotte.mart.agent.utils.AgentIniUtil
import com.lotte.mart.agent.utils.Util

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        const val TAG = "AlarmReceiver"
        const val NOTIFICATION_ID = 199
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Received intent : $intent")
        if(!Util.isExistFile(AgentIniUtil.Constant.FILE_SETTING_PATH)){
            Toast.makeText(context, "POS 설정 완료 후 재시작해주세요", Toast.LENGTH_LONG).show()
        } else {
            AgentIniUtil.getInstance().initIni()
            Util.initIni(context)
            var isRunning = ForegroundService.isServiceRunning(context)
            if (!isRunning) {
                ForegroundService.startService(context, "Agent Service is running...")
            }
        }
    }
}