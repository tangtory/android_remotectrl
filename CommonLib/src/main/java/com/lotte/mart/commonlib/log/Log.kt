package com.lotte.mart.commonlib.log

import android.util.Log
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * 로그 파일 작성
 */
class Log {
    companion object{
        private val VERBOSE = "V"
        private val DEBUG = "D"
        private val INFO = "I"
        private val WARN = "W"
        private val ERROR = "E"

        private val TAG = "Log"

        private var DEBUG_ENABLED = false
        private var LOG_DATE = ""
        private var PATH = "ApplicationLog.txt"
        private var logListener: OnLogListener? = null

        /**
         * 디버그 모드 설정
         * @param debug - 디버그 여부
         */
        fun setDebug(debug: Boolean) {
            DEBUG_ENABLED = debug
        }

        /**
         * 디버그 모드 설정
         * @param date - 로깅 일자(파일명)
         */
        fun setDate(date: String) {
            LOG_DATE = date
        }

        /**
         * 디버그 모드 설정
         * @return 현재 설정된 로깅일자
         */
        fun getDate():String {
            return LOG_DATE
        }

        /**
         * 디버그 모드 설정
         * @param path - 로그 경로 설정(디렉토리 경로만 있을 경우 AgentLog.log에 저장)
         */
        fun setPath(path: String) {
            PATH = if (path.endsWith("/")) {
                "${path}AgentLog.log"
            } else {
                path
            }
        }

        /**
         * 로깅 작성 완료 리스너
         * @param listener - 로그 작성 완료 리스너
         */
        fun setLogListener(listener: OnLogListener?) {
            logListener = listener
        }

        fun e(tag: String?, message: String) {
            val logResult = Log.e(tag, message)
            if (logResult > 0) logToFile(ERROR, tag, message)
        }

        fun e(tag: String?, message: String, error: Throwable?) {
            val logResult = Log.e(tag, message, error)
            if (logResult > 0) logToFile(
                ERROR,
                tag,
                "$message\r\n${Log.getStackTraceString(error)}"
            )
        }

        fun v(tag: String?, message: String) {
            val logResult = Log.v(tag, message)
            if (logResult > 0) logToFile(VERBOSE, tag, message)
        }

        fun v(tag: String?, message: String, error: Throwable?) {
            val logResult = Log.v(tag, message, error)
            if (logResult > 0) logToFile(
                VERBOSE,
                tag,
                "$message\r\n${Log.getStackTraceString(error)}"
            )
        }

        fun d(tag: String?, message: String) {
            if (DEBUG_ENABLED) {
                val logResult = Log.d(tag, message)
                if (logResult > 0) logToFile(DEBUG, tag, message)
            }
        }

        fun d(tag: String?, message: String, error: Throwable?) {
            if (DEBUG_ENABLED) {
                val logResult = Log.d(tag, message, error)
                if (logResult > 0) logToFile(
                    DEBUG,
                    tag,
                    "$message\r\n${Log.getStackTraceString(error)}"
                )
            }
        }

        fun i(tag: String?, message: String) {
            val logResult = Log.i(tag, message)
            if (logResult > 0) logToFile(INFO, tag, message)
        }

        fun i(tag: String?, message: String, error: Throwable?) {
            val logResult = Log.i(tag, message, error)
            if (logResult > 0) logToFile(
                INFO,
                tag,
                "$message\r\n${Log.getStackTraceString(error)}"
            )
        }

        fun w(tag: String?, message: String) {
            if (DEBUG_ENABLED) {
                val logResult = Log.w(tag, message)
                if (logResult > 0) logToFile(WARN, tag, message)
            }
        }

        fun w(tag: String?, message: String, error: Throwable?) {
            if (DEBUG_ENABLED) {
                val logResult = Log.w(tag, message, error)
                if (logResult > 0) logToFile(
                    WARN,
                    tag,
                    "$message\r\n${Log.getStackTraceString(error)}"
                )
            }
        }

        fun isLoggable(string: String?, num: Int): Boolean {
            return true
        }

        /**
         * 로깅 일시값 취득
         * @return - 로깅 일시[yyyyMMdd HH:mm:ss.SSS]
         */
        private fun getDateTimeStamp(): String? {
            val format = SimpleDateFormat("yyyyMMdd HH:mm:ss.SSS")
            val dateNow = Calendar.getInstance().time
            return "[" + format.format(dateNow) + "]"
        }

        /**
         * 로그 작성
         */
        private fun logToFile(
            level: String,
            tag: String?,
            message: String
        ) {
            try {

                val logFile = File(PATH)
                if (!logFile.exists()) {
                    logFile.parentFile.mkdirs()
                    logFile.createNewFile()
                    return
                }


                val ste =
                    Thread.currentThread().stackTrace[4]
                val sb = StringBuilder()
                sb.append(ste.fileName)
                sb.append(":")
                sb.append(ste.lineNumber)
                sb.append(">")
                val writer =
                    BufferedWriter(FileWriter(logFile, true))
                writer.write(
                    String.format(
                        "%s [%s]%s %s\r\n",
                        getDateTimeStamp(),
                        level,
                        sb,
//                        tag,
                        message
                    )
                )
                writer.close()
                if (logListener != null) {
                    logListener!!.onLogged(tag, message)
                }
            } catch (e: IOException) {
                Log.e(TAG, "Unable to log exception to file.", e)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}