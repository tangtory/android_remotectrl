package com.lotte.mart.messengerlib.messenger.service

import android.os.Bundle
import android.os.Message
import android.os.Messenger
import android.os.RemoteException
import android.util.Log
import com.lotte.mart.messengerlib.messenger.callback.RequestCallback
import com.lotte.mart.messengerlib.messenger.callback.ResponseCallback
import com.lotte.mart.messengerlib.messenger.constant.MessengerConstant
import com.lotte.mart.messengerlib.messenger.module.server.MessengerManager
import com.lotte.mart.commonlib.exception.ExceptionHandler
import java.io.Serializable

/**
 * Process간 통신을 위한 서비스 제공 서버
 * 클라이언트에서 서비스를 바인딩 할 수 있도록 서비스를 제공
 */
class ServerMessengerService () {
    companion object{
        private var instance : ServerMessengerService? = null
        private var manager : MessengerManager? = null
        var tag : String = ServerMessengerService::class.java.simpleName
        var mCallback : RequestCallback? = null

        /**
         * 서비스 생성
         * @return ServerMessengerService 인스턴스
         */
        fun with() : ServerMessengerService? = ExceptionHandler.tryOrDefault(null) {
            Log.d(tag, "Start initialization")
            if(instance == null) {
                instance =
                    ServerMessengerService()
                Log.d(tag, "instance init")
            }

            Log.d(tag, "End initialization, $instance")
            instance
        }

        /**
         * 요청 원격지로 요청 처리 성공 응답 전송
         * @param messenger - 응답을 전송 할 대상 Messenger
         * @param bd - 수신받은 요청 데이터
         */
        fun replySuccess(messenger: Messenger, bd: Bundle) = ExceptionHandler.tryOrDefault() {
            val msg = Message.obtain(null, MessengerConstant.SUCCEED, 0, 0)
            msg.data = bd

            try {
                messenger.send(msg)
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        }

        /**
         * 요청 원격지로 요청 처리 성공 응답 전송
         * @param messenger - 응답을 전송 할 대상 Messenger
         * @param bd - 수신받은 요청 데이터
         */
        fun replyFailed(messenger: Messenger, bd: Bundle) = ExceptionHandler.tryOrDefault() {
            val msg = Message.obtain(null, MessengerConstant.FAILED, 0, 0)
            msg.data = bd

            try {
                messenger.send(msg)
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        }
    }

    init {

    }

    /**
     * 할당된 요청 수신 콜백
     * @return 할당된 요청 수신 콜백 반환
     */
    fun getCallback() : RequestCallback? {
        return mCallback
    }

    /**
     * 요청 수신 콜백 할당
     * @param callback 요청 수신 콜백
     */
    fun setCallback(callback: RequestCallback) = ExceptionHandler.tryOrDefault() {
        mCallback = callback
    }

    /**
     * 원격지로 정의된 요청 전송
     * @param what - 메시지 타입 MessengerConstant참조
     * @param bundle - 데이터 및 콜백 전달
     */
    private fun send(what: Int, bundle: Bundle) = ExceptionHandler.tryOrDefault() {
        manager?.send(what, bundle)
    }

    /**
     * 원격지로 정의된 요청 전송
     * @param what - 메시지 타입 MessengerConstant참조
     * @param bundle - 데이터 및 콜백 전달
     * @param callback - 요청 결과를 수신 받을 콜백
     */
    private fun send(what: Int, bundle: Bundle, callback: ResponseCallback) = ExceptionHandler.tryOrDefault() {
        bundle.putSerializable(MessengerConstant.CALLBACK, callback as Serializable);
        manager?.send(what, bundle)
    }

    /**
     * 원격지로 정의된 요청 전송
     * @param what - 메시지 타입 MessengerConstant참조
     * @param callback - 요청 결과를 수신 받을 콜백
     */
    private fun send(what: Int, callback: ResponseCallback) = ExceptionHandler.tryOrDefault() {
        Log.d(tag, "send, $manager")
        var bundle = Bundle()
        bundle.putSerializable(MessengerConstant.CALLBACK, callback as Serializable);
        manager?.send(what, bundle)
    }

    fun powerOff(callback: ResponseCallback){
        send(MessengerConstant.POWER_OFF, callback)
    }

    fun reboot(callback: ResponseCallback){
        send(MessengerConstant.REBOOT, callback)
    }

    fun appUpdate(version:String, callback: ResponseCallback){
        var bundle = Bundle()
        bundle.putString(MessengerConstant.DATA_VERSION, version)
        send(MessengerConstant.APP_UPDATE, bundle, callback)
    }

    fun setIpAddressBound(ips:Array<String>, callback: ResponseCallback){
        var bundle = Bundle()
        bundle.putStringArray(MessengerConstant.DATA_IPS, ips)
        send(MessengerConstant.SET_IPADDRESS_BOUND, bundle, callback)
    }
}