package com.lotte.mart.messengerlib.messenger.callback

import android.os.Message
import java.io.Serializable

/**
 * Messenger request 콜백 인터페이스
 */
interface RequestCallback : Serializable {
    fun powerOff(msg: String, result: Boolean, message: Message)
    fun reboot(msg: String, result: Boolean)
    fun appUpdate(msg: String, result: Boolean)
    fun setIpAddressBounds(msg: String, result: Boolean)
    fun cmdExec(msg: String, result: Boolean, message: Message)
    fun setTime(h: Int, m: Int, s: Int, result: Boolean, message: Message)
    fun setDateTime(Y: Int, M: Int, D: Int, h: Int, m: Int, s: Int, result: Boolean, message: Message)
}