package com.lotte.mart.agent.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.*
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.lotte.mart.agent.MainActivity
import com.lotte.mart.agent.R
import com.lotte.mart.agent.receiver.AlarmReceiver
import com.lotte.mart.agent.utils.AgentIniUtil
import com.lotte.mart.agent.utils.Util
import com.lotte.mart.commonlib.log.Log
import com.lotte.mart.commonlib.utility.Utility
import java.util.*

/**
 * 포그라운드 서비스
 */
class ForegroundService : Service() {
    private val CHANNEL_ID = "LOTTEDS_AGENT_SERVICE"
    companion object {
        var updateService: UpdateService? = null
        var emsService: EmsService? = null
        var watcherService: WatchService? = null
        val TAG = ForegroundService::class.java.simpleName
        var AlarmSet:Boolean = true

        /**
         * 포그라운드 서비스 시작
         * @param context - context
         * @param message - 서비스 메시지
         */
        fun startService(context: Context, message: String) {
            //Ini set
            AgentIniUtil.getInstance().initIni()
            Util.initIni(context)

            //Log set
            val now = Utility.getCurrentDay()
            Log.setDebug(false)
            Log.setDate(now)
            Log.setPath("${AgentIniUtil.getInstance().getLogPath(AgentIniUtil.Constant.PATH_LOCAL_LOG)}AgentLog_${now}.log")

            Log.i(TAG, "startService, $AlarmSet")
            val startIntent = Intent(context, ForegroundService::class.java)
            startIntent.putExtra("inputExtra", message)
            ContextCompat.startForegroundService(context, startIntent)
        }

        /**
         * 포그라운드 서비스 종료
         * @param context - context
         */
        fun stopService(context: Context) {
            Log.i(TAG, "stopService, $AlarmSet")
            if(updateService != null) {
                updateService!!.stopUpdateService()
                updateService = null
            }

            if(emsService != null){
                emsService!!.stopService()
                emsService = null
            }

            if(watcherService != null){
                watcherService!!.stopService()
                watcherService = null
            }

            val stopIntent = Intent(context, ForegroundService::class.java)
            context.stopService(stopIntent)
        }

        /**
         * 포그라운드 서비스 가동여부
         * @param context - context
         */
        fun isServiceRunning(context: Context):Boolean {
            var am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            for (rsi in am.getRunningServices(Integer.MAX_VALUE)) {
                if (ForegroundService::class.java.name == rsi.service.className)
                    return true
            }
            return false
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(CHANNEL_ID, "Foreground Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT)

            val manager = getSystemService(NotificationManager::class.java)
            manager!!.createNotificationChannel(serviceChannel)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        //do heavy work on a background thread
        val input = intent?.getStringExtra("inputExtra")
        createNotificationChannel()
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0, notificationIntent, 0
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Agent service")
            .setContentText(input)
            .setSmallIcon(R.drawable.ic_appicon_others)
            .setContentIntent(pendingIntent)
            .setSilent(true)
            .build()

        startForeground(1, notification)
        service().run()
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        stopService(baseContext)
        Log.i(TAG, "onTaskRemoved, $AlarmSet")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "onDestroy, $AlarmSet")
        //재시작 알람 설정 시 3초 후 앱 실행
        if(AlarmSet) {
            var calendar = Calendar.getInstance()
            calendar.timeInMillis = System.currentTimeMillis()
            calendar.add(Calendar.SECOND, 3) //3초 후
            val intent = Intent(this, AlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                this,
                AlarmReceiver.NOTIFICATION_ID,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
            val alarmManager = this.getSystemService(ALARM_SERVICE) as AlarmManager
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        }
    }

    override fun onCreate() {
        super.onCreate()
    }

    fun service(): Runnable {
        return Runnable {
            Log.i(TAG, "Run forground service")
            //업데이트 서비스 시작
            updateService = UpdateService(this@ForegroundService)
            updateService!!.startUpdateService()
            
            //EMS로그 전송 서비스 시작
            emsService = EmsService(this.applicationContext, AgentIniUtil.getInstance().getServerPort(
                "31070"
            ).toInt())
            emsService!!.startService()

            //감시 서비스 시작
            watcherService = WatchService(this)
            watcherService!!.startService()
        }
    }
}
