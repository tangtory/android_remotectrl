package com.lotte.mart.agent

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.lotte.mart.agent.service.ForegroundService
import com.lotte.mart.agent.utils.AgentIniUtil
import com.lotte.mart.agent.utils.Util
import com.lotte.mart.commonlib.utility.PermissionUtil

/**
 * 권한 요청 처리 Activity
 */
class PermissionActivity : AppCompatActivity() {
    val TAG = PermissionActivity::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permission)
        PermissionUtil.requestPermission(
            this@PermissionActivity,
            object : PermissionUtil.PermissionsListener {
                override fun onGranted() {
                    if(!Util.isExistFile(AgentIniUtil.Constant.FILE_SETTING_PATH)){
                        Toast.makeText(this@PermissionActivity, "POS 설정 완료 후 재시작해주세요", Toast.LENGTH_LONG).show()
                        finish()
                    } else {
                        var isRunning = ForegroundService.isServiceRunning(this@PermissionActivity)
                        if (isRunning) {
                            finish()
                        } else {
                            ForegroundService.startService(
                                this@PermissionActivity,
                                "Agent service is running..."
                            )
                            finish()
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

    override fun onDestroy() {
        super.onDestroy()
    }
}