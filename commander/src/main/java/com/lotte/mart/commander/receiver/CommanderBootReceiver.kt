package com.lotte.mart.commander.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.lotte.mart.commander.MainActivity

/**
 * 부팅 시 자동 앱 실행
 */
class CommanderBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if("android.intent.action.BOOT_COMPLETED" == intent.action) {
            var i = Intent(context, MainActivity::class.java)
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(i)
            Toast.makeText(context, "Start Commander", Toast.LENGTH_LONG).show()
        }
    }
}