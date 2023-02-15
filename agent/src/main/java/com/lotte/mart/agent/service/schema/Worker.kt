package com.lotte.mart.agent.service.schema

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.lotte.mart.agent.room.database.AgentLogDatabase
import com.lotte.mart.agent.room.entity.AgentLogEntity
import com.lotte.mart.agent.data.Constant
import com.lotte.mart.agent.service.UpdateService
import com.lotte.mart.agent.utils.AgentIniUtil
import com.lotte.mart.agent.utils.Util
import com.lotte.mart.commonlib.log.Log
import java.lang.Exception

class Worker {
    companion object {
        val TAG = Worker::class.java.simpleName
        fun agentUpdateFinish(result: String, context: Context){
            val url = Constant.getAgentUpdateFinishURL(result)
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        }

        fun vncFinish(context: Context){
            val url = Constant.getVncFinishURL()
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            context.startActivity(intent)
            Log.i(TAG, "vncFinish $url")
        }

        fun vncUpdateFinish(result: String, context: Context){
            val url = Constant.getVncUpdateFinishURL(result)
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            context.startActivity(intent)
            Log.i(TAG, "vncUpdateFinish $url")
        }

        fun appFinish(context: Context){
            val url = Constant.getFinishURL()
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            //intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            context.startActivity(intent)
            Log.i(TAG, "appFinish $url")
        }

        fun appUpdateFinish(result: String, context: Context){
            val url = Constant.getUpdateFinishURL(result)
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            //intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            context.startActivity(intent)
            Log.i(TAG, "appUpdateFinish $url")
        }

        fun setTime(result:String, context: Context){
            val url = Constant.getSetTimeURL(result)
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            //intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            context.startActivity(intent)
            Log.i(TAG, "setTime $url")
        }

        fun parser(intent: Intent, listener : SchemaListener, context: Context):Boolean{
            if (intent.action.equals(Intent.ACTION_VIEW)) {
                val uri = intent.data
                val schema = uri?.scheme
                val host = uri?.host
                val type = uri?.getQueryParameter(Constant.TYPE)
                Log.i(TAG, "received schema, $uri")
                if(schema == Constant.SCHEMA_REMOTE) {
                    when (type) {
                        Constant.TYPE_APPUPDATE -> {
                            val file = uri?.getQueryParameter(Constant.FILE)
                            //앱 종료
                            listener.onAppUpdate("${UpdateService.Constant.ROOT}${UpdateService.Constant.FILE_APK_PATH}/${file}")
                        }
                        Constant.TYPE_SETTIME -> {
                            //시간 설정
                            val time = uri.getQueryParameter(Constant.TIME)
                            listener.onSetTime(time.toString())
                        }
                        Constant.TYPE_REBOOT -> {
                            //앱 종료
                            val reboot = uri.getQueryParameter(Constant.REBOOT)
                            listener.onReboot(reboot!!.toBoolean())
                        }
                        else -> {
                            //그외 에러
                            listener.onError(Constant.ERROR)
                        }
                    }
                } else if(schema == Constant.SCHEMA_AGENT){
                    when (type) {
                        Constant.TYPE_SETTIME -> {
                            //시간 설정
                            val result = uri?.getQueryParameter(Constant.RESULT) as String
                            Log.d(TAG, "SetTime, $result")
                            listener.onSetTimeFinish(result)
                        }
                        Constant.TYPE_APPUPDATEFINISH -> {
                            val result = uri?.getQueryParameter(Constant.RESULT) as String
                            listener.onAgentUpdateFinish(result)
                        }
                        Constant.TYPE_CMD -> {
                            val result = uri?.getQueryParameter(Constant.RESULT) as String
                            val data = uri?.getQueryParameter(Constant.DATA) as String
                            val cmd = uri?.getQueryParameter(Constant.CMD) as String
                            Log.d(TAG, "TYPE_CMD $cmd")
                            if(Util.getCurrentAgentVersion().isNotEmpty() && cmd.contains(Util.getCurrentAgentVersion().split('.')[0])) {
                                if(data.toUpperCase().trim() == "SUCCESS") {
                                    Log.d(TAG, "AGENT 업데이트 완료, $data")
                                    Util.setAgentUpdateFinish(1)
                                    listener.onAgentUpdateFinish(Constant.RESULT_SUCCESS)
                                }
                                else{
                                    Log.e(TAG, "AGENT 업데이트 실패, $data, ${Util.getAgentBackupVersion()}, ${Util.getAgentUpdateFinish()}")
                                    Util.setCurrentAgentVersion(Util.getAgentBackupVersion())
                                    //AGENT UPDATE 실패 로그
                                    UpdateService.agentLog(context, context.packageName, "Update error, $data")
                                    listener.onAgentUpdateFinish(Constant.RESULT_FAIL)
                                }
                            } else if(Util.getPosUpdate().isNotEmpty() && cmd.contains(Util.getPosUpdate().split('.')[0])) {
                                if(data.toUpperCase().trim() == "SUCCESS") {
                                    Log.d(TAG, "POS 업데이트 완료, $data")
                                    listener.onAppUpdateFinish(Constant.RESULT_SUCCESS)
                                }
                                else{
                                    Log.e(TAG, "POS 업데이트 실패, $data, ${Util.getPosUpdate()}")
                                    listener.onAppUpdateFinish(Constant.RESULT_FAIL)
                                    //POS UPDATE 실패 로그
                                    UpdateService.agentLog(context, AgentIniUtil.getInstance().getPosPackageName(
                                        "com.lotte.mart.cloudpos"
                                    ), "Update error, $data")
                                }

                                Util.setPosUpdate("")
                            } else if(Util.getCurrentVncVersion().isNotEmpty() && cmd.contains(Util.getCurrentVncVersion().split('.')[0])) {
                                if(data.toUpperCase().trim() == "SUCCESS") {
                                    Log.d(TAG, "VNC 업데이트 완료, $data")
                                    listener.onVncUpdateFinish(Constant.RESULT_SUCCESS)
                                }
                                else{
                                    Log.e(TAG, "VNC 업데이트 실패, $data, ${Util.getVncBackupVersion()}")
                                    Util.setCurrentVncVersion(Util.getVncBackupVersion())
                                    //VNC UPDATE 실패 로그
                                    UpdateService.agentLog(context,Util.getCurrentVncVersion(),"Update error, $data")
                                    listener.onVncUpdateFinish(Constant.RESULT_FAIL)
                                }
                            } else if(Util.getCurrentCmdVersion().isNotEmpty() && cmd.contains(Util.getCurrentCmdVersion().split('.')[0])) {
                                if(data.toUpperCase().trim() == "SUCCESS") {
                                    Log.d(TAG, "COMMANDER 업데이트 완료, $data")
                                    listener.onCommanderUpdateFinish(Constant.RESULT_SUCCESS)
                                }
                                else{
                                    Log.e(TAG, "COMMANDER 업데이트 실패, $data, ${Util.getCmdBackupVersion()}")
                                    Util.setLastCmdVersion(Util.getCmdBackupVersion())
                                    //COMMANDER UPDATE 실패 로그
                                    UpdateService.agentLog(context,Util.getCurrentCmdVersion(),"Update error, $data")
                                    listener.onCommanderUpdateFinish(Constant.RESULT_FAIL)
                                }
                            }
                        }
                        Constant.TYPE_VNCCONNECTION_INFO -> {
                            val info = uri?.getQueryParameter(Constant.INFO) as String
                            Log.i(TAG, info)
//                            vncLog(info, context)
                        }
                        Constant.TYPE_SFTPCONNECTION_INFO -> {
                            val info = uri?.getQueryParameter(Constant.INFO) as String
                            Log.i(TAG, info)
//                            vncLog(info, context)
                        }
                        else -> {
                            //그외 에러
                            listener.onError(Constant.ERROR)
                        }
                    }
                }

                return true
            }
            return false
        }

        private fun vncLog(info:String, context: Context){
            Thread(Runnable {
                try {
                    //20210222 시스템 상태 전송 - 데이터 입력
                    AgentLogDatabase.getInstance(context)!!.agentLogDao().insert(
                        AgentLogEntity(
                            null,
                            Util.getCurrentDateTime("yyyyMMdd"),
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            info,
                            "",
                            "",
                            "",
                            "",
                            ""
                        )
                    )
                }
                catch (e:Exception){
                    e.printStackTrace()
                } finally {
                    AgentLogDatabase.close()
                }

            }).start()
        }
    }

}