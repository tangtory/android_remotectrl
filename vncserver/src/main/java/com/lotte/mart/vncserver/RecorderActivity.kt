package com.lotte.mart.vncserver

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.lotte.mart.commonlib.log.Log
import com.lotte.mart.commonlib.utility.PermissionUtil
import com.lotte.mart.vncserver.service.ForegroundService
import com.lotte.mart.vncserver.service.MainService
import com.lotte.mart.vncserver.utils.AgentIniUtil
import kotlin.system.exitProcess

//화면 녹화 Activity(캡쳐링)
class RecorderActivity : AppCompatActivity() {
    val TAG = RecorderActivity::class.java.simpleName
    companion object{
        val RECORD_REQUEST_CODE = 101
        var projectionManager: MediaProjectionManager? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recorder)

        if(MainService.isRecording()) {
            //이미 화면 캡쳐 중이면 종료
            finish()
            return
        }

        projectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

        //권한 요청 (파일 읽기/쓰기)
         PermissionUtil.requestPermission(
            this@RecorderActivity,
            object : PermissionUtil.PermissionsListener {
                override fun onGranted() {
                    Log.i(TAG, "Permission granted")
                    val captureIntent: Intent = projectionManager!!.createScreenCaptureIntent()
                    startActivityForResult(captureIntent, RECORD_REQUEST_CODE)

                    //AgentIni 검사
                    if(!AgentIniUtil.getInstance(this@RecorderActivity).isExistJsonCache() ||!AgentIniUtil.getInstance(
                            this@RecorderActivity
                        ).hasData()){
                        Toast.makeText(this@RecorderActivity, "Agent 서비스 실행 후 재시작해주세요", Toast.LENGTH_LONG).show()
                        ForegroundService.AlarmSet = false
                        ActivityCompat.finishAffinity(this@RecorderActivity)
                        System.runFinalization()
                        exitProcess(0)
                    } else {
                        val isRunning = ForegroundService.isServiceRunning(this@RecorderActivity)
                        Log.i(TAG, "ForegroundService running check, $isRunning")
                        if(isRunning) {
                        } else {
                            ForegroundService.startService(this@RecorderActivity, "Vnc service is running...")
                        }
                    }
                }

                override fun onDenied() {
                    finish()
                }
            },
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RECORD_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            //화면 캡쳐링 요청시 화면 캡쳐 서비스 시작
            var mediaProjection: MediaProjection = projectionManager!!.getMediaProjection(resultCode, data!!)
            MainService.startRecorder(mediaProjection, this.getSystemService(Context.WINDOW_SERVICE) as WindowManager)
            Handler().postDelayed({
                finish()
            }, 1000L)
        } else if (requestCode == RECORD_REQUEST_CODE && resultCode == Activity.RESULT_CANCELED) {
            finish()
        }
    }
}
