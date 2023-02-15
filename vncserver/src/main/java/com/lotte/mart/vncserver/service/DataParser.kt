package com.lotte.mart.vncserver.service

import android.content.Context
import com.google.gson.Gson
import com.lotte.mart.commonlib.log.Log
import com.lotte.mart.daemonlib.service.WebSocketServerService
import com.lotte.mart.messengerlib.messenger.callback.ResponseCallback
import com.lotte.mart.messengerlib.messenger.service.ClientMessengerService
import com.lotte.mart.vncserver.data.*
import com.lotte.mart.vncserver.utils.AgentIniUtil
import java.lang.Exception

//VNC 서버-클라이언트간 전문 파서
class DataParser(request :String, webSocket :WebSocketServerService) {
    var requestError : Boolean = false
    var responseError : Boolean = false
    var ws : WebSocketServerService? = null
    val TAG = DataParser::class.java.simpleName
    lateinit var json : Request

    /**
     * 원격제어 데몬
     * @param webSocket - 웹소켓 서버 객체
     */
    data class Builder(var webSocket :WebSocketServerService){
        lateinit var request :String
        fun request(req: String) = apply { this.request = req}
        fun build() = DataParser(request, webSocket)
    }

    companion object {
        val TAG = DataParser::class.java.simpleName
    }

    init {
        ws = webSocket
        try {
            //요청 전문 파싱
            json = Gson().fromJson(request, Request::class.java)
        } catch (e:Exception){
            //parsing error
            requestError = true;
        }
    }

    //요청 에러 여부
    fun isRequestError() : Boolean {
        return requestError
    }

    //응답 에러 여부
    fun isResponseError() : Boolean {
        return responseError
    }

    //전문 클래스
    fun getClass() : String {
        return json.header!!.Class
    }

    //전문 함수
    fun getFunc() : String {
        return json.header!!.Func
    }

    //전문 응답 코드
    fun getRespCd() : String {
        return json.header!!.RespCd
    }

    //전문 명령 구분
    fun getCmdGbn() : String? {
        return json.requestBody!!.Command.CmdGbn
    }

    //전문 시작 x좌표
    fun getPosSx() : String? {
        return json.requestBody!!.Command.PosSx
    }

    //전문 시작 y좌표
    fun getPosSy() : String? {
        return json.requestBody!!.Command.PosSy
    }

    //전문 끝 x좌표
    fun getPosEx() : String? {
        return json.requestBody!!.Command.PosEx
    }

    //전문 끝 y좌표
    fun getPosEy() : String? {
        return json.requestBody!!.Command.PosEy
    }

    //전문 메세지
    fun getMessage() : String? {
        return json.requestBody!!.Command.Msg
    }

    //전문 물리 키 코드
    fun getHardwareKeyCode(): String? {
        when(json.requestBody!!.Command.Hwkey){
            Command.HWKEY_MENU->return "187"
            Command.HWKEY_HOME->return "3"
            Command.HWKEY_BACK->return "4"
            Command.HWKEY_SCREEN->return "26"
        }
        return ""
    }

    //전문 키보드 키 코드
    fun getKeyboardKeyCode(): String? {
        val key = json.requestBody!!.Command.Hwkey
        when(key){
            "\b"->{
                return Command.KBKEY_BACKSPACE
            }
        }

        return key
    }

    //전문 화질 값
    fun getQRateVal():Int{
        when(json.requestBody!!.Command.Qrate){
            Command.QRATE_LOW->return 5
            Command.QRATE_MID->return 45
            Command.QRATE_HIG->return 80
        }
        return 80
    }

    //시스템 정보 취득
    fun getSysinfo(context : Context) {
        var cmd = "echo core " +
                "&& cat /proc/cpuinfo | grep 'processor' " +
                "&& echo cpu " +
                "&& dumpsys cpuinfo | grep 'Load' | sed 's/Load://' | sed 's/ *//g' | sed 's%/%\\n%g'" +
                "&& echo memory " +
                "&& top -n 2 -d 1 | grep -m 1 -E 'Mem'* " +
                "&& echo battery " +
                "&& dumpsys battery | grep 'level' | sed 's/[^0-9]//g'"
        val servicePackage = "com.lotte.mart.commander"

        ClientMessengerService.release()
        ClientMessengerService.with(context, servicePackage)?.cmdExec(cmd, object: ResponseCallback {
            override fun onSuccess(res: String) {
                Log.d("ClientMessengerService","onSuccess $res")
                val sys = Sysinfo.parse(res, ws!!.clientList().size.toString())

                responseSys(Response.CODE_OK, sys.Cpu, sys.Memory, sys.Battery, sys.Usercount)
                ClientMessengerService.release()
            }

            override fun onFail(err: String) {
                Log.d("ClientMessengerService","onFail $err")
                //failed
                responseSys(Response.CODE_ERR,"-1","-1","-1","-1")
                ClientMessengerService.release()
            }

            override fun onResponse(msg: String) {
                Log.d("ClientMessengerService","onResponse $msg")
            }
        })
    }

