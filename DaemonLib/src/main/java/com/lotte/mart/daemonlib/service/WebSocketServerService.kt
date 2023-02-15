package com.lotte.mart.daemonlib.service

import android.content.Context
import android.util.Log
import com.lotte.mart.commonlib.exception.ExceptionHandler
import com.lotte.mart.daemonlib.callback.WebSocketServerListener
import com.lotte.mart.daemonlib.module.websocket.WebSocketServerModule
import kotlin.properties.Delegates

/**
 * Websocket server 서비스
 */
class WebSocketServerService(ip: String, port: Int, key:String, listener: WebSocketServerListener) {
    var tag: String = WebSocketServerService::class.java.simpleName
    lateinit var wsServer: WebSocketServerModule

    var _ip : String
    var _port by Delegates.notNull<Int>()
    var _key :String
    var _listener : WebSocketServerListener

    /**
     * Websocket 서버 빌더
     * @param ip - 서버 아이피
     * @param port - 서버 포트
     * @param key - 접속키(비밀번호)
     * @param listener - Websocket 서비스 리스너
     */
    data class Builder(var context: Context){
        lateinit var ip: String
        var port by Delegates.notNull<Int>()
        lateinit var key :String
        lateinit var listener: WebSocketServerListener
        fun ip(ip: String) = apply { this.ip = ip }
        fun port(port: Int) = apply { this.port = port }
        fun key(key: String) = apply { this.key = key }
        fun listener(listener: WebSocketServerListener) = apply { this.listener = listener }
        fun build() = WebSocketServerService(this.ip, this.port, this.key, this.listener)
    }

    init {
        this._ip = ip
        this._port = port
        this._listener = listener
        this._key = key
    }

    /**
     * Websocket 서버 생성
     */
    fun create() = ExceptionHandler.tryOrDefault() {
        Log.d(tag, "create, $_ip, $_port")
        this.wsServer = WebSocketServerModule(_ip, _port, _key)
        this.wsServer.setListener(_listener)
        this.wsServer.connectionLostTimeout = 5
        start()
    }

    /**
     * Websocket 서버 서비스 시작
     */
    fun start() = ExceptionHandler.tryOrDefault() {
        this.wsServer.start()
    }

    /**
     * Websocket 서버 중지
     */
    fun dispose() = ExceptionHandler.tryOrDefault(){
        wsServer?.stopWithException()
    }

    /**
     * Websocket 브로드캐스트 전송
     * @param s - String형 전송 데이터
     */
    fun broadcast(s:String) = ExceptionHandler.tryOrDefault() {
        wsServer.broadcast(s)
    }

    /**
     * Websocket 브로드캐스트 전송
     * @param arr - ByteArray형 전송 데이터
     */
    fun broadcast(arr:ByteArray) = ExceptionHandler.tryOrDefault() {
        wsServer.broadcast(arr)
    }

    /**
     * Websocket 서비스 가동여부
     */
    fun isRunning():Boolean = ExceptionHandler.tryOrDefault(false) {
        wsServer.isRunning()
    }

    /**
     * Websocket 접속 클라이언트 리스트
     * @return 접속 클라이언트 아이피 리스트
     */
    fun clientList():ArrayList<String> = ExceptionHandler.tryOrDefault(ArrayList<String>()){
        wsServer.clientList()
    }
}