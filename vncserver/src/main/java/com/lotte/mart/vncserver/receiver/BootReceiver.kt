package com.lotte.mart.vncserver.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.lotte.mart.vncserver.service.ForegroundService
import com.lotte.mart.vncserver.utils.AgentIniUtil
import kotlin.system.exitProcess

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if("android.intent.action.BOOT_COMPLETED" == intent.action) {
            if(!AgentIniUtil.getInstance(context).isExistJsonCache() ||!AgentIniUtil.getInstance(
                    context
                ).hasData()){
                Toast.makeText(context, "Agent 서비스 실행 후 재시작해주세요", Toast.LENGTH_LONG).show()
                ForegroundService.AlarmSet = false
                System.runFinalization()
                exitProcess(0)
            } else {
                val isRunning = ForegroundService.isServiceRunning(context)
                if(!isRunning) {
                    ForegroundService.startService(context, "Vnc service is running...")
                }
            }
        }
    }
}