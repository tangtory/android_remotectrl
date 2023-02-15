package com.lotte.mart.commonlib.exception

import com.lotte.mart.commonlib.log.Log


/**
 * Exception 발생 후 공통 처리
 */
class CommonException(){
    companion object {
        val LEVEL_NONE: Int get() = 0       //수행 내용 없음
        val LEVEL_DEBUG: Int get() = 1      //디버그 출력
        val LEVEL_LOG: Int get() = 2        //로깅 처리
        val LEVEL_SEND: Int get() = 3       //서버에 전송
        val LEVEL_EMERGENCY: Int get() = 4  //서버에 전송 및 긴급 사항 알림
        val tag : String = CommonException::class.java.simpleName

        /**
         * Exception 발생 시 예외 처리 수행
         * @param message - 예외 사항 내용
         * @param trace - 예외 사항 발생 trace
         * @param level - 예외 사항 처리 레벨
         */
        fun doException(message: String, trace: String, level: Int = 0){
            when(level){
                LEVEL_NONE -> {
                    TODO("Nothing")
                }
                LEVEL_DEBUG -> {
                    Log.e(tag, "Exception($level), $message, ${trace.toString()}")
                }
                LEVEL_LOG -> {
                    Log.e(tag, "Exception($level), $message, ${trace.toString()}")
                    TODO("Write on log file")
                }
                LEVEL_SEND -> {
                    Log.e(tag, "Exception($level), $message, ${trace.toString()}")
                    TODO("Write on log file and send to server")
                }
                LEVEL_EMERGENCY -> {
                    Log.e(tag, "Exception($level), $message, ${trace.toString()}")
                    TODO("Write on log file and send to server and alarm to manager")
                }
            }
        }
    }
}