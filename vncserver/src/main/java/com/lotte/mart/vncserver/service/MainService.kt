package com.lotte.mart.vncserver.service

import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjection
import android.util.Base64
import android.util.Base64.NO_WRAP
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.core.content.ContextCompat.startActivity
import com.lotte.mart.commonlib.exception.ExceptionHandler
import com.lotte.mart.commonlib.log.Log
import com.lotte.mart.commonlib.utility.Utility
import com.lotte.mart.daemonlib.callback.WebSocketServerListener
import com.lotte.mart.daemonlib.module.imagerecorder.RecorderImage
import com.lotte.mart.daemonlib.module.sftp.SftpServerModule
import com.lotte.mart.daemonlib.module.websocket.WebSocketServerModule
import com.lotte.mart.daemonlib.service.RecordService
import com.lotte.mart.daemonlib.service.WebSocketServerService
import com.lotte.mart.messengerlib.messenger.callback.ResponseCallback
import com.lotte.mart.messengerlib.messenger.service.ClientMessengerService
import com.lotte.mart.vncserver.MessageActivity
import com.lotte.mart.vncserver.RecorderActivity
import com.lotte.mart.vncserver.data.*
import com.lotte.mart.vncserver.service.schema.Worker
import com.lotte.mart.vncserver.utils.AgentIniUtil
import java.lang.Exception
import kotlin.properties.Delegates

class MainService(ip:String, port: Int, key:String, context: Context) : Thread() {

    /**
     * 원격제어 데몬
     * @param ip - 서버 아이피
     * @param port - 서버 포트
     */
    data class Builder(var context: Context){
        lateinit var ip :String
        var port by Delegates.notNull<Int>()
        lateinit var key :String
        fun ip(ip: String) = apply { this.ip = ip}
        fun port(port: Int) = apply { this.port = port }
        fun key(key: String) = apply { this.key = key }
        fun build() = MainService(this.ip, this.port, this.key, this.context)
    }

    companion object {
        val TAG = MainService::class.java.simpleName
        var runThread : Boolean = false
        var hasClients : Boolean = false
        var _serverWebSocket : WebSocketServerService? = null
        lateinit var _context : Context
        var _recordService : RecordService? = null
        var _sftpServerModule : SftpServerModule? = null
        var bLock :Boolean = false

        fun startRecorder(mediaProjection: MediaProjection, windowManager:WindowManager){
            Log.i(TAG, "startRecorder")
            _recordService!!.setMediaProject(mediaProjection!!)
            startSFTPServer()
            val metrics = DisplayMetrics()
            windowManager.defaultDisplay.getRealMetrics(metrics)
            _recordService!!.setConfig(metrics.widthPixels, metrics.heightPixels, metrics.densityDpi)
            _recordService!!.start(_context)
        }

        fun isRecording() : Boolean{
            return (_recordService != null && _recordService!!.isRunning())
        }

        private fun startSFTPServer(){
            val sftpPort = AgentIniUtil.getInstance(_context).getVncSftpPort("2222")
            val sftpId = AgentIniUtil.getInstance(_context).getVncSftpId("lotteds")
            val sftpPwd = AgentIniUtil.getInstance(_context).getVncSftpPwd("1234")
            Log.i(TAG, "startSFTPServer, $sftpPort $sftpId")
            _sftpServerModule = SftpServerModule(_context, sftpPort.toInt(), sftpId, sftpPwd)
            _sftpServerModule!!.startServer()
        }

        fun stopSFTPServer(){
            Log.i(TAG, "stopSFTPServer")
            try {
                if(_sftpServerModule != null) {
                    synchronized(_sftpServerModule!!) {
                        _sftpServerModule!!.stopServer()
                    }
                }
            } catch (e:Exception){
                Log.e(TAG, "sftp stop exception", e)
            }
        }
    }

    init {
        _context = context
        _serverWebSocket = WebSocketServerService.Builder(_context)
            .ip(ip)
            .port(port)
            .key(key)
            .listener(websocketListener)
            .build()
        _serverWebSocket!!.create()
        Log.i(TAG, "WebSocket server start, $ip $port $key")
        _recordService = RecordService(_context)
        runThread = true
        this.start()
    }

    fun stopService(){
        Log.i(TAG, "stopService")
        runThread = false
        _serverWebSocket!!.dispose()
        _serverWebSocket = null
        _sftpServerModule!!.stopServer()
        _sftpServerModule = null
        _recordService!!.stop()
        _recordService = null
    }

    override fun run() {
        var sendIndex = 0
        var logChecked = false
        while (runThread) {
            try {
                if (_recordService!!.isRunning()) {
                    if(_serverWebSocket!!.clientList().size == 0){
                        Log.i(TAG, "Record service stop, ${_serverWebSocket!!.clientList().size}")
                        _recordService!!.stop()
                        stopSFTPServer()
                    } else {
                        sleep(50)
                        _recordService?.getRecordImage()?.let {
                            synchronized(it) {
                                val image: RecorderImage? = _recordService?.getRecordImage()
                                if (image != null) {
                                    if (image?.index!! > sendIndex) {
                                        sendIndex = image?.index
                                        sendImage(image)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    sleep(500)
                }

                checkDateChange(logChecked)
                logChecked = true
            } catch (e: Exception){
                e.printStackTrace()
            }
        }
    }

    private fun checkDateChange(firstTimeChecked : Boolean){
        var date = Log.getDate()
        if(Utility.diffOfToday(date) != 0L || !firstTimeChecked){
            if(firstTimeChecked)
                Log.i(TAG, "날짜 변경됨")

            val now = Utility.getCurrentDay()
            Log.setDate(now)
            Log.setPath("${AgentIniUtil.getInstance(_context).getLogPath(AgentIniUtil.Constant.PATH_LOCAL_LOG)}VncLog_${now}.log")
        }
    }

    fun sendImage(image: RecorderImage) = ExceptionHandler.tryOrDefault() {
        val base64Str = Base64.encodeToString(image.bitmap, NO_WRAP)
        if (_serverWebSocket?.isRunning()!!)
            _serverWebSocket?.broadcast(base64Str.toByteArray())
    }

    object websocketListener : WebSocketServerListener{
        override fun onWsServerStatusChanged(isRunning: Boolean) {
            Log.i(TAG, "onWsServerStatusChanged, $isRunning")
            val intent = Intent()
            intent.action = ForegroundService.BROADCAST_STATUS_UPDATE
            intent.putExtra("STATUS", isRunning)
            _context.sendBroadcast(intent)
        }

        override fun onWsServerError(errorType: Int) {
            Log.i(TAG, "onWsServerError, $errorType")
            when(errorType){
                WebSocketServerModule.RESULTS.ERROR_TYPE_PORT_IN_USE -> {
                    Log.i(TAG, "서버 포트가 이미 사용중입니다")
                    //이미 포트가 열려있음
                    Thread(Runnable {
                        sleep(10000)
                        if(_serverWebSocket!!.isRunning()){
                            Log.i(TAG, "WebSocket server is not released")
                            _serverWebSocket!!.dispose()
                        } else {
                            Log.i(TAG, "WebSocket server start")
                            val intent = Intent()
                            intent.action = ForegroundService.BROADCAST_STATUS_UPDATE
                            intent.putExtra("STATUS", false)
                            _context.sendBroadcast(intent)
                            _serverWebSocket!!.create()
                        }
                    }).start()
                }
            }
        }

        override fun onWsServerConnChanged(connList: List<String?>?, open : Boolean, connIp:String) {
            Log.i(TAG, "onWsServerConnChanged, ${connList.toString()}")
            if(connList.isNullOrEmpty()){
                try{
                    _recordService.let {
                        if(_recordService!!.isRunning()) {
                            Log.i(TAG, "Record service stop")
                            _recordService!!.stop()
                            stopSFTPServer()
                        }
                    }
                } catch (e:Exception){
                    Log.e(TAG, "onWsServerConnChanged exception,", e)
                    e.printStackTrace()
                }
            } else {
                hasClients = true
                if(_recordService!!.isRunning())
                    return

                val nextIntent = Intent(_context, RecorderActivity::class.java)
                startActivity(_context, nextIntent, null)
            }

            if(open){
                Worker.agentVncInfo("connected vnc client, $connIp", _context)
            } else {
                Worker.agentVncInfo("disconnected vnc client, $connIp", _context)
            }
        }

        override fun onWsServerClosing(connList: List<String?>?) {
            if(connList.isNullOrEmpty())
                hasClients = false
        }

        override fun onWsServerReceived(conn: String?, message: String?) {
            Log.i(TAG, "onWsServerReceived")
            Log.d(TAG, "$message")

            val servicePackage = "com.lotte.mart.commander"
            var parser = DataParser.Builder(_serverWebSocket!!)
                .request(message!!)
                .build()

            Log.i(TAG, "Command, ${parser.getCmdGbn()}")

            if(parser.isRequestError())
                parser.responseCmd(Response.CODE_PARSING_ERR)
            else {
                when(parser.getCmdGbn()){
                    Command.CMD_SINGLE_CLICK, Command.CMD_SLIDE, Command.CMD_LONG_CLICK,
                    Command.CMD_SHUTDOWN, Command.CMD_REBOOT, Command.CMD_POS_APP_FINISH, Command.CMD_HWKEY, Command.CMD_KBKEY -> {
                        var cmd = parser.getCommand(_context)!!
                        Log.d(TAG,"getCommand $cmd")
                        //20200121
                        ClientMessengerService.release()
                        ClientMessengerService.with(_context, servicePackage)?.cmdExec(
                            cmd,
                            object :
                                ResponseCallback {
                                override fun onSuccess(res: String) {
                                    Log.d("ClientMessengerService", "onSuccess $res")
                                    parser.responseCmd(Response.CODE_OK)
                                    ClientMessengerService.release()
                                }

                                override fun onFail(err: String) {
                                    Log.d("ClientMessengerService", "onFail $err")
                                    //failed
                                    parser.responseCmd(Response.CODE_ERR)
                                    ClientMessengerService.release()
                                }

                                override fun onResponse(msg: String) {
                                    Log.d("ClientMessengerService", "onResponse $msg")
                                }
                            })
                    }
                    Command.CMD_MSG -> {
                        val nextIntent = Intent(_context, MessageActivity::class.java)
                        nextIntent.putExtra("TYPE", Command.CMD_MSG)
                        nextIntent.putExtra("MSG", parser.getMessage())
                        nextIntent.putExtra("LOCK", bLock)
                        nextIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        startActivity(_context, nextIntent, null)
                        parser.responseCmd(Response.CODE_OK)
                    }
                    Command.CMD_LOCK -> {
                        val nextIntent = Intent(_context, MessageActivity::class.java)
                        nextIntent.putExtra("TYPE", Command.CMD_LOCK)
                        if(bLock)
                            nextIntent.putExtra("UNLOCK", true)

                        bLock = !bLock
                        nextIntent.putExtra("LOCK", bLock)
                        nextIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        startActivity(_context, nextIntent, null)
                        parser.responseCmd(Response.CODE_OK)
                    }
                    Command.CMD_QRATE -> {
                        _recordService!!.setQrate(parser.getQRateVal())
                        parser.responseCmd(Response.CODE_OK)
                    }
                    Command.CMD_SYSINFO -> {
                        parser.getSysinfo(_context)
                    }
                }
            }
        }
    }
}