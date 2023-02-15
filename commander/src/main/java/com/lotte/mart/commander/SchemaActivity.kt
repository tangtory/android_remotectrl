package com.lotte.mart.commander

import android.app.AlarmManager
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import com.lotte.mart.commander.data.ConstantSchema
import com.lotte.mart.commander.schema.SchemaListener
import com.lotte.mart.commander.schema.Worker
//import com.lotte.mart.messengerlib.messenger.constant.MessengerConstant
//import com.lotte.mart.messengerlib.messenger.service.ServerMessengerService
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * 외부 앱 Intent schema를 통해 값을 전달 받아 처리
 */
class SchemaActivity : AppCompatActivity() {
    /**
     * 시간 설정 이나 명령 수행 처리 핸들러
     */
    var mHandler = object : Handler(Looper.getMainLooper()){
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when(msg.what){
                ConstantSchema.HANDLER_CMD-> {
                    var cmd = msg.data.getString(ConstantSchema.CMD)
                    var host = msg.data.getString(ConstantSchema.HOST)
                    commandExec(cmd!!, host!!)
                }
                ConstantSchema.HANDLER_SETTIME->{
                    var time = msg.data.getString(ConstantSchema.TIME)
                    var host = msg.data.getString(ConstantSchema.HOST)
                    setTimeExec(time!!, host!!)
                }
                ConstantSchema.HANDLER_ERROR->{
                    finish()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_schema)

        //Schema 파싱 및 처리
        Worker.parser(intent, object : SchemaListener {
            override fun onCmd(cmd: String, host: String) {
                val m = mHandler.obtainMessage()
                var bundle = Bundle()
                bundle.putString(ConstantSchema.CMD, cmd)
                bundle.putString(ConstantSchema.HOST, host)

                m.what = ConstantSchema.HANDLER_CMD
                m.obj = "[COMMANDER]명령실행"
                m.data = bundle
                mHandler.sendMessage(m)
            }

            override fun onSetTime(time: String, host: String) {
                //에러
                var bundle = Bundle()
                bundle.putString(ConstantSchema.TIME, time)
                bundle.putString(ConstantSchema.HOST, host)

                val m = mHandler.obtainMessage()
                m.what = ConstantSchema.HANDLER_SETTIME
                m.obj = "[COMMANDER]시간설정"
                m.data = bundle

                mHandler.sendMessage(m)
            }

            override fun onError(type: String) {
                //에러
                val m = mHandler.obtainMessage()
                m.what = ConstantSchema.HANDLER_ERROR
                m.obj = "[COMMANDER]에러"
                mHandler.sendMessage(m)
            }
        })
        finish()
    }

    /**
     * 요청 받은 시스템 명령 수행
     * @param cmd - 명령
     * @param host - 요청 host
     */
    fun commandExec(cmd : String, host:String){
        Thread(Runnable {
            try {
                val runtime = Runtime.getRuntime()
                var process = runtime.exec(arrayOf("sh", "-c", "$cmd\n"))
                process.waitFor()

                var res = process.inputStream.bufferedReader().use { it.readText() }
                Log.d("commander intent", "commandExec result $res")
                process.destroy()
                Worker.cmdResponse(host!!, ConstantSchema.RESULT_SUCCESS, res, cmd, this@SchemaActivity)
            } catch (e:Exception) {
                e.printStackTrace()
                Worker.cmdResponse(host!!, ConstantSchema.RESULT_FAIL, "Command failed, $e", cmd, this@SchemaActivity)
            }
            finish()
        }).start()
    }

    /**
     * 시스템 시간 설정
     * @param time - 설정 시간
     * @param host - 요청 host
     */
    fun setTimeExec(time : String, host:String){
        Thread(Runnable {
            try {
                val formatter = DateTimeFormatter.ofPattern("yyyyMMdd.HHmmss");
                val dt = LocalDateTime.parse(time, formatter)
                val c: Calendar = Calendar.getInstance()
                c.set(dt.year, dt.monthValue-1, dt.dayOfMonth, dt.hour, dt.minute, dt.second)
                Log.d("commander intent", "setTimeExec, $time")
                val am = getSystemService(Context.ALARM_SERVICE) as AlarmManager
                am.setTime(c.timeInMillis)
                Worker.setTimeResponse(host!!, ConstantSchema.RESULT_SUCCESS, this@SchemaActivity)
            } catch (e:Exception) {
                e.printStackTrace()
                Worker.setTimeResponse(host!!, ConstantSchema.RESULT_FAIL, this@SchemaActivity)
            }
            finish()
        }).start()
    }
}