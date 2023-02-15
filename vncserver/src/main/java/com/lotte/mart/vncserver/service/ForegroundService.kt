package com.lotte.mart.vncserver.service

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
//import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.lotte.mart.commonlib.log.Log
import com.lotte.mart.commonlib.utility.Utility
import com.lotte.mart.vncserver.MainActivity
import com.lotte.mart.vncserver.MessageDialog
import com.lotte.mart.vncserver.R
import com.lotte.mart.vncserver.receiver.AlarmReceiver
import com.lotte.mart.vncserver.utils.AgentIniUtil
import java.lang.Exception
import java.util.*

//포그라운드 서비스
class ForegroundService : Service() {
    private val CHANNEL_ID = "VNC_SERVICE_CHEANNEL"
    var notificationBuilder :NotificationCompat.Builder? = null
    companion object {
        val TAG = ForegroundService::class.java.simpleName
        //메인 서비스
        lateinit var mainService: MainService
        var AlarmSet:Boolean = true
        //상태 변경 브로드캐스트
        val BROADCAST_STATUS_UPDATE = "STATUS_UPDATE"
        //메시지 발생 브로드캐스트
        val BROADCAST_MESSAGE_BOX = "MESSAGE_BOX"

        //서비스 시작
        fun startService(context: Context, message: String) {
            //Log 설정
            val now = Utility.getCurrentDay()
            Log.setDebug(false) //Log.d출력 여부
            Log.setDate(now) //로그 날짜 설정
            Log.setPath("${AgentIniUtil.getInstance(context).getLogPath(AgentIniUtil.Constant.PATH_LOCAL_LOG)}VncLog_${now}.log")
            Log.i(TAG, "startService, $AlarmSet")

            //포그라운드 서비스 시작
            val startIntent = Intent(context, ForegroundService::class.java)
            startIntent.putExtra("inputExtra", message)
            ContextCompat.startForegroundService(context, startIntent)
        }

        //서비스 종료
        fun stopService(context: Context) {
            val stopIntent = Intent(context, ForegroundService::class.java)
            try {
                //포그라운드 서비스 종료
                context.stopService(stopIntent)
                //메인 서비스 종료
                mainService.stopService()
                mainService.join(0)
                Log.i(TAG, "stopService, $AlarmSet")
            }catch (e:Exception){
                Log.e(TAG, "stopService", e)
            }
        }

        //포그라운드 서비스 동작여부
        fun isServiceRunning(context: Context):Boolean {
            var am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            for (rsi in am.getRunningServices(Integer.MAX_VALUE)) {
                if (ForegroundService::class.java.name == rsi.service.className)
                    return true
            }
            return false
        }
    }

    //포그라운드 알림 채널 생성
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(CHANNEL_ID, "Foreground Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT)

            val manager = getSystemService(NotificationManager::class.java)
            manager!!.createNotificationChannel(serviceChannel)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val input = intent?.getStringExtra("inputExtra")
        createNotificationChannel()
        val notificationIntent = Intent(this, MainActivity::class.java)
        notificationIntent.action = Intent.ACTION_MAIN
        notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER)
        notificationIntent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        val pendingIntent = PendingIntent.getActivity(
            this,
            0, notificationIntent, 0
        )

        notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("VNC Service")
            .setContentText(input)
            .setSmallIcon(R.drawable.ic_appicon_others)
            .setContentIntent(pendingIntent)
            .setSilent(true)

        startForeground(1, notificationBuilder!!.build())
        //브로드캐시트 리시버 생성
        registerReceiver(broadCastReceiver, IntentFilter(BROADCAST_STATUS_UPDATE))
        registerReceiver(broadCastReceiver, IntentFilter(BROADCAST_MESSAGE_BOX))
        service().run()
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        stopService(baseContext)
        Log.i(TAG, "onTaskRemoved")
        //브로드캐스트 리시버 해제
        unregisterReceiver(broadCastReceiver)
        //알람 메니저 생성(포그라운드 서비스 종료 시 재시작)
        var calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        calendar.add(Calendar.SECOND, 3) //3초 후
        val intent = Intent(this, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, AlarmReceiver.NOTIFICATION_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val alarmManager = this.getSystemService(ALARM_SERVICE) as AlarmManager
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
    }

    fun service(): Runnable {
        return Runnable {
            //메인 서비스 시작
            mainService = MainService.Builder(this.baseContext)
                .ip("0.0.0.0")
                .port(AgentIniUtil.getInstance(this).getVncPort("31079").toInt())
                .key(AgentIniUtil.getInstance(this).getVncPwd("3333"))
                .build()
        }
    }

    //브로드 캐스트 리시버
    val broadCastReceiver = object : BroadcastReceiver() {
        override fun onReceive(contxt: Context?, intent: Intent?) {
            when (intent?.action) {
                //웹소켓 상태
                BROADCAST_STATUS_UPDATE -> {
                    var status = intent.getBooleanExtra("STATUS", false)
                    Log.i(TAG, "Websocket server status updated, $status")
                    if(status)
                        notificationBuilder!!.setContentText("Server opened")
                    else
                        notificationBuilder!!.setContentText("Server closed")

                    startForeground(1,notificationBuilder!!.build())
                }
                //메세지
                BROADCAST_MESSAGE_BOX -> {
                    val dlg = MessageDialog(MainService._context)
                    dlg.start(intent.getStringExtra("MSG")!!)
                }
            }
        }
    }
}
