package com.lotte.mart.agent

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.*
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import com.lotte.mart.agent.data.Constant
import com.lotte.mart.agent.service.ForegroundService
import com.lotte.mart.agent.service.UpdateService
import com.lotte.mart.agent.service.schema.Worker
import com.lotte.mart.agent.utils.Util
import com.lotte.mart.commonlib.log.Log

import kotlinx.android.synthetic.main.activity_update.*


/**
 * Agent 업데이트 Activity
 */
class UpdateActivity : AppCompatActivity() {
    companion object {
        //업데이트 상태 변화 브로드캐스트
        val BROADCAST_STATUS_UPDATE = "STATUS_UPDATE"
        //업데이트 종료 브로드캐스트
        val BROADCAST_FINISHED_UPDATE = "FINISHED_UPDATE"
        //업데이트 완료 브로드캐스트
        val BROADCAST_ERROR_UPDATE = "ERROR_UPDATE"

        //Active 상태
        private var active = false

        fun isActive(): Boolean {
            return active
        }
    }

    val TAG = UpdateActivity::class.java.simpleName
    var mHandler = object : Handler(Looper.getMainLooper()){
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when(msg.what){
                1-> Toast.makeText(this@UpdateActivity, ""+msg.obj as String, Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * 업데이트 화면처리를 위한 리시버
     */
    val broadCastReceiver = object : BroadcastReceiver() {
        override fun onReceive(contxt: Context?, intent: Intent?) {
            when (intent?.action) {
                BROADCAST_STATUS_UPDATE -> {
                    var msg = intent.getStringExtra("MSG")
                    if(!msg.isNullOrEmpty()){
                        tvUpdate.text = msg
                    }
                    Log.i(TAG, "BROADCAST_STATUS_UPDATE, $msg")
                }
                BROADCAST_FINISHED_UPDATE -> {
                    val res = intent.getStringExtra("RES")
                    val type = intent.getStringExtra("TYPE")

                    when(type){
                        "VNC" -> {
                            if(res == Constant.RESULT_SUCCESS) {
                                tvUpdate.text =
                                    "[${Util.getCurrentVncVersion()}]\nVNC 업데이트 완료"
                                Worker.vncUpdateFinish(Constant.RESULT_SUCCESS, this@UpdateActivity)
                            }
                            else {
                                tvUpdate.text =
                                    "[${Util.getCurrentVncVersion()}]\nVNC 업데이트 실패"
                                    Worker.vncUpdateFinish(Constant.RESULT_FAIL, this@UpdateActivity)
                            }
                            UpdateService.setUpdateFinish()
                            Handler().postDelayed({
                                if(Util.getPosUpdate().isNullOrEmpty() && !UpdateService.isUpdating()) {
                                    finish()
                                }
                            }, 2000L)
                        }
                        "POS" -> {
                            if(res == Constant.RESULT_SUCCESS){
                                tvUpdate.text = "POS 업데이트 완료"
                                Worker.appUpdateFinish(Constant.RESULT_SUCCESS, this@UpdateActivity)
                                UpdateService.setUpdateFinish()
                                Handler().postDelayed({
                                    if(!UpdateService.isUpdating()) {
                                        finish()
                                    }
                                }, 500)
                            }
                            else {
                                tvUpdate.text = "POS 업데이트 실패"
                                Worker.appUpdateFinish(Constant.RESULT_FAIL, this@UpdateActivity)
                                UpdateService.setUpdateFinish()
                                Handler().postDelayed({
                                    if(!UpdateService.isUpdating()) {
                                        finish()
                                    }
                                }, 500)
                            }
                        }
                        "COMMANDER" -> {
                            if(res == Constant.RESULT_SUCCESS) {
                                tvUpdate.text =
                                    "[${Util.getCurrentCmdVersion()}]\nCOMMANDER 업데이트 완료"
                            }
                            else {
                                tvUpdate.text =
                                    "[${Util.getCurrentCmdVersion()}]\nCOMMANDER 업데이트 실패"
                            }

                            UpdateService.setUpdateFinishCommander()
                            UpdateService.setUpdateFinish()
                            Handler().postDelayed({
                                if(Util.getPosUpdate().isNullOrEmpty() && !UpdateService.isUpdating()) {
                                    finish()
                                }
                            }, 2000L)
                        }
                    }

                    Log.i(TAG, "BROADCAST_FINISHED_UPDATE, $res")
                }
                BROADCAST_ERROR_UPDATE -> {
                    var msg = intent.getStringExtra("MSG")
                    if(!msg.isNullOrEmpty()){
                        tvUpdate.text = msg
                    }

                    Handler().postDelayed({
                        if(Util.getPosUpdate().isNullOrEmpty()) {
                            finish()
                        }
                        UpdateService.setUpdateFinish()
                    }, 2000)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_update)

        //리시버 생성
        registerReceiver(broadCastReceiver, IntentFilter(BROADCAST_STATUS_UPDATE))
        registerReceiver(broadCastReceiver, IntentFilter(BROADCAST_FINISHED_UPDATE))
        registerReceiver(broadCastReceiver, IntentFilter(BROADCAST_ERROR_UPDATE))
        active = true

        Log.i("UpdateActivity", "onCreate intent, ${intent.getStringExtra("MSG")}")
        var msg = intent.getStringExtra("MSG")
        var id = intent.getStringExtra("ID")
        var res = intent.getBooleanExtra("RES", false)

        if(!msg.isNullOrEmpty()){
            tvUpdate.text = msg
        }

        if(!id.isNullOrEmpty()){
            when(id){
                "VNC" -> {
                }
                "POS" -> {
                }
                "AGENT" -> {
                    UpdateService.setUpdateFinish()
                    var isRunning = ForegroundService.isServiceRunning(this@UpdateActivity)
                    if (!isRunning) {
                        ForegroundService.startService(
                            this@UpdateActivity,
                            "Agent service is running..."
                        )
                    }

                    Handler().postDelayed({
                        if(Util.getPosUpdate().isNullOrEmpty() && !UpdateService.isUpdating()) {
                            finish()
                        }
                    }, 2000L)
                }
                "COMMANDER" -> {

                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(broadCastReceiver)
        active = false
    }

    override fun onNewIntent(intent: Intent){
        super.onNewIntent(intent)
        Log.i("UpdateActivity", "onNewIntent intent, ${intent.getStringExtra("MSG")}")
        var msg = intent.getStringExtra("MSG")
        var id = intent.getStringExtra("ID")
        var res = intent.getBooleanExtra("RES", false)

        if(!msg.isNullOrEmpty()){
            tvUpdate.text = msg
        }

        if(!id.isNullOrEmpty()){
            when(id){
                "VNC" -> {
                }
                "POS" -> {
                }
                "AGENT" -> {
                    UpdateService.setUpdateFinish()
                    var isRunning = ForegroundService.isServiceRunning(this@UpdateActivity)
                    if (!isRunning) {
                        ForegroundService.startService(
                            this@UpdateActivity,
                            "Agent service is running..."
                        )
                    }

                    Handler().postDelayed({
                        Log.i("UpdateActivity", "postDelayed ${Util.getPosUpdate().isNullOrEmpty()}, ${UpdateService.isUpdating()}")
                        if(Util.getPosUpdate().isNullOrEmpty() && !UpdateService.isUpdating()) {
                            finish()
                        }
                    }, 2000L)
                }
                "COMMANDER" -> {
                }
            }
        }
    }
}
