package com.lotte.mart.commander

import android.Manifest
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.lotte.mart.commander.service.ForegroundService
import com.lotte.mart.commonlib.utility.PermissionUtil

/**
 * Commander 서비스
 * 외부 앱에서 IPC(MessengerService)또는 Intent schema를 통해
 * 전달 받은 시스템 권한 동작을 수행함
 */
class MainActivity : AppCompatActivity() {

    companion object{

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //화면 제거
//        setContentView(R.layout.activity_main)
        var isRunning = ForegroundService.isServiceRunning(this)
        if(isRunning) {
            finish()
        } else {
            PermissionUtil.requestPermission(
                this@MainActivity,
                object : PermissionUtil.PermissionsListener {
                    override fun onGranted() {

                    }

                    override fun onDenied() {

                    }
                },
//                        Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            //서비스 시작
            ForegroundService.startService(this@MainActivity, "Commander service is running...")
            finish()
        }
    }
}
