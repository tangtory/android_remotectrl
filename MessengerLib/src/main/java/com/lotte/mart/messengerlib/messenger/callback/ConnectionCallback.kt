package com.lotte.mart.messengerlib.messenger.callback

/**
 * Messenger 연결 콜백 인터페이스
 */
interface ConnectionCallback {
    fun connected(result: Boolean)      //연결완료
}