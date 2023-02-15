package com.lotte.mart.commander.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.lotte.mart.commander.MainActivity
import com.lotte.mart.commander.R
import com.lotte.mart.commander.data.ConstantSchema
import com.lotte.mart.commander.receiver.AlarmReceiver
import com.lotte.mart.messengerlib.messenger.callback.RequestCallback
import com.lotte.mart.messengerlib.messenger.constant.MessengerConstant
import com.lotte.mart.messengerlib.messenger.service.ServerMessengerService
//import com.lotte.mart.messengerlib.messenger.callback.RequestCallback
//import com.lotte.mart.messengerlib.messenger.constant.MessengerConstant
//import com.lotte.mart.messengerlib.messenger.service.ServerMessengerService
import java.util.*

class ForegroundService : Service() {
    private val CHANNEL_ID = "LOTTEDS_COMMANDER_SERVICE"
    /**
     * 시간 설정 이나 명령 수행 처리 핸들러
     */
    var mHandler = object : Handler(Looper.getMainLooper()){
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when(msg.what){
                ConstantSchema.HANDLER_CMD-> {
                    var cmd = msg.data.getString(MessengerConstant.DATA_CMD)
                    commandExec(cmd!!, msg.obj as Message)
                }
                ConstantSchema.HANDLER_SETTIME->{
                    var cal = msg.data.getLong(MessengerConstant.DATA_CALENDAR)
                    setSystemTime(cal!!, msg.obj as Message)
                }
            }
        }
    }
    companion object {
        fun startService(context: Context, message: String) {
            val startIntent = Intent(context, ForegroundService::class.java)
            startIntent.putExtra("inputExtra", message)
            ContextCompat.startForegroundService(context, startIntent)
        }

        fun stopService(context: Context) {
            val stopIntent = Intent(context, ForegroundService::class.java)
            context.stopService(stopIntent)
        }

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
            .setContentTitle("Commander service")
            .setContentText(input)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
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
        Log.d("ForegroundService", "onTaskRemoved")

        //서비스 종료시 알람 서비스에 등록(서비스 자동 재시작)
        var calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        calendar.add(Calendar.SECOND, 3) //3초 후
        val intent = Intent(this, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, AlarmReceiver.NOTIFICATION_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val alarmManager = this.getSystemService(ALARM_SERVICE) as AlarmManager
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
    }

    override fun onCreate() {
        super.onCreate()
    }

    /**
     * MessengerService 서버 시작
     */
    private fun service(): Runnable {
        return Runnable {
            ServerMessengerService.with()?.setCallback(object : RequestCallback {
                override fun powerOff(msg: String, result: Boolean, message: Message) {
                    val m = mHandler.obtainMessage()
                    m.what = 19
                    m.obj = "Received request, $msg"
                    mHandler.sendMessage(m)
                    ServerMessengerService.replySuccess(message.replyTo, message.data)
                }

                override fun reboot(msg: String, result: Boolean){
                    val m = mHandler.obtainMessage()
                    m.what = ConstantSchema.HANDLER_CMD
                    m.obj = "Received request, $msg"
                    mHandler.sendMessage(m)
                }

                override fun appUpdate(msg: String, result: Boolean){
                    val m = mHandler.obtainMessage()
                    m.what = ConstantSchema.HANDLER_CMD
                    m.obj = "Received request, $msg"
                    mHandler.sendMessage(m)
                }

                override fun setIpAddressBounds(msg: String, result: Boolean){
                    val m = mHandler.obtainMessage()
                    m.what = ConstantSchema.HANDLER_CMD
                    m.obj = "Received request, $msg"
                    mHandler.sendMessage(m)
                }

                override fun cmdExec(msg: String, result: Boolean, message: Message) {
                    val m = mHandler.obtainMessage()

                    //Message copy
                    var ms = Message()
                    ms.copyFrom(message)

                    //data copy
                    var bundle = Bundle()
                    bundle.putString(MessengerConstant.DATA_CMD, msg)

                    m.what = ConstantSchema.HANDLER_CMD
                    m.obj = ms
                    m.data = bundle
                    mHandler.sendMessage(m)
                }

                override fun setTime(h: Int, m: Int, s: Int, result: Boolean, message: Message) {
                    Log.d("commander", "setDateTime $h, $m, $s")
                    val c: Calendar = Calendar.getInstance()
                    c.set(Calendar.HOUR_OF_DAY, h)
                    c.set(Calendar.MINUTE, m)
                    c.set(Calendar.SECOND, s)
                    val am = getSystemService(Context.ALARM_SERVICE) as AlarmManager
                    am.setTime(c.timeInMillis)
                }

                override fun setDateTime(Y: Int, M: Int, D: Int, h: Int, m: Int, s: Int, result: Boolean, message: Message) {
                    Log.d("commander", "setDateTime $Y, $M, $D, $h, $m, $s")
                    try {
                        val m1 = mHandler.obtainMessage()

                        //Message copy
                        var ms = Message()
                        ms.copyFrom(message)

                        //data copy
                        val c: Calendar = Calendar.getInstance()
                        c.set(Y, M, D, h, m, s)
//                        val am = getSystemService(Context.ALARM_SERVICE) as AlarmManager
//                        am.setTime(c.timeInMillis)
                        var bundle = Bundle()
                        bundle.putLong(MessengerConstant.DATA_CALENDAR, c.timeInMillis)

                        m1.what = ConstantSchema.HANDLER_SETTIME
                        m1.obj = ms
                        m1.data = bundle
                        mHandler.sendMessage(m1)
                    } catch (e:Exception){
                        var bundle = Bundle()
                        bundle.putString(MessengerConstant.DATA_RESULT, "set time failed")
                        ServerMessengerService.replyFailed(message.replyTo, bundle)
                    }
                }
            })
        }
    }

    /**
     * 요청 받은 시스템 명령 수행
     * @param cmd - 명령
     * @param message - 요청 MessengerClient 메세지
     */
    fun commandExec(cmd : String, message:Message){
        Thread(Runnable {
            try {
                val runtime = Runtime.getRuntime()
                var process = runtime.exec(arrayOf("sh", "-c", "$cmd\n"))
                process.waitFor()

                var res = process.inputStream.bufferedReader().use { it.readText() }
                Log.d("commander", "commandExec result $res")
                process.destroy()

                var bundle = Bundle()
                bundle.putString(MessengerConstant.DATA_RESULT, res)
//                bundle.putString(MessengerConstant.DATA_RESULT, cmd)
                ServerMessengerService.replySuccess(message.replyTo, bundle)
            } catch (e:Exception) {
                e.printStackTrace()
                var bundle = Bundle()
                bundle.putString(MessengerConstant.DATA_RESULT, e.toString())
                ServerMessengerService.replyFailed(message.replyTo, bundle)
            }
        }).start()
    }

    /**
     * 시스템 시간 설정
     * @param cal - 시간 값
     * @param message - 요청 MessengerClient 메세지
     */
    fun setSystemTime(cal : Long, message:Message){
        Thread(Runnable {
            try {
                Log.d("commander", "setSystemTime, $cal")
                val am = getSystemService(Context.ALARM_SERVICE) as AlarmManager
                am.setTime(cal)
                var bundle = Bundle()
                bundle.putString(MessengerConstant.DATA_RESULT, "set time")
                ServerMessengerService.replySuccess(message.replyTo, bundle)
            } catch (e:Exception) {
                e.printStackTrace()
                var bundle = Bundle()
                bundle.putString(MessengerConstant.DATA_RESULT, e.toString())
                ServerMessengerService.replyFailed(message.replyTo, bundle)
            }
        }).start()
    }
}
