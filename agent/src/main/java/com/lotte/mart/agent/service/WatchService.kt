package com.lotte.mart.agent.service

import android.content.Context
import android.content.Intent
import android.os.*
import android.widget.Toast
import com.lotte.mart.agent.UpdateActivity
import com.lotte.mart.agent.data.Constant
import com.lotte.mart.agent.room.database.AgentLogDatabase
import com.lotte.mart.agent.room.entity.AgentLogEntity
import com.lotte.mart.agent.data.CpuUsage
import com.lotte.mart.agent.data.MemUsage
import com.lotte.mart.agent.data.Sysinfo
import com.lotte.mart.agent.utils.AgentIniUtil
import com.lotte.mart.agent.utils.Util
import com.lotte.mart.commonlib.log.Log
import com.lotte.mart.commonlib.utility.Utility
import com.lotte.mart.messengerlib.messenger.callback.ResponseCallback
import com.lotte.mart.messengerlib.messenger.service.ClientMessengerService
import java.io.File
import java.io.IOException
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

/**
 * 시스템 및 앱 감시 서비스
 */
class WatchService(context: Context) : Thread() {
    var _context = context
    var checking: Boolean = false

    //감시 앱 아이디
    val IDEL_ID : Int get() = 0
    //VNC 감시 아이디
    val VNC_ID : Int get() = 1
    //Commander 감시 아이디
    val COMMNADER_ID : Int get() = 2
    //POS 감시 아이디
    val POS_ID : Int get() = 3

    //앱 실행 핸들러 아이디
    val APP_START : Int get() = 1
    //토스트 메시지 핸들러 아이디
    val MSG : Int get() = 2

    val PACKAGE_NAME : String get() = "PACKAGE_NAME"
    //감시 대상 앱 종료 상태 재확인 인터벌
    val RECHECK_RESTART_INTERVAL : Int = 3000
    //감시 주기
    val CHECK_INTERVAL : Int = AgentIniUtil.getInstance().getWatcherInterval("30000").toInt()
    var checkInterval : Int = CHECK_INTERVAL
    var watchTime : Long = 0L
    var lastCheckTime : Long = 0L
    var loggingTime : Long = 0L
    var retryCheckPrcs = IDEL_ID

    companion object {
        //현재 POS 앱 상태
        private var CUR_POS : String = ""
        //현재 VNC 앱 상태
        private var CUR_VNC : String = ""
        //현재 COMMANDER 앱 상태
        private var CUR_COMMANDER : String = ""
        //현재 저장소 상태
        private var CUR_STORAGE : String = ""

        var lastCommanderCheckTime : Long = 0L
        var commanderUpdateCheckTimeSet : Boolean = false
        val TAG = WatchService::class.java.simpleName
        var running : Boolean = false

        /**
         * 현재 COMMANDER 앱 가동 여부
         */
        fun isRunningCommander():Boolean{
            return CUR_COMMANDER == "ON"
        }
    }

    init {

    }

