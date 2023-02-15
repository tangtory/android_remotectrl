package com.lotte.mart.daemonlib.module.websocket

import android.util.Log
import com.lotte.mart.commonlib.exception.ExceptionHandler
import com.lotte.mart.daemonlib.callback.WebSocketServerListener
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import java.io.IOException
import java.net.InetSocketAddress
import java.util.*

/**
 * Websocket 서버 모듈
 */
open class WebSocketServerModule(host:String, port: Int, key:String) : WebSocketServer(InetSocketAddress(host, port))  {
    var tag: String = WebSocketServerModule::class.java.simpleName
    private var mListener: WebSocketServerListener?=null
    private var connList: ArrayList<String> =  ArrayList()
    private var connSockList: ArrayList<WebSocket> =  ArrayList()
    private var isRunning : Boolean = false
    private var timerTask: Timer? = null
    private val pwd = key

    /**
     * WebSocket 에러 결과값
     */
    object RESULTS {
        const val ERROR_TYPE_NORMAL = 0             //에러
        const val ERROR_TYPE_PORT_IN_USE = 1        //사용중인 포트 에러
        const val ERROR_TYPE_SERVER_CLOSE_FAIL = 2  //서버 종료 에러
    }

    companion object{
    }

    init {
        this.connectionLostTimeout = 10
    }

    /**
     * WebSocket 서버 리스너 설정
     */
    fun setListener(listener: WebSocketServerListener) = ExceptionHandler.tryOrDefault(){
        this.mListener = listener
    }

    /**
     * WebSocket 서버 중지 시 발생 예외 처리
     */
    open fun stopWithException(){
        try {
            this.stopConnectionChecker()
            this.stop()
            isRunning = false
            this.mListener?.onWsServerStatusChanged(false)
        } catch (e: IOException) {
            e.printStackTrace()
            this.mListener?.onWsServerError(RESULTS.ERROR_TYPE_SERVER_CLOSE_FAIL) //서버 종료 실패
        } catch (e: InterruptedException) {
            e.printStackTrace()
            this.mListener?.onWsServerError(RESULTS.ERROR_TYPE_SERVER_CLOSE_FAIL) //서버 종료 실패
        } catch (e: Exception){
            e.printStackTrace()
            this.mListener?.onWsServerError(RESULTS.ERROR_TYPE_SERVER_CLOSE_FAIL) //서버 종료 실패
        }
    }

    /**
     * WebSocket 서버 중지 시 발생 예외 처리
     */
    fun disconnection(conn:WebSocket) = ExceptionHandler.tryOrDefault(){
//        this.removeConnection(conn)
        this.onClosing(conn, 1000, "", false)
    }

    /**
     * WebSocket 서버 모듈 가동 여부
     */
    open fun isRunning():Boolean {
        return isRunning
    }

    /**
     * WebSocket 접속 클라이언트 리스트
     */
    open fun clientList():ArrayList<String> {
        return connList
    }

    override fun broadcast(s:String){
        super.broadcast(s)
    }

    override fun broadcast(b:ByteArray){
        super.broadcast(b)
    }

    override fun onOpen(conn: WebSocket?, handshake: ClientHandshake?) = ExceptionHandler.tryOrDefault() {
        Log.d(tag,"onOpen, ${conn!!.resourceDescriptor}")
        //접속 url에서 접속 key 취득
        var key = getCertificateKey(conn.resourceDescriptor)
        Log.d(tag,"key, $key")
        //key값이 맞지 않을경우 접속 거부
        if(key != this.pwd){
            conn.close()
            return@tryOrDefault
        }

        //접속 아이피 취득
        val connIp = conn!!.remoteSocketAddress.address.toString().replace("/", "")
        if(connList.size == 0)
            startConnectionChecker()
        connList.add(connIp)
        connSockList.add(conn)
        mListener?.onWsServerConnChanged(connList, true, connIp)
        Log.d(tag,"onOpen: // " + connIp + " //Opened connection number  " + connList.size)
    }

    override fun onClose(conn: WebSocket?, code: Int, reason: String?, remote: Boolean) = ExceptionHandler.tryOrDefault() {
        super.onWebsocketClose(conn, code, reason, remote)
        var connIp = ""
        if(conn!!.remoteSocketAddress != null)
            connIp = conn!!.remoteSocketAddress.address.toString().replace("/", "")

        mListener?.onWsServerConnChanged(connList, false, connIp)
        Log.d(tag,"onClose: // //Closed connection number  " + connList.size)
    }

    override fun onMessage(conn: WebSocket?, message: String?) = ExceptionHandler.tryOrDefault() {
        mListener?.onWsServerReceived(conn.toString(), message)
        Log.d(tag, "onMessage: $message")
    }

    override fun onStart() = ExceptionHandler.tryOrDefault(){
        isRunning = true
        mListener?.onWsServerStatusChanged(true) //서비스가 성공적으로 시작되었습니다.
        Log.d(tag, "onStart: ")
    }

    override fun onError(conn: WebSocket?, ex: Exception?) = ExceptionHandler.tryOrDefault() {
        Log.d(tag,"onError: " + ex!!.message)
        ex.printStackTrace()
        if (ex.message != null && ex.message!!.contains("Address already in use")) {
            Log.d(tag,"ws server: 포트가 이미 사용 중입니다.")
            mListener?.onWsServerError(RESULTS.ERROR_TYPE_PORT_IN_USE) //서비스 시작에 실패했습니다. 포트가 이미 사용 중입니다. 포트를 변경하십시오
        } else {
            mListener?.onWsServerError(RESULTS.ERROR_TYPE_NORMAL)
        }
    }

    override fun onClosing(conn: WebSocket?, code: Int, reason: String?, remote: Boolean) = ExceptionHandler.tryOrDefault() {
        super.onClosing(conn, code, reason, remote)
        Log.d(tag, "onClose: ")

        if(!remote){
            if(connSockList.remove(conn)){
                connList.clear()
                for(c in connSockList){
                    val connIp = c.remoteSocketAddress.address.toString().replace("/", "")
                    connList.add(connIp)
                }
            }
        } else {
            val connIp = conn!!.remoteSocketAddress.address.toString().replace("/", "")
            for (ip in connList) {
                if (ip == connIp) {
                    connList.remove(ip)
                    connSockList.remove(conn)
                    break
                }
            }
        }

        if(connList.size == 0)
            stopConnectionChecker()
    }

    /**
     * WebSocket 연결 감시 시작
     */
    fun startConnectionChecker() = ExceptionHandler.tryOrDefault(){
        timerTask = kotlin.concurrent.timer(period = 1000) {
            for(conn in connSockList){
                if(conn.readyState == WebSocket.READYSTATE.CLOSED)
                {
                    disconnection(conn)
                }
            }
        }
    }

    /**
     * WebSocket 연결 감시 종료
     */
    fun stopConnectionChecker() = ExceptionHandler.tryOrDefault(){
        timerTask?.cancel()
    }

    /**
     * 인증 키 취득
     */
    fun getCertificateKey(value:String):String = ExceptionHandler.tryOrDefault(""){
        var params = value.split('?')
        var name = ""
        var cKey = ""
        if(params.size > 1){
            var data = params[params.size - 1].split('=')
            for(v in data){
                if(name == ""){
                    name = v
                } else {
                    if(name == "cKey"){
                        cKey = v
                    }
                }
            }
        }

        cKey
    }
}