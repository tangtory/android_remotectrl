package com.lotte.mart.messengerlib.messenger.service

import android.content.Context
import android.os.Bundle

import com.lotte.mart.messengerlib.messenger.callback.ConnectionCallback
import com.lotte.mart.messengerlib.messenger.callback.ResponseCallback
import com.lotte.mart.messengerlib.messenger.constant.MessengerConstant
import com.lotte.mart.messengerlib.messenger.module.client.MessengerManager
import com.lotte.mart.commonlib.exception.ExceptionHandler
import com.lotte.mart.commonlib.log.Log

/**
 * Process간 통신을 위한 서비스 바인딩 클라이언트
 * 서버에서 제공하는 서비스를 바인딩하여 서비스 이용
 * @param context - Context
 * @param servicePackage - 원격지 서비스 패키지명
 */
class ClientMessengerService (context: Context, servicePackage: String) {
    companion object{
        private var instance : ClientMessengerService? = null
        private var manager : MessengerManager? = null
        private var mContext : Context? = null
        private var packageName : String? = null
        var tag : String = ClientMessengerService::class.java.simpleName

        /**
         * 서비스 생성
         * @param context - Context
         * @param servicePackage - 원격지 서비스 패키지명
         * @return ClientMessengerService 인스턴스
         */
        fun with(context: Context, packageName: String) : ClientMessengerService? = ExceptionHandler.tryOrDefault(null) {
            Log.d(tag, "Start initialization")
            if(instance == null) {
                instance =
                    ClientMessengerService(
                        context,
                        packageName
                    )
                Log.d(tag, "instance init")
            }

            Log.d(tag, "End initialization, $instance")
            instance
        }

        fun release(){
            if(instance !== null)
                instance!!.closeService()
            instance = null
            manager = null
        }
    }

    init {
        mContext = context
        packageName = servicePackage
    }

    /**
     * 원격지로 정의된 요청 전송
     * @param what - 메시지 타입 MessengerConstant참조
     * @param bundle - 데이터 및 콜백 전달
     * @param callback - 요청 결과를 수신 받을 콜백
     */
    private fun send(what: Int, bundle: Bundle, callback: ResponseCallback) = ExceptionHandler.tryOrDefault() {
        if(manager == null) {
            manager = packageName?.let { MessengerManager(it, callback) }
            manager?.startBindService(
                mContext!!, object :
                    ConnectionCallback {
                    override fun connected(result: Boolean) {
//                        bundle.putSerializable(MessengerConstant.CALLBACK, callback as Serializable);
//                        bundle.putParcelable(MessengerConstant.CALLBACK, RequestParcel(callback, bundle.getString(MessengerConstant.DATA_CMD)!!))
                        manager?.send(what, bundle)

//                        Message.obtain(null, what, 0, 0, RequestParcel(callback, bundle.getString(MessengerConstant.DATA_CMD)!!).run {
//                            manager?.send(what)
//                        })
                    }
                })
        } else {
            if(manager!!.getConnected()) {
//                bundle.putSerializable(MessengerConstant.CALLBACK, callback as Serializable);
                manager?.send(what, bundle)
            } else {
                manager?.startBindService(
                    mContext!!, object :
                        ConnectionCallback {
                        override fun connected(result: Boolean) {
//                            bundle.putSerializable(MessengerConstant.CALLBACK, callback as Serializable);
                            manager?.send(what, bundle)
                        }
                    })
            }
        }
    }

    /**
     * 원격지로 정의된 요청 전송
     * @param what - 메시지 타입 MessengerConstant참조
     * @param callback - 요청 결과를 수신 받을 콜백
     */
    private fun send(what: Int, callback: ResponseCallback) = ExceptionHandler.tryOrDefault() {
        var bundle = Bundle()
        if(manager == null) {
            manager = packageName?.let { MessengerManager(it, callback) }
            manager?.startBindService(
                mContext!!, object :
                    ConnectionCallback {
                    override fun connected(result: Boolean) {
                        manager?.send(what, bundle)
                    }
                })
        } else {
            if(manager!!.getConnected()) {
                manager?.send(what, bundle)
            } else {
                manager?.startBindService(
                    mContext!!, object :
                        ConnectionCallback {
                        override fun connected(result: Boolean) {
                            manager?.send(what, bundle)
                        }
                    })
            }
        }
    }

    /**
     * 서비스 바인딩 종료
     */
    fun closeService() = ExceptionHandler.tryOrDefault() {
        if(manager != null) {
            manager?.stopBindService()
        }
    }

    /**
     * 전원 종료 요청
     * @param callback - 요청 결과를 수신 받은 콜백
     */
    fun powerOff(callback: ResponseCallback) = ExceptionHandler.tryOrDefault() {
        send(MessengerConstant.POWER_OFF, callback)
    }

    /**
     * 재부팅 요청
     * @param callback - 요청 결과를 수신 받은 콜백
     */
    fun reboot(callback: ResponseCallback) = ExceptionHandler.tryOrDefault() {
        send(MessengerConstant.REBOOT, callback)
    }

    /**
     * 앱 업데이트 요청
     * @param callback - 요청 결과를 수신 받은 콜백
     */
    fun appUpdate(version:String, callback: ResponseCallback) = ExceptionHandler.tryOrDefault() {
        var bundle = Bundle()
        bundle.putString(MessengerConstant.DATA_VERSION, version)
        send(MessengerConstant.APP_UPDATE, bundle, callback)
    }

    /**
     * 아이피 허용 대역대 설정
     * @param callback - 요청 결과를 수신 받은 콜백
     */
    fun setIpAddressBound(ips:Array<String>, callback: ResponseCallback) = ExceptionHandler.tryOrDefault() {
//        var arr = ArrayList<String>()
//        arr.get(10)
        var bundle = Bundle()
        bundle.putStringArray(MessengerConstant.DATA_IPS, ips)
        send(MessengerConstant.SET_IPADDRESS_BOUND, bundle, callback)
    }

    fun cmdExec(cmd : String, callback : ResponseCallback) = ExceptionHandler.tryOrDefault() {
        var bundle = Bundle()
        bundle.putString(MessengerConstant.DATA_CMD, cmd)
        send(MessengerConstant.CMD_EXEC, bundle, callback)
    }

    fun setTime(h:Int, m:Int, s:Int, callback : ResponseCallback) = ExceptionHandler.tryOrDefault() {
        var bundle = Bundle()
        bundle.putInt(MessengerConstant.DATA_HOUR, h)
        bundle.putInt(MessengerConstant.DATA_MINUTE, m)
        bundle.putInt(MessengerConstant.DATA_SECOND, s)
        send(MessengerConstant.SET_TIME, bundle, callback)
    }

    fun setDateTime(Y:Int, M:Int, D:Int, h:Int, m:Int, s:Int, callback : ResponseCallback) = ExceptionHandler.tryOrDefault() {
        var bundle = Bundle()
        bundle.putInt(MessengerConstant.DATA_YEAR, Y)
        bundle.putInt(MessengerConstant.DATA_MONTH, M)
        bundle.putInt(MessengerConstant.DATA_DAY, D)
        bundle.putInt(MessengerConstant.DATA_HOUR, h)
        bundle.putInt(MessengerConstant.DATA_MINUTE, m)
        bundle.putInt(MessengerConstant.DATA_SECOND, s)
        send(MessengerConstant.SET_DATETIME, bundle, callback)
    }
}