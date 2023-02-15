package com.lotte.mart.daemonlib.module.tcpip

import com.lotte.mart.commonlib.exception.ExceptionHandler
import okio.*
import java.net.*
import java.util.concurrent.TimeUnit

open class TcpIpModule(private var ip: String?, private var port: Int? = 0) {
    var tag: String = TcpIpModule::class.java.simpleName

    object RESULTS {
        const val SUCCESS = "SUCCESS"
        const val FAIL = "FAIL"
        const val EXCEPTION_TIMEOUT = "TIMEOUT"
        const val EXCEPTION = "EXCEPTION"
    }

    companion object{
        var socket = Socket()
        lateinit var sink: Sink;
        lateinit var source: Source;
    }

    /**
     * Socket 연결을 시도한다. 연결시 초기 값 설정
     * @return 처리 결과 RESULTS 값 참조
     */
    fun connect():String{
        try{
            port?.let {
                socket = Socket(ip, it)
                sink = socket.sink().buffer()
                source = socket.source().buffer()
                sink.timeout().timeout(1, TimeUnit.SECONDS)
                source.timeout().timeout(1, TimeUnit.SECONDS)
                return RESULTS.SUCCESS
            }
            return RESULTS.FAIL;
        } catch(e:Exception){
            close()
            e.printStackTrace()
            return RESULTS.EXCEPTION
        }
    }

    /**
     * 연결된 서버로 메시지 값을 전달한다.
     * @param msg - 전달 메시지 값
     * @return 처리 결과 RESULTS 값 참조
     */
    fun send(msg:String):String{
        return try{
            var buffer = Buffer().writeUtf8(msg)
            sink.write(buffer, buffer.size)
            sink.flush()
            RESULTS.SUCCESS
        } catch (e: SocketTimeoutException) {
            close()
            e.printStackTrace()
            RESULTS.EXCEPTION_TIMEOUT
        } catch(e:Exception){
            close()
            e.printStackTrace()
            RESULTS.EXCEPTION
        }
    }

    /**
     * 요청의 응답값 수신한다.
     * @param count - 수신 데이터 길이
     * @return 처리 결과 RESULTS 값 참조
     */
    fun receive(count: Int): String {
        try{
            var buffer = Buffer()
            var len = source.read(buffer, count.toLong())
            if(buffer == null || len <= 0L)
                return ""

            var msg = buffer.readUtf8()
            return msg
        } catch (e: SocketTimeoutException) {
            close()
            e.printStackTrace()
            return RESULTS.EXCEPTION_TIMEOUT
        } catch(e:Exception){
            close()
            e.printStackTrace()
            return RESULTS.EXCEPTION
        }
    }

    /**
     * 소켓 연결 종료
     * @return 처리 결과 RESULTS 값 참조
     */
    fun close(): String{
        return try{
            source.close()
            sink.close()
            socket.close()
            RESULTS.SUCCESS
        }catch(e:Exception){
            e.printStackTrace()
            RESULTS.EXCEPTION
        }
    }

    /**
     * 소켓 연결 상태
     * @return 연결 Boolean
     */
    fun isConnected(): Boolean = ExceptionHandler.tryOrDefault(false){
        socket.isBound && !socket.isClosed && socket.isConnected && !socket.isInputShutdown && !socket.isOutputShutdown;
    }
}