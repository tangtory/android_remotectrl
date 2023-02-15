package com.lotte.mart.messengerlib.messenger.callback

import java.io.Serializable

/**
 * Messenger 응답 콜백 인터페이스
 */
interface ResponseCallback : Serializable {
    fun onSuccess(res:String)           //성공
    fun onResponse(msg: String)         //요청수신 응답
    fun onFail(err: String)             //실패
}