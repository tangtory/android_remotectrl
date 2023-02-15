package com.lotte.mart.messengerlib.messenger.module.server

import android.os.Bundle
import android.os.Message
import android.os.Messenger
import android.os.RemoteException
import android.util.Log
import com.lotte.mart.commonlib.exception.ExceptionHandler

internal class MessengerManager() {

    private var mService: Messenger? = null
    private var mBound = false

    companion object {
        var tag : String = MessengerManager::class.java.simpleName
        private var messenger : Messenger? = null
    }

    init {
        initClass()
    }

    fun initClass() = ExceptionHandler.tryOrDefault(){
        if(messenger == null) {
            messenger = Messenger(MessengerHandler())
            Log.d(tag, "messenger init")
        }
    }

    /**
     * 원격지에 핸들러 메시지 전송
     * @param what - 메시지 타입 MessengerConstant참조
     * @param bd - 요청 데이터
     */
    fun send(what:Int, bundle:Bundle) = ExceptionHandler.tryOrDefault() {
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
     * 원격지에 핸들러 메시지 전송
     * @param what - 메시지 타입 MessengerConstant참조
     */
    fun send(what:Int) = ExceptionHandler.tryOrDefault() {
        if (mBound && mService != null) {
            val msg = Message.obtain(null, what, 0, 0)
            try {
                mService!!.send(msg)
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        }
    }
}