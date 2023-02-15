package com.lotte.mart.agent

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import com.lotte.mart.agent.data.Constant
import com.lotte.mart.agent.service.ForegroundService
import com.lotte.mart.agent.service.UpdateService
import com.lotte.mart.agent.service.schema.SchemaListener
import com.lotte.mart.agent.service.schema.Worker
import com.lotte.mart.agent.utils.AgentIniUtil
import com.lotte.mart.agent.utils.Util
import com.lotte.mart.commonlib.log.Log

/**
 * Agent Main Activity
 */
class MainActivity : AppCompatActivity() {
    val TAG = MainActivity::class.java.simpleName

    /**
     * 토스트 메시지 처리 핸들러
     */
    var mHandler = object : Handler(Looper.getMainLooper()){
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when(msg.what){
                1-> {
                    Toast.makeText(this@MainActivity, ""+msg.obj as String, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //퍼미션 확인 후 결과에 따라 퍼미션 요청 또는 포그라운드 서비스 실행
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
            || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            var i = Intent(this, PermissionActivity::class.java)
            i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(i)
            finish()
        } else {
            // 파일 검사
            if(!Util.isExistFile(AgentIniUtil.Constant.FILE_SETTING_PATH)){
                Toast.makeText(this@MainActivity, "POS 설정 완료 후 재시작해주세요", Toast.LENGTH_LONG).show()
                finish()
            } else {
                var isRunning = ForegroundService.isServiceRunning(this@MainActivity)
                if (isRunning) {
                    finish()
                } else {
                    //포그라운드 서비스 실행
                    ForegroundService.startService(
                        this@MainActivity,
                        "Agent service is running..."
                    )
                    finish()
                }
            }
        }

        //스키마 파싱 Agent <-> Commander, Vnc
        var schema = Worker.parser(intent, object : SchemaListener {
            override fun onCommanderUpdateFinish(result: String) {
                Log.d("WorkerInterface", "[COMMANDER] onCommanderUpdateFinish, $result")
                //Commander 업데이트 처리 결과(실제로 수행되지 않음)
                //Commander는 스스로 업데이트 하기 때문에 업데이트 완료 결과를 취득 할 수 없음
                //WatcherService에서 Commander앱 감시 후 재시작 시 버전비교하여 업데이트 결과 취득함
                val m = mHandler.obtainMessage()
                m.what = 1

                if(result == Constant.RESULT_FAIL){
                    m.obj = "COMMANDER 업데이트 실패, ${Util.getCurrentCmdVersion()}"
                } else {
                    m.obj = "COMMANDER 업데이트 완료, ${Util.getCurrentCmdVersion()}"
                }
                finish()
                mHandler.sendMessage(m)
                UpdateService.setUpdateFinish()
            }

            override fun onVncUpdateFinish(result: String) {
                Log.d("WorkerInterface", "[VNC] onVncUpdateFinish, $result")
                //Vnc 업데이트 처리 결과
                val intent = Intent()
                intent.action = UpdateActivity.BROADCAST_FINISHED_UPDATE
                intent.putExtra("RES", result)
                intent.putExtra("TYPE", "VNC")
                sendBroadcast(intent)
            }
            override fun onAgentUpdateFinish(result: String) {
                Log.d("WorkerInterface", "[Agent] onAgentUpdateFinish, $result")
                //Agent 업데이트 처리 결과
                if(result == Constant.RESULT_FAIL){
                    var i = Intent(this@MainActivity, UpdateActivity::class.java)
                    i.putExtra("MSG", "[${Util.getCurrentAgentVersion()}]\nAGENT 업데이트 실패")
                    i.putExtra("ID", "AGENT")
                    i.putExtra("RES", false)
                    i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    startActivity(i)
                } else {
                    var i = Intent(this@MainActivity, UpdateActivity::class.java)
                    i.putExtra("MSG", "[${Util.getCurrentAgentVersion()}]\nAGENT 업데이트 완료")
                    i.putExtra("ID", "AGENT")
                    i.putExtra("RES", true)
                    i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    startActivity(i)
                }
            }

            override fun onAppUpdate(path: String) {
            }

            override fun onAppUpdateFinish(result: String) {
                Log.d("WorkerInterface", "[POS] onAppUpdateFinish, $result")
                //POS앱 업데이트 처리 결과
                val intent = Intent()
                intent.action = UpdateActivity.BROADCAST_FINISHED_UPDATE
                intent.putExtra("RES", result)
                intent.putExtra("TYPE", "POS")
                sendBroadcast(intent)
            }

            override fun onSetTime(time: String) {
            }

            override fun onSetTimeFinish(result: String) {
                //시간 설정 완료 처리
                Worker.setTime(result, this@MainActivity)
                finish()
            }

            override fun onReboot(reboot: Boolean) {
            }

            override fun onError(type: String) {
            }

        }, this@MainActivity)

        Log.d(TAG, "has schema, $schema")

        if(schema)
            return

        Thread.setDefaultUncaughtExceptionHandler(ExceptionCatcher())
        finish()
    }

    /**
     * ExceptionCatcher
     */
    inner class ExceptionCatcher : Thread.UncaughtExceptionHandler {
        override fun uncaughtException(t: Thread, e: Throwable) {
            Log.i(TAG, "uncaughtException", e)
            finish()
        }
    }
}