    var mHandler = object : Handler(Looper.getMainLooper()){
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when(msg.what){
                APP_START -> {
                    if(!UpdateService.isUpdating() && !UpdateService.isUpdatingCommander()) {
                        var packageName = msg.data.getString(PACKAGE_NAME)
                        Log.i(TAG, "App start, $packageName")
                        try {
                            val intent =
                                _context.packageManager.getLaunchIntentForPackage(packageName!!)
                            intent!!.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                            _context.startActivity(intent)
                        }catch (e:Exception){
                            e.printStackTrace()
                        }
                    } else {
                        if(UpdateService.isUpdatingCommander()) {
                            var result = Constant.RESULT_FAIL
                            if(Utility.getInstallPackage("com.lotte.mart.commander", _context)) {
                                if(Util.getCurrentCmdVersion() == Utility.getVersionInfo("com.lotte.mart.commander",_context)){
                                    result = Constant.RESULT_SUCCESS
                                }
                            }
                            val intent = Intent()
                            intent.action = UpdateActivity.BROADCAST_FINISHED_UPDATE
                            intent.putExtra("RES", result)
                            intent.putExtra("TYPE", "COMMANDER")
                            _context.sendBroadcast(intent)

                            var packageName = msg.data.getString(PACKAGE_NAME)
                            Log.i(TAG, "App start, $packageName")
                            try {
                                val intent =
                                    _context.packageManager.getLaunchIntentForPackage(packageName!!)
                                intent!!.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                                _context.startActivity(intent)
                            }catch (e:Exception){
                                e.printStackTrace()
                            }
                            commanderUpdateCheckTimeSet = false
                        }
                    }
                }
                MSG -> {
                    Toast.makeText(_context, ""+msg.obj as String, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    /**
     * 감시 서비스 시작
     */
    fun startService(){
        Log.i(TAG, "start WatchService")
        running = true
        this.start()
    }

    /**
     * 감시 서비스 종료
     */
    fun stopService(){
        Log.i(TAG, "stop WatchService")
        running = false
        this.join(0)
    }

    override fun run() {
        var logChecked = false
        Log.i(TAG, "Watcher service is running")
        while (running) {
            try {
                //저장소 상태 확인
                if(!storageCheck(10.toDouble())){
                    //Storage is full
                    Log.d(TAG, "Storage is full")
                }

                //업데이트중이 아닐경우
                if(!UpdateService.isUpdating() && !UpdateService.isUpdatingCommander()) {
                    if (Utility.diffOfCurrTime(loggingTime, CHECK_INTERVAL)) {
                        Log.i(TAG, "check system log, loggingtime $loggingTime")
                        loggingTime = System.currentTimeMillis()
                        Log.i(TAG, "check system log, curtime $loggingTime")
                        //날짜 변경 확인
                        checkDateChange(logChecked)
                        //시스템 감시 로그 작성
                        setSystemWatcherLog()
                        logChecked = true
                        //ini파일 확인
                        if(!checkIniFiles()){
                            val m = mHandler.obtainMessage()
                            m.what = MSG
                            m.obj = "ini파일을 찾을 수 없습니다.\n수정 후 PDA를 재시작 해 주시기 바랍니다."
                            mHandler.sendMessage(m)
                        }
                    }

                    try {
                        //감시 작업 수행
                        watcher()
                        sleep(1000)
                    } catch (e:IOException) {
//                    Log.d(TAG, "Watch error", e)
                        sleep(600000)    //io exception 발생할 경우 1분단위로 확인
                        e.printStackTrace()
                    }
                } else {
                    if(!commanderUpdateCheckTimeSet){
                        if(UpdateService.isUpdatingCommander()){
                            commanderUpdateCheckTimeSet = true
                            lastCommanderCheckTime = System.currentTimeMillis()
                        }
                    } else {
                        if (lastCommanderCheckTime == 0L)
                            lastCommanderCheckTime = System.currentTimeMillis()
                    }
                    //Commander업데이트 중일 경우 앱 감시(업데이트 완료 여부 확인)
                    commanderWatcher()
                }
                sleep(1000)
            } catch (e: InterruptedException){
                Log.i(EmsService.TAG, "WatchService thread interrupted", e)
            } catch (e: Exception){
                e.printStackTrace()
                sleep(600000)    //exception 발생 시 1분단위로 확인
                Log.e(TAG, "Watcher error", e)
            }
        }

        Log.i(TAG, "Watcher service stopped")
    }

    /**
     * 저장소 확인
     * @param min - 최소 메모리 %
     */
    private fun storageCheck(min: Double):Boolean{
        val totalStorage = checkInternalStorageAllMemory()
        val availableStorage = checkInternalAvailableMemory()

        var percent = ((availableStorage.toDouble()/totalStorage.toDouble()) * 100.0 )
        var res = (percent * 10).roundToInt() /10f
        CUR_STORAGE = res.toString()
        return res > min
    }

    /**
     * 전체 내부 저장소 메모리 확인
     */
    private fun checkInternalStorageAllMemory(): Long {
        val stat = StatFs(Environment.getDataDirectory().path)
        val blockSize = stat.blockSizeLong
        val totalBlocks = stat.blockCountLong

        return blockSize * totalBlocks
    }

    /**
     * 사용가능한 내부 저장소 메모리 확인
     */
    fun checkInternalAvailableMemory(): Long {
        val stat = StatFs(Environment.getDataDirectory().path)
        val blockSize = stat.blockSizeLong
        val availableBlocks = stat.availableBlocksLong

        return blockSize * availableBlocks
    }

    /**
     * Commander앱 감시(업데이트용)
     */
    fun commanderWatcher(){
        val servicePackage = "com.lotte.mart.commander"
        //timeout check
        if (Utility.diffOfCurrTime(lastCommanderCheckTime, 20000)) {
            Log.i(TAG, "commander 응답없음")
            //commnader start
            checking = false
            watchTime = 0L
            //restart commander
            val m = mHandler.obtainMessage()
            var bundle = Bundle()
            bundle.putString(PACKAGE_NAME, servicePackage)
            m.what = APP_START
            m.obj = "start commnader"
            m.data = bundle
            mHandler.sendMessage(m)
            //Commander Timeout 프로세스 종료 로그
        }
    }

    /**
     * 앱 감시 작업 수행
     */
    fun watcher(){
        val servicePackage = "com.lotte.mart.commander"
        val cmd = "dumpsys activity | grep 'Proc #' | grep -v 'cch-empty' | grep 'com.lotte.mart.*'"
        val posPackage = AgentIniUtil.getInstance().getPosPackageName("com.lotte.mart.cloudpos")
        val vncPackage = AgentIniUtil.getInstance().getVncPackageName("com.lotte.mart.vncserver")
        val commanderPackage = "com.lotte.mart.commander"
        val posWatch = AgentIniUtil.getInstance().getPosWatch("0")

        if (!checking) {
            if(Utility.diffOfCurrTime(watchTime, checkInterval)) {
                watchTime = System.currentTimeMillis()
                lastCheckTime = System.currentTimeMillis()
                checking = true
                Log.d(TAG, cmd)
                ClientMessengerService.release()
                ClientMessengerService.with(_context, servicePackage)?.cmdExec(
                    cmd,
                    object :
                        ResponseCallback {
                        override fun onSuccess(res: String) {
                            Log.d(TAG, "onSuccess")
                            //VNC 감시 결과
                            if (res.contains(vncPackage, true)) {
                                Log.i(TAG, "$vncPackage is running")
                                CUR_VNC = "ON"
                            } else {
                                Log.i(TAG, "$vncPackage dead")
                                CUR_VNC = "OFF"
                                if (retryCheckPrcs == IDEL_ID) {
                                    checkInterval = RECHECK_RESTART_INTERVAL
                                    retryCheckPrcs = VNC_ID
                                    watchTime = System.currentTimeMillis()
                                } else if (retryCheckPrcs == VNC_ID) {
                                    watchTime = System.currentTimeMillis()
                                    checkInterval = CHECK_INTERVAL
                                    retryCheckPrcs = IDEL_ID
                                    val m = mHandler.obtainMessage()
                                    val bundle = Bundle()
                                    bundle.putString(PACKAGE_NAME, vncPackage)
                                    m.what = APP_START
                                    m.obj = "start vncserver"
                                    m.data = bundle
                                    mHandler.sendMessage(m)

                                    //VNC 프로세스 종료 로그
                                    agentLog(vncPackage, "Vnc Restart")
                                }
                            }

                            //Commander감시 결과
                            if (res.contains(commanderPackage, true)) {
                                Log.i(TAG, "$commanderPackage is running")
                                CUR_COMMANDER = "ON"
                            } else {
                                Log.i(TAG, "$commanderPackage dead")
                                CUR_COMMANDER = "OFF"

                                if (retryCheckPrcs == IDEL_ID) {
                                    checkInterval = RECHECK_RESTART_INTERVAL
                                    retryCheckPrcs = COMMNADER_ID
                                    watchTime = System.currentTimeMillis()
                                } else if (retryCheckPrcs == COMMNADER_ID) {
                                    watchTime = System.currentTimeMillis()
                                    checkInterval = CHECK_INTERVAL
                                    retryCheckPrcs = IDEL_ID
                                    val m = mHandler.obtainMessage()
                                    var bundle = Bundle()
                                    bundle.putString(PACKAGE_NAME, commanderPackage)
                                    m.what = APP_START
                                    m.obj = "start commander"
                                    m.data = bundle
                                    mHandler.sendMessage(m)
                                    //Commander 프로세스 종료 로그
                                    agentLog(commanderPackage, "Commander Restart")
                                }
                            }

                            //POS앱 감시 결과
                            if(posWatch == "1") {
                                if (res.contains(posPackage, true)) {
                                    Log.i(TAG, "$posPackage is running")
                                    CUR_POS = "ON"
                                } else {
                                    Log.i(TAG, "$posPackage dead")
                                    CUR_POS = "OFF"
                                    if (retryCheckPrcs == IDEL_ID) {
                                        checkInterval = RECHECK_RESTART_INTERVAL
                                        retryCheckPrcs = POS_ID
                                        watchTime = System.currentTimeMillis()
                                    } else if (retryCheckPrcs == POS_ID) {
                                        watchTime = System.currentTimeMillis()
                                        checkInterval = CHECK_INTERVAL
                                        retryCheckPrcs = IDEL_ID
                                        if(AgentIniUtil.getInstance()!!.getPosAutoStart("0") == "1") {
                                            val m = mHandler.obtainMessage()
                                            var bundle = Bundle()
                                            bundle.putString(PACKAGE_NAME, posPackage)
                                            m.what = APP_START
                                            m.obj = "start pos"
                                            m.data = bundle
                                            mHandler.sendMessage(m)
                                        }
                                        //POS 프로세스 종료 로그
                                        agentLog(posPackage, "Pos Restart")
                                    }
                                }
                            } else {
                                if(retryCheckPrcs == POS_ID) {
                                    watchTime = System.currentTimeMillis()
                                    checkInterval = CHECK_INTERVAL
                                    retryCheckPrcs = IDEL_ID
                                }
                            }
                            checking = false
                            ClientMessengerService.release()
                        }

                        override fun onFail(err: String) {
                            Log.d(TAG, "onFail, $err")
                            checking = false
                            //restart commander
                            val m = mHandler.obtainMessage()
                            var bundle = Bundle()
                            bundle.putString(PACKAGE_NAME, commanderPackage)
                            m.what = APP_START
                            m.obj = "$err"
                            m.data = bundle
                            mHandler.sendMessage(m)
                            ClientMessengerService.release()
                        }

                        override fun onResponse(msg: String) {
                            Log.d(TAG, "onResponse $msg")
                        }
                    })
            }
        } else {
            //timeout check
            if (Utility.diffOfCurrTime(lastCheckTime, RECHECK_RESTART_INTERVAL)) {
                Log.i(TAG, "commander 응답없음")
                //commnader start
                checking = false
                lastCheckTime = 0L
                watchTime = 0L
                //restart commander
                val m = mHandler.obtainMessage()
                var bundle = Bundle()
                bundle.putString(PACKAGE_NAME, commanderPackage)
                m.what = APP_START
                m.obj = "start commnader"
                m.data = bundle
                mHandler.sendMessage(m)
                //Commander Timeout 프로세스 종료 로그
            }
        }
    }

    /**
     * ini파일 검사
     */
    fun checkIniFiles():Boolean{
        return !(!Util.isExistFile(AgentIniUtil.Constant.FILE_SETTING_PATH))
    }

    /**
     * 시스템 상태 확인
     */
    fun setSystemWatcherLog() {

        var cmd = "echo core " +
                "&& cat /proc/cpuinfo | grep 'processor' " +
                "&& echo cpu " +
                "&& dumpsys cpuinfo | grep 'Load' | sed 's/Load://' | sed 's/ *//g' | sed 's%/%\\n%g'" +
                "&& echo memory " +
                "&& top -n 2 -d 1 | grep -m 1 -E 'Mem'* " +
                "&& echo battery " +
                "&& dumpsys battery | grep 'level' | sed 's/[^0-9]//g'"
        val servicePackage = "com.lotte.mart.commander"
        var infoCnt = 0
        var cpu : CpuUsage? = null
        var mem : MemUsage? = null
        var battery : Int? = null

        ClientMessengerService.release()
        ClientMessengerService.with(_context, servicePackage)?.cmdExec(cmd, object: ResponseCallback {
            override fun onSuccess(res: String) {
                val sys = Sysinfo.parse(res, CUR_STORAGE, CUR_POS, CUR_VNC, CUR_COMMANDER)
                Log.i(TAG,"$sys")
                ClientMessengerService.release()
            }

            override fun onFail(err: String) {
                //failed
                Log.d(TAG,"onFail $err")
                ClientMessengerService.release()
            }

            override fun onResponse(msg: String) {
                Log.d(TAG,"onResponse $msg")
            }
        })
    }

    fun setSystemWatcherLog2(){
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
        ClientMessengerService.with(_context, servicePackage)?.cmdExec(cmd, object:
            ResponseCallback {
            override fun onSuccess(res: String) {
                Log.d("d","onSuccess $res")
                val sys = Sysinfo.parse(res, CUR_STORAGE, CUR_POS, CUR_VNC, CUR_COMMANDER)
                ClientMessengerService.release()
            }

            override fun onFail(err: String) {
                //failed

                ClientMessengerService.release()
            }

            override fun onResponse(msg: String) {
                Log.d("d","onResponse $msg")
            }
        })
    }

    /**
     * Agent로그 작성
     * @param packageName - 패키지명
     * @param info - 로그내용
     */
    private fun agentLog(packageName:String, info:String){
        Log.i(TAG, "logging, $packageName $info")
        Thread(Runnable {
            try {
                var cal = Calendar.getInstance()
                AgentLogDatabase.getInstance(_context).agentLogDao().insert(
                    AgentLogEntity(
                        SEQ_NO = null,
                        SALE_DATE = Util.getCurrentDateTime("yyyyMMdd"),
                        BUSI_TYPE = "8",
                        STR_CD = Util.getStrCd(),
                        SYS_ID = Util.getPosNo(),
                        PROC_CD= UpdateService.Constant.EVT_PROC_CD,
                        EVT_CD = UpdateService.Constant.EVT_CODE_POS_ERROR,
                        CUR_LOC ="PS",
                        EVT_GBN = UpdateService.Constant.EVT_GBN,
                        EVT_STAT_GBN = UpdateService.Constant.EVT_STAT,
                        ERR_LVL = UpdateService.Constant.EVT_ERR_LVL,
                        APP_ERR_CMT =info,
                        DET_CMT = "[$packageName]",
                        CUR_DATE =SimpleDateFormat("yyyyMMdd", Locale.KOREA).format(cal.time),
                        CUR_TIME =SimpleDateFormat("HHmmss", Locale.KOREA).format(cal.time),
                        SND_GB = "",
                        RCV_DATE = "",
                        SND_DATE = ""
                    )
                )
            }
            catch (e:Exception){
                e.printStackTrace()
                Log.e(TAG, "logging error, $packageName $info", e)
            } finally {
                AgentLogDatabase.close()
            }
        }).start()
    }

    /**
     * 날짜 변경 확인
     * @param firstTimeChecked -  앱실행후 처음 확인여부
     */
    private fun checkDateChange(firstTimeChecked : Boolean){
        var date = Log.getDate()
        if(Utility.diffOfToday(date) != 0L || !firstTimeChecked){
            if(firstTimeChecked)
                Log.i(TAG, "날짜 변경됨")

            val now = Utility.getCurrentDay()
            Log.setDate(now)
            Log.setPath("${AgentIniUtil.getInstance().getLogPath(AgentIniUtil.Constant.PATH_LOCAL_LOG)}AgentLog_${now}.log")
            checkLogDays()
        }
    }

    /**
     * 로그 만료일 검사
     */
    private fun checkLogDays(){
        var cal = Calendar.getInstance()
        var todayMil = cal.timeInMillis
        var oneDayMil = 24*60*60*1000

        var fileCal = Calendar.getInstance()
        var fileDate:Date? = null

        var path = File(AgentIniUtil.getInstance().getLogPath(AgentIniUtil.Constant.PATH_LOCAL_LOG))
        var list = path.listFiles()
        val days = AgentIniUtil.getInstance().getLogDays().toInt()
        Log.i(TAG, "삭제 로그 체크")
        for (log in list) {
            if(!log.name.contains("Agent") && !log.name.contains("Vnc"))
                continue

            var logName = log.name.split(".")[0].split("_")
            if(logName.size < 2)
                continue

            val date = logName[1]
            fileDate = SimpleDateFormat("yyyyMMdd", Locale.KOREA).parse(date)
            fileCal.time = fileDate
            var diffMil = todayMil - fileCal.timeInMillis
            var diffDay = (diffMil/oneDayMil)

            if(diffDay > days && log.exists()){
                log.delete()
                Log.d(TAG, "${log.name} 파일을 삭제했습니다.")
            }
        }

        val dbDays = AgentIniUtil.getInstance().getDbLogDays().toInt()
        cal.add(Calendar.DATE, -dbDays)
        var df = SimpleDateFormat("yyyyMMdd", Locale.KOREA)
        var logDay = df.format(cal.time)
        AgentLogDatabase.getInstance(_context).agentLogDao().deleteOverDate(logDay)
        AgentLogDatabase.close()
        Log.i(TAG, "AGENT 데이터베이스 로그 삭제")
    }
}