package com.lotte.mart.vncserver

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.lotte.mart.commonlib.log.Log
import com.lotte.mart.vncserver.service.ForegroundService
import com.lotte.mart.vncserver.service.schema.SchemaListener
import com.lotte.mart.vncserver.service.schema.Worker
import com.lotte.mart.vncserver.utils.AgentIniUtil
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {
    val TAG = MainActivity::class.java.simpleName
    companion object{
        // uncaught exception handler variable
        var defaultUEH : Thread.UncaughtExceptionHandler? =null
        var mHandler = Handler()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //퍼미션 체크(파일 읽기 쓰기)
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
            || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            //퍼미션 미 체크 상태일 경우 레코딩 체크를 위해 RecorderActivity에서 퍼미션 처리 진행
            var i = Intent(this, RecorderActivity::class.java)
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(i)
            finish()
        } else {
            //현재 포그라운드 서비스 동작 중인지 확인
            var isRunning = ForegroundService.isServiceRunning(this@MainActivity)
            if(isRunning) {
                Log.i(TAG, "ForegroundService running check, $isRunning")
                finish()
            } else {
                //AgentIni파일 체크
                //Agent에서 ini파일을 앱 실행 시 다운받는 시간과 동시에 실행하면 ini파일을 확인 할 수 없음
                //확인 실패 시 Agent에서 VNC를 자동 재시작 하여 다시 검사함
                if(!AgentIniUtil.getInstance(this@MainActivity).isExistJsonCache() ||!AgentIniUtil.getInstance(
                        this@MainActivity
                    ).hasData()) {
                    Toast.makeText(this@MainActivity, "Agent 서비스 실행 후 재시작해주세요", Toast.LENGTH_LONG).show()
                    ForegroundService.AlarmSet = false
                    //체크 실패시 완전히 종료 되어야 정확한 체크 가능.
                    ActivityCompat.finishAffinity(this@MainActivity)
                    System.runFinalization()
                    exitProcess(0)
                } else {
                    //포그라운드 서비스 실행
                    ForegroundService.startService(this@MainActivity, "Vnc service is running...")
                    defaultUEH = Thread.getDefaultUncaughtExceptionHandler()
                    Thread.setDefaultUncaughtExceptionHandler(ExceptionCatcher())
                    finish()
                }
            }
        }

        //인텐트 통신(스키마) 파서 작업
        var schema = Worker.parser(intent, object : SchemaListener {
            override fun onAppFinish() {
                var isRunning = ForegroundService.isServiceRunning(this@MainActivity)
                if(isRunning) {
                    ForegroundService.stopService(this@MainActivity)
                }
                ForegroundService.AlarmSet = false
                ActivityCompat.finishAffinity(this@MainActivity)
                System.runFinalization()
                exitProcess(0)
            }

            override fun onAppUpdateFinish(result: Boolean) {
                var isRunning = ForegroundService.isServiceRunning(this@MainActivity)
                Log.i(TAG, "ForegroundService running check, $isRunning")
                if(isRunning) {
                    finish()
                } else {
                    if(!AgentIniUtil.getInstance(this@MainActivity).isExistJsonCache() ||!AgentIniUtil.getInstance(
                            this@MainActivity
                        ).hasData()){
                        Log.i(TAG, "AgentIni is not set")
                        Toast.makeText(this@MainActivity, "Agent 서비스 실행 후 재시작해주세요", Toast.LENGTH_LONG).show()
                        ForegroundService.AlarmSet = false
                        ActivityCompat.finishAffinity(this@MainActivity)
                        System.runFinalization()
                        exitProcess(0)
                    } else {
                        ForegroundService.startService(this@MainActivity, "Vnc service is running...")
                        defaultUEH = Thread.getDefaultUncaughtExceptionHandler()

                        Thread.setDefaultUncaughtExceptionHandler(ExceptionCatcher())
                        finish()
                    }
                }
            }

            override fun onError(type: String) {
                val m = mHandler.obtainMessage()
                m.what = 1
                m.obj = "[AGENT]에러"
                mHandler.sendMessage(m)
            }

        }, this@MainActivity)

        //스키마로 호출된 인텐트 생성시 리턴
        if(schema)
            return


    }

    //Exception캐치
    inner class ExceptionCatcher : Thread.UncaughtExceptionHandler {
        override fun uncaughtException(t: Thread, e: Throwable) {
            Log.e(TAG, "uncaughtException, $t", e)
            finish()
        }
    }
}
