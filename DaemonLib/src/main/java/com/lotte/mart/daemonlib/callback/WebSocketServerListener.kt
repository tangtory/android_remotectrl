package com.lotte.mart.daemonlib.callback


interface WebSocketServerListener {
    fun onWsServerStatusChanged(isRunning: Boolean)
    fun onWsServerError(errorType: Int)
    fun onWsServerConnChanged(connList: List<String?>?, open:Boolean, connIp:String)
    fun onWsServerReceived(conn: String?, message: String?)
    fun onWsServerClosing(connList: List<String?>?)
}