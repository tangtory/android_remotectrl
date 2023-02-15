package com.lotte.mart.messengerlib.messenger.module.client

import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import com.lotte.mart.messengerlib.messenger.constant.MessengerConstant
import com.lotte.mart.messengerlib.messenger.callback.ResponseCallback
import com.lotte.mart.commonlib.exception.ExceptionHandler

/**
 * Messenger 클라이언트 핸들러 
 * 요청에 대한 응답 처리
 */
internal class MessengerHandler(callback: ResponseCallback) : Handler(Looper.getMainLooper()) {
    var tag : String = MessengerHandler::class.java.simpleName
    var mCallback : ResponseCallback?=null
    companion object {

    }

    init {
        mCallback = callback
    }

    override fun handleMessage(msg: Message) = ExceptionHandler.tryOrDefault(){
        when (msg.what) {
            MessengerConstant.POWER_OFF -> {    //전원 OFF
                mCallback!!.onResponse("POWER_OFF")
            }
            MessengerConstant.REBOOT -> {   //시스템 재부팅
                mCallback!!.onResponse("REBOOT")
            }
            MessengerConstant.APP_UPDATE -> {   //앱 업데이트
                mCallback!!.onResponse("APP_UPDATE")
            }
            MessengerConstant.SET_IPADDRESS_BOUND -> {  //아이피 허용 영역 설정
                mCallback!!.onResponse("SET_IPADDRESS_BOUND")
            }
            MessengerConstant.CMD_EXEC -> {   //shell 실행
                mCallback!!.onResponse("CMD_EXEC")
            }
            MessengerConstant.SET_TIME -> {   //시간 설정
                mCallback!!.onResponse("SET_TIME")
            }
            MessengerConstant.SET_DATETIME -> {   //날짜 시간 설정
                mCallback!!.onResponse("SET_DATETIME")
            }
            MessengerConstant.FAILED -> {   //요청 처리 실패
                var res = ""
                res = msg.data.getString(MessengerConstant.DATA_RESULT)!!
                mCallback!!.onFail("FAILED_REQUEST, $res")
            }
            MessengerConstant.SUCCEED -> {  //요청 처리 성공
                Log.d("result", msg.data.getString(MessengerConstant.DATA_RESULT)!!)
                var res = ""
                res = msg.data.getString(MessengerConstant.DATA_RESULT)!!
                mCallback!!.onSuccess(res)
            }
            else ->
                mCallback!!.onFail("NO_REPLY") //응답없음
        }
//        mCallback = null
    }
}