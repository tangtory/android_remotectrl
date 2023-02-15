package com.lotte.mart.messengerlib.messenger.module.server

import android.os.*
import com.lotte.mart.messengerlib.messenger.service.ServerMessengerService
import com.lotte.mart.messengerlib.messenger.constant.MessengerConstant
import com.lotte.mart.commonlib.exception.ExceptionHandler

/**
 * Messenger 서비스 핸들러
 * 요청에 대한 응답 처리
 */
internal class MessengerHandler() : Handler(Looper.getMainLooper()) {
    var tag : String = MessengerHandler::class.java.simpleName
    var messenger : Messenger? = null

    companion object {
        /**
         * 요청 원격지로 응답 전송
         * @param messenger - 응답을 전송 할 대상 Messenger
         * @param what - 메시지 타입 MessengerConstant참조
         * @param bd - 수신받은 요청 데이터
         */
        fun reply(messenger: Messenger, what:Int, bd: Bundle) = ExceptionHandler.tryOrDefault(){
            val msg = Message.obtain(null, what, 0, 0)
            msg.data = bd
            try {
                messenger.send(msg)
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        }
    }

    init{
    }

    override fun handleMessage(msg: Message) = ExceptionHandler.tryOrDefault(){

        var messenger = msg.replyTo
        val bundle = msg.data

        if(ServerMessengerService.with()?.getCallback() == null) {
            reply(messenger, 0, msg.data)
        } else {
            reply(messenger, msg.what, msg.data)
            when (msg.what) {
                MessengerConstant.POWER_OFF -> {    //전원 OFF
                    ServerMessengerService.with()?.getCallback()?.powerOff("POWER_OFF", true, msg)
                }
                MessengerConstant.REBOOT -> {   //시스템 재부팅
                    ServerMessengerService.with()?.getCallback()?.reboot("REBOOT", true)
                }
                MessengerConstant.APP_UPDATE -> {   //앱 업데이트
                    ServerMessengerService.with()?.getCallback()?.appUpdate("APP_UPDATE", true)
                }
                MessengerConstant.SET_IPADDRESS_BOUND -> {  //아이피 허용 영역 설정
                    ServerMessengerService.with()?.getCallback()
                        ?.setIpAddressBounds("SET_IPADDRESS_BOUND", true)
                }
                MessengerConstant.CMD_EXEC -> {
                    var cmd = bundle.getString(MessengerConstant.DATA_CMD)
                    ServerMessengerService.with()?.getCallback()
                        ?.cmdExec(cmd!!, true, msg)
                }
                MessengerConstant.SET_TIME -> {
                    var hour = bundle.getInt(MessengerConstant.DATA_HOUR)
                    var minute = bundle.getInt(MessengerConstant.DATA_MINUTE)
                    var second = bundle.getInt(MessengerConstant.DATA_SECOND)
                    ServerMessengerService.with()?.getCallback()
                        ?.setTime(hour, minute, second,true, msg)
                }
                MessengerConstant.SET_DATETIME -> {
//                    bundle.getSerializable(MessengerConstant.CALLBACK)
//                    Log.d("debug", "" + bundle[MessengerConstant.DATA_HOUR])
                    var year = bundle.getInt(MessengerConstant.DATA_YEAR)
                    var month = bundle.getInt(MessengerConstant.DATA_MONTH)
                    var day = bundle.getInt(MessengerConstant.DATA_DAY)
                    var hour = bundle.getInt(MessengerConstant.DATA_HOUR)
                    var minute = bundle.getInt(MessengerConstant.DATA_MINUTE)
                    var second = bundle.getInt(MessengerConstant.DATA_SECOND)
                    ServerMessengerService.with()?.getCallback()
                        ?.setDateTime(year, month, day, hour, minute, second,true, msg)
                }
                else -> ServerMessengerService.with()?.getCallback()
                    ?.setIpAddressBounds("NON_TYPE", false) //정의 되지 않은 요청
            }
        }
    }
}