package com.lotte.mart.vncserver.service.schema

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.lotte.mart.commonlib.log.Log
import com.lotte.mart.vncserver.data.Constant

//스키마 작업
class Worker {
    companion object {
        val TAG = Worker::class.java.simpleName

        //VNC 접속정보 -> Agent
        fun agentVncInfo(info: String, context: Context){
            val url = Constant.getAgentURLVncInfo(info)
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        }

        //VNC SFTP정보 -> Agent
        fun agentSftpInfo(info: String, context: Context){
            val url = Constant.getAgentURLSftpInfo(info)
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        }

        //스키마 파서
        fun parser(intent: Intent, listener : SchemaListener, context: Context):Boolean{
            if (intent.action.equals(Intent.ACTION_VIEW)) {
                val uri = intent.data
                val schema = uri?.scheme
                val host = uri?.host
                val type = uri?.getQueryParameter(Constant.TYPE)
                Log.i(TAG, "received schema, $uri")
                //스키마 구분(VNC)
                if(schema == Constant.SCHEMA_VNC) {
                    when (type) {
                        //앱종료
                        Constant.TYPE_APPFINISH -> {
                            listener.onAppFinish()
                        }
                        //앱 업데이트 완료
                        Constant.TYPE_APPUPDATEFINISH -> {
                            val res = uri?.getQueryParameter(Constant.RESULT)
                            if(res == Constant.RESULT_SUCCESS)
                                listener.onAppUpdateFinish(true)
                            else
                                listener.onAppUpdateFinish(false)
                        }
                        //그외 (에러)
                        else -> {
                            listener.onError(Constant.ERROR)
                        }
                    }
                }
                return true
            }
            return false
        }
    }
}