package com.lotte.mart.messengerlib.messenger.module.client

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import android.os.Messenger
import android.util.Log
import com.lotte.mart.messengerlib.messenger.callback.ConnectionCallback
import com.lotte.mart.commonlib.exception.ExceptionHandler
import com.lotte.mart.messengerlib.messenger.callback.ResponseCallback

/**
 * Messenger 클라이언트 매니저
 * 연결 및 통신 처리
 */
internal class MessengerManager(servicePackage: String, calllback: ResponseCallback) {
    private var mService: Messenger? = null
    private var mBound = false
    private var stop = false
    private lateinit var mContext: Context
    private var mCallback : ConnectionCallback? = null
    private var messenger : Messenger? = null
    //서비스(서버) 패키지 명
    private var packageName : String? = null

    //원격 바인드 서비스명
//    val SERVICE_ACTION: String get() = "MessengerService"
    val SERVICE_ACTION: String get() = "com.lotte.mart.messengerlib.messenger.module.server.MessengerService"
    companion object {
        var tag : String = MessengerManager::class.java.simpleName

    }

    init {
        initClass(servicePackage, calllback)
    }

    /**
     * 클래스 초기화
     */
    private fun initClass(servicePackage: String, calllback: ResponseCallback) = ExceptionHandler.tryOrDefault(){
        packageName = servicePackage
        if(messenger == null) {
            messenger = Messenger(MessengerHandler(calllback))
            Log.d(tag, "messenger init, $packageName")
        }
    }

    /**
     * Messenger Service 연결(bind) 상태
     * @return 연결 true, 연결해제 false
     */
    fun getConnected(): Boolean = ExceptionHandler.tryOrDefault(false){
        (mBound && mService != null)
    }

    /**
     * 원격 서비스에 바인드 요청을 진행한다
     * @param context - context
     * @param callback - 서비스 연결 콜백 함수
     */
    fun startBindService(context: Context, callback: ConnectionCallback) = ExceptionHandler.tryOrDefault() {
        Log.d(tag, "bind service start")
        stop = false
        mContext = context
        mCallback = callback
        var intent = Intent()
        intent.action = SERVICE_ACTION
        intent.setPackage(packageName)
        context.bindService(intent, mConnection, Context.BIND_AUTO_CREATE)
        Log.d(tag, "bind service")
    }

    /**
     * 바인드 된 서비스연결을 해제한다
     */
    fun stopBindService() = ExceptionHandler.tryOrDefault() {
        stop = true

        mBound?.let {
            mContext.unbindService(mConnection)
        }
    }

    /**
     * 원격 서비스에 핸들러 메시지 전송
     * @param what - 메시지 타입 MessengerConstant참조
     * @param bundle - 메시지 데이터, 콜백 등
     */
    fun send(what:Int, bundle:Bundle) = ExceptionHandler.tryOrDefault(){
        Log.d(tag, "send, $mBound, $mService")

        if (mBound && mService != null) {
            val msg = Message.obtain(null, what, 0, 0)
            msg.data = bundle
            msg.replyTo = messenger
            try {
                mService!!.send(msg)
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 원격 서비스에 핸들러 메시지 전송
     * @param what - 메시지 타입 MessengerConstant참조
     */
    fun send(what:Int) = ExceptionHandler.tryOrDefault(){
        if (mBound && mService != null) {
            val msg = Message.obtain(null, what, 0, 0)
            try {
                mService!!.send(msg)
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 원격 서비스에 연결 콜백
     */
    private val mConnection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) = ExceptionHandler.tryOrDefault(){
            // This is called when the connection with the service has been
            // established, giving us the object we can use to
            // interact with the service.  We are communicating with the
            // service using a Messenger, so here we get a client-side
            // representation of that from the raw IBinder object.
            mService = Messenger(service)
            mBound = true
            mCallback?.connected(true)
            Log.d(tag, "onServiceConnected, $mBound, $mService")
        }

        override fun onServiceDisconnected(className: ComponentName) = ExceptionHandler.tryOrDefault(){
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            mCallback = null
            mService = null
            mBound = false
            Log.d(tag, "onServiceDisconnected, $mBound, $mService")
        }
    }
}