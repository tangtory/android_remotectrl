package com.lotte.mart.commonlib.log

/**
 * 로깅 작성 완료 리스너
 */
interface OnLogListener {
    fun onLogged(tag: String?, message: String?)
}