    //시스템 정보 취득(다른방식)
    fun getSysinfo2(context : Context) {
        var cmd = "echo core " +
                "&& cat /proc/cpuinfo | grep 'processor' " +
                "&& echo cpu " +
                "&& dumpsys cpuinfo | grep 'Load' | sed 's/Load://' | sed 's/ *//g' | sed 's%/%\\n%g'" +
                "&& echo memory " +
                "&& dumpsys meminfo | grep -A 3 'Total RAM' | sed 's/K.*\$//g' | sed 's/[^0-9]//g'"+
                "&& echo battery " +
                "&& dumpsys battery | grep 'level' | sed 's/[^0-9]//g'"
        val servicePackage = "com.lotte.mart.commander"
        var infoCnt = 0
        var cpu : CpuUsage? = null
        var mem : MemUsage? = null
        var battery : Int? = null

        ClientMessengerService.release()
        ClientMessengerService.with(context, servicePackage)?.cmdExec(cmd, object:
            ResponseCallback {
            override fun onSuccess(res: String) {
                Log.d("d","onSuccess $res")
                val sys = Sysinfo.parse(res, ws!!.clientList().size.toString())

                responseSys(Response.CODE_OK, sys.Cpu, sys.Memory, sys.Battery, sys.Usercount)
//                cpu = CpuUsage.parse(res)
//                if(cpu != null)
//                    Log.d("memUsage","memUsage $cpu")
//                setSystemInfo(cpu, mem, battery)
                ClientMessengerService.release()
            }

            override fun onFail(err: String) {
                //failed
                responseSys(Response.CODE_ERR,"-1","-1","-1","-1")
                ClientMessengerService.release()
            }

            override fun onResponse(msg: String) {
                Log.d("d","onResponse $msg")
            }
        })
    }

    //명령 취득
    fun getCommand(context:Context) : String? {
        when(getCmdGbn()){
            //터치
            Command.CMD_SINGLE_CLICK-> {
                //input tap x y
                return "input tap ${getPosSx()} ${getPosSy()}"
            }
            //슬라이딩
            Command.CMD_SLIDE-> {
                //input swipe x1 y1 x2 y2 500
                return "input swipe ${getPosSx()} ${getPosSy()} ${getPosEx()} ${getPosEy()} 300"
            }
            //롱클릭
            Command.CMD_LONG_CLICK-> {
                //input swipe x1 y1 x1 y1 500
                return "input swipe ${getPosSx()} ${getPosSy()} ${getPosSx()} ${getPosSy()} 500"
            }
            //종료
            Command.CMD_SHUTDOWN-> {
                //reboot -p
                //input keyevent --longpress 26
                return "svc power shutdown"
            }
            //재부팅
            Command.CMD_REBOOT-> {
                //reboot
                //input keyevent --longpress 26
                return "svc power reboot"
            }
            //POS앱 종료
            Command.CMD_POS_APP_FINISH-> {
                //am force-stop <packagename>
                val packageName = AgentIniUtil.getInstance(context).getPosPackageName("com.lotte.mart.cloudpos")
                return if(packageName.isEmpty())
                    ""
                else
                    "am force-stop $packageName"
            }
            //시스템 정보
            Command.CMD_SYSINFO-> {
                //top -n 1
                //dumpsys battery | grep level
                return "top -n 1"
            }
            //메세지
            Command.CMD_MSG-> {
                return getMessage()
            }
            Command.CMD_LOCK-> {
                //화면 잠금
            }
            Command.CMD_QRATE-> {
                //화면 품질
            }
            //하드웨어 키
            Command.CMD_HWKEY-> {
                val key = getHardwareKeyCode()
                return "input keyevent $key"
            }
            //키보드 키
            Command.CMD_KBKEY-> {
                val key = getKeyboardKeyCode()
                when(key){
                    Command.KBKEY_BACKSPACE->{
                        return "input keyevent $key"
                    }
                }

                return "input text $key"
            }
        }
        return ""
    }

    //명령 요청 응답
    fun responseCmd(code : String){
        var response = Response()
        response.header = Header(DataParser::class.java.simpleName, this::responseCmd.name, code)
        var json = Gson().toJson(response)
        Log.i(TAG,"responseCmd, $code")
        this.ws!!.broadcast(json)
    }

    //시스템 정보 요청 응답
    fun responseSys(code : String, cpu:String, mem:String, btty:String, userCnt:String){
        var response = Response()
        response.header = Header(DataParser::class.java.simpleName, this::responseSys.name, code)
        response.responseBody = ResponseBody(Sysinfo(cpu, mem, btty, userCnt))
        var json = Gson().toJson(response)
        Log.i(TAG,"responseSys, $code")
        this.ws!!.broadcast(json)
    }
}