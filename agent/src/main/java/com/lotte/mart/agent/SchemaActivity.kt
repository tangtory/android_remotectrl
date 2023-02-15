package com.lotte.mart.agent

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.widget.Toast
import com.lotte.mart.agent.data.Constant
import com.lotte.mart.agent.service.ForegroundService
import com.lotte.mart.agent.service.UpdateService
//import com.lotte.mart.agent.logger.Log

import com.lotte.mart.agent.service.schema.SchemaListener
import com.lotte.mart.agent.service.schema.Worker
import com.lotte.mart.agent.utils.AgentIniUtil
import com.lotte.mart.agent.utils.Util
import com.lotte.mart.commonlib.log.Log
import com.lotte.mart.messengerlib.messenger.callback.ResponseCallback
import com.lotte.mart.messengerlib.messenger.service.ClientMessengerService

/**
 * POS <-> Agent간 Schema처리
 */
class SchemaActivity : AppCompatActivity() {
    val TAG = SchemaActivity::class.java.simpleName
    var mHandler = object : Handler(Looper.getMainLooper()){
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when(msg.what){
                1-> Toast.makeText(this@SchemaActivity, ""+msg.obj as String, Toast.LENGTH_SHORT).show()
                2-> {
                    val path = msg.obj as String
                    val servicePackage = "com.lotte.mart.commander"
                    val cmd = "pm install -r -i com.lotte.mart.commander --user 0 $path"
                    Log.d("onAppUpdate",cmd)

                    ClientMessengerService.release()
                    ClientMessengerService.with(this@SchemaActivity, servicePackage)?.cmdExec(
                        cmd,
                        object :
                            ResponseCallback {
                            override fun onSuccess(res: String) {
                                Log.d("d", "onSuccess $res")
                                if(res.toUpperCase().trim() == "SUCCESS") {
                                    val m = obtainMessage()
                                    m.what = 1
                                    m.obj = "POS 업데이트 완료"
                                    sendMessage(m)
                                    Worker.appUpdateFinish(Constant.RESULT_SUCCESS,this@SchemaActivity)
                                }
                                else{
                                    val m = obtainMessage()
                                    m.what = 1
                                    m.obj = "POS 업데이트 실패"
                                    sendMessage(m)
                                    Worker.appUpdateFinish(Constant.RESULT_FAIL,this@SchemaActivity)
                                }

                                UpdateService.setUpdateFinish()
                                ClientMessengerService.release()
                            }

                            override fun onFail(err: String) {
                                //failed
                                val m = obtainMessage()
                                m.what = 1
                                m.obj = "POS 업데이트 실패"
                                sendMessage(m)
                                Worker.appUpdateFinish(Constant.RESULT_FAIL,this@SchemaActivity)
                                UpdateService.setUpdateFinish()
                                ClientMessengerService.release()
                            }

                            override fun onResponse(msg: String) {
                                Log.d("d", "onResponse $msg")
                                val m = obtainMessage()
                                m.what = 1
                                m.obj = "POS 업데이트 진행중..."
                                sendMessage(m)
                            }
                        })
                }
                3->{
                    val time = msg.obj as String
                    Toast.makeText(this@SchemaActivity, "[AGENT]시간 설정 $time", Toast.LENGTH_SHORT).show()
                    setTime(time)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_schema)

        //스키마 파싱
        Worker.parser(intent, object : SchemaListener {
            override fun onCommanderUpdateFinish(result: String) {

            }

            override fun onVncUpdateFinish(result: String) {

            }

            override fun onAgentUpdateFinish(result: String) {
            }

            override fun onAppUpdate(path:String) {
                Log.d("WorkerInterface", "onAppUpdate, $path")
                if(!Util.getPosUpdate().isNullOrEmpty()) {
                    Log.d(TAG, "pos update exist, ${Util.getPosUpdate()}")
                    return
                }

                //POS 업데이트 값 세팅
                Util.setPosUpdate(path)

                //서비스 실행 여부에 따라 포그라운드 서비스 실행
                var isRunning = ForegroundService.isServiceRunning(this@SchemaActivity)
                if (isRunning) {
                    finish()
                } else {
                    //업데이트 요청을 통한 서비스 실행의 경우 첫번째 실행일 수 있으므로 ini 파일및 데이터 검사 진행
                    if(!Util.isExistFile(AgentIniUtil.Constant.FILE_SETTING_PATH)){
                        //ini파일 오류의 경우 PDA 재시작
                        Toast.makeText(this@SchemaActivity, "PDA를 재시작합니다", Toast.LENGTH_LONG).show()
                        //POS앱 종료 요청
                        Worker.appFinish(this@SchemaActivity)
                        Handler().postDelayed({
                            val cmd = "svc power reboot"
                            val url = "androidcommander://agent?type=Cmd&cmd=$cmd"
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            startActivity(intent)
                        }, 1000)
                    } else {
                        if(!UpdateActivity.isActive()) {
                            //업데이트 화면 비활성화 일 경우 액티비티 호출
                            var i = Intent(this@SchemaActivity, UpdateActivity::class.java)
                            i.putExtra("MSG", "POS 업데이트 대기")
                            i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                            startActivity(i)
                        }

                        //포그라운드 서비스 가동
                        ForegroundService.startService(
                            this@SchemaActivity,
                            "Agent service is running..."
                        )
                    }
                }
            }

            override fun onAppUpdateFinish(result: String) {

            }

            override fun onSetTime(time: String) {
                Log.d("WorkerInterface", "onSetTime, $time")
                //시간 설정 요청에 따라 명령 수행
                val m = mHandler.obtainMessage()
                m.what = 3
                m.obj = time
                mHandler.sendMessage(m)
            }

            override fun onSetTimeFinish(result: String) {
                Log.d("WorkerInterface", "onSetTimeFinish, $result")
                //시간 설정 완료 시 완료 응답 전송
                Worker.setTime(result, this@SchemaActivity)
                finish()
            }

            override fun onReboot(reboot: Boolean) {
                Log.d("WorkerInterface", "onReboot, $reboot")
                //재부팅 명령 수행
                val m = mHandler.obtainMessage()
                m.what = 1
                m.obj = "[AGENT]PDA 종료"
                mHandler.sendMessage(m)

                //reboot true, 재부팅
                //reboot false, 종료
                val cmd = if(reboot) "svc power reboot" else "svc power shutdown"

                val url = "androidcommander://agent?type=Cmd&cmd=$cmd"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                this@SchemaActivity.startActivity(intent)
                Log.d(TAG, "onReboot, $url")
            }

            override fun onError(type: String) {
                val m = mHandler.obtainMessage()
                m.what = 1
                m.obj = "[AGENT]에러"
                mHandler.sendMessage(m)
            }

        }, this@SchemaActivity)

        finish()
    }

    /**
     * 시스템 시간 설정
     * @param time - 설정 시간
     */
    fun setTime(time : String){
        Thread(Runnable {
            try {
                val url = "androidcommander://agent?type=SetTime&time=$time"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                this@SchemaActivity.startActivity(intent)
                Log.d(TAG, "setTime, $url")
            } catch (e:Exception) {
                e.printStackTrace()
                Log.e(TAG, "setTime exception, $time", e)
                Worker.setTime(Constant.RESULT_FAIL, this@SchemaActivity)
            }
        }).start()
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}