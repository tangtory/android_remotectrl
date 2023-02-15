package com.lotte.mart.commander.schema

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.lotte.mart.commander.data.ConstantSchema

/**
 * 스키마 작업 Class
 * 외부 앱에서 인텐트를 전달 받은 스키마를 파싱하거나 전송함
 */
class Worker {
    companion object {
        /**
         * 시간 설정 응답 전송
         * @param host - 요청 host
         * @param result - 수행 결과
         * @param context - context
         */
        fun setTimeResponse(host: String, result: String,context: Context){
            val url = ConstantSchema.getSetTimeURL(host, result)
            if(url == ConstantSchema.ERROR)
                return
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        }

        /**
         * 명령 수행 응답 전송
         * @param host - 요청 host
         * @param result - 수행 결과
         * @param context - context
         */
        fun cmdResponse(host: String, result: String, response: String, cmd:String, context: Context){
            val url = ConstantSchema.getCmdURL(host, result, response, cmd)
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        }

        /**
         * 수신 스키마 파싱
         * @param intent - intent
         * @param listener - 스키마 리스너
         */
        fun parser(intent: Intent, listener : SchemaListener){
            if (intent.action.equals(Intent.ACTION_VIEW)) {
                val uri = intent.data
                val host = uri?.host
                val type = uri?.getQueryParameter(ConstantSchema.TYPE)
                when(type){
                    ConstantSchema.TYPE_CMD ->{
                        val cmd = uri?.getQueryParameter(ConstantSchema.CMD)
                        //명령 수행
                        listener.onCmd(cmd!!, host!!)
                    }
                    ConstantSchema.TYPE_SETTIME ->{
                        //시간 설정
                        val time = uri?.getQueryParameter(ConstantSchema.TIME)
                        listener.onSetTime(time.toString(), host!!)
                    }
                    else ->{
                        //그외 에러
                        listener.onError(ConstantSchema.ERROR)
                    }
                }
            }
        }
    }
}