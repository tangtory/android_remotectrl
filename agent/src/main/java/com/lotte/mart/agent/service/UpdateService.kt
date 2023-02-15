package com.lotte.mart.agent.service

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.widget.Toast
import com.lotte.mart.agent.UpdateActivity
import com.lotte.mart.agent.data.Constant.RESULT_FAIL
import com.lotte.mart.agent.room.database.AgentLogDatabase
import com.lotte.mart.agent.room.entity.AgentLogEntity
import com.lotte.mart.agent.service.UpdateService.Constant.EVT_CODE_POS_ERROR
import com.lotte.mart.agent.service.UpdateService.Constant.EVT_ERR_LVL
import com.lotte.mart.agent.service.UpdateService.Constant.EVT_GBN
import com.lotte.mart.agent.service.UpdateService.Constant.EVT_PROC_CD
import com.lotte.mart.agent.service.UpdateService.Constant.EVT_STAT
import com.lotte.mart.agent.service.schema.Worker
import com.lotte.mart.agent.utils.AgentIniUtil
import com.lotte.mart.agent.utils.Util
import com.lotte.mart.commonlib.log.Log
import com.lotte.mart.commonlib.utility.Utility
import com.lotte.mart.messengerlib.messenger.callback.ResponseCallback
import com.lotte.mart.messengerlib.messenger.service.ClientMessengerService
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.text.SimpleDateFormat
import java.util.*
import java.util.zip.ZipFile

/**
 * 앱 업데이트 서비스
 */
class UpdateService(context: Context) : Thread() {
    object Constant{
        //루트 경로
        val ROOT : String get() = Environment.getExternalStorageDirectory().path

        const val EVT_PROC_CD = "1321"
        const val EVT_CODE_POS_ERROR = "0233"
        const val EVT_GBN = "2"
        const val EVT_STAT = "0"
        const val EVT_ERR_LVL = "1"

        const val FILE_ROOT_PATH = "/lottepos"
        const val FILE_DIR_DOWNLOAD = "/download"
        const val FILE_DIR_BACKUP = "/backup"
        //업데이트 경로
        const val FILE_APK_PATH = FILE_ROOT_PATH + FILE_DIR_DOWNLOAD
        //업데이트 백업 파일 경로
        const val BACKUP_PATH = FILE_ROOT_PATH + FILE_DIR_BACKUP

    }

    var runThread = false
    var _context = context

    //업데이트 체크 인터벌`
    var updateCheckDelay:Int = AgentIniUtil.getInstance().getUpdateInterval("120000").toInt()
    //POS앱 종료 체크 시 Commander 응답 여부
    var response = false
    //POS앱 종료 여부
    var appFinish = false
    //POS앱 첫 업데이트 시도 여부
    var triedPosUpdateFirstTime = false
    //Agent앱 첫 업데이트 시도 여부
    var triedAgentUpdateFirstTime = false
    //Vnc앱 첫 업데이트 시도 여부
    var triedVncUpdateFirstTime = false
    //Commander앱 첫 업데이트 시도 여부
    var triedCmdUpdateFirstTime = false
    //ini 첫 업데이트 시도 여부
    var triedIniUpdateFirstTime = false


    companion object {
        val TAG = UpdateService::class.java.simpleName
        var updatingCommander:Boolean = false   //Commander 업데이트 진행중 여부
        var updating:Boolean = false    //업데이트 진행중 여부

        /**
         * 업데이트 진행 설정
         */
        fun setUpdate(){
            updating = true
        }

        /**
         * 업데이트 완료 설정
         */
        fun setUpdateFinish(){
            updating = false
        }

        /**
         * 업데이트 진행중 여부
         */
        fun isUpdating():Boolean{
            return updating
        }

        /**
         * Commander 업데이트 설정
         */
        fun setUpdateCommander(){
            updatingCommander = true
        }

        /**
         * Commander 업데이트 완료
         */
        fun setUpdateFinishCommander(){
            updatingCommander = false
        }

        /**
         * Commander 업데이트 진행중 여부
         */
        fun isUpdatingCommander():Boolean{
            return updatingCommander
        }

        /**
         * Agent로그 작성
         */
        fun agentLog(context: Context, packageName:String, info:String){
            Log.i(TAG, "logging, $packageName $info")
            Thread(Runnable {
                try {
                    var cal = Calendar.getInstance()

                    AgentLogDatabase.getInstance(context).agentLogDao().insert(
                        AgentLogEntity(
                            SEQ_NO = null,
                            SALE_DATE = Util.getCurrentDateTime("yyyyMMdd"),
                            BUSI_TYPE = "8",
                            STR_CD = Util.getStrCd(),
                            SYS_ID = Util.getPosNo(),
                            PROC_CD = EVT_PROC_CD,
                            EVT_CD = EVT_CODE_POS_ERROR,
                            CUR_LOC = "PS",
                            EVT_GBN = EVT_GBN,
                            EVT_STAT_GBN = EVT_STAT,
                            ERR_LVL = EVT_ERR_LVL,
                            APP_ERR_CMT =info,
                            DET_CMT=  "[$packageName]",
                            CUR_DATE = SimpleDateFormat("yyyyMMdd", Locale.KOREA).format(cal.time),
                            CUR_TIME = SimpleDateFormat("HHmmss", Locale.KOREA).format(cal.time),
                            SND_GB = "",
                            RCV_DATE = "",
                            SND_DATE = ""
                        )
                    )
                }
                catch (e: Exception){
                    e.printStackTrace()
                    Log.e(TAG, "logging error, $packageName $info", e)
                } finally {
                    AgentLogDatabase.close()
                }

            }).start()
        }
    }

    /**
     * 토스트 메시지 핸들러
     */
    var mHandler = object : Handler(Looper.getMainLooper()){
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when(msg.what){
                1-> Toast.makeText(_context, ""+msg.obj as String, Toast.LENGTH_LONG).show()
            }
        }
    }



    /**
     * 업데이트 서비스 시작
     */
    fun startUpdateService(){
        Log.i(TAG, "start UpdateService")
        runThread = true
        this.start()
    }

    /**
     * 업데이트 서비스 종료
     */
    fun stopUpdateService(){
        Log.i(TAG, "stop UpdateService")
        runThread = false
        this.join(0)
    }

    override fun run() {
        Log.i(TAG, "Update service is running")
        var posUpdatetime = 0L      //pos 업데이트 체크 시간 설정
        var agentUpdatetime = 0L    //Agent 업데이트 체크 시간설정
        while (runThread) {
            sleep(500)
            try {
                if (updating) {
                    //업데이트 완료 여부 2초 체크
                    Log.d(TAG, "업데이트 진행중...")
                    sleep(1000)
                } else {
                    if (Utility.diffOfCurrTime(agentUpdatetime, updateCheckDelay)) {
                        Log.i(TAG, "업데이트 검사")
                        //앱 시작 후 처음 앱 업데이트 검사 여부
                        if (!triedAgentUpdateFirstTime || !triedVncUpdateFirstTime || !triedCmdUpdateFirstTime) {
                            updateCheckDelay = 1000

                            agentUpdatetime = System.currentTimeMillis()

                            //업데이트 작업 실행
                            if (!doWorkUpdate()) {
                                setUpdateFinish()
                                Log.i(TAG, "Update 확인 종료")
                            }

                        } else {
                            updateCheckDelay = AgentIniUtil.getInstance().getUpdateInterval("120000")
                                .toInt()
                            agentUpdatetime = System.currentTimeMillis()
                        }
                    } else {
                        //포스 업데이트 확인 (Agent -> Vnc -> Commander순으로 업데이트 처리 후 포스앱 업데이트 시작)
                        if (triedAgentUpdateFirstTime && triedVncUpdateFirstTime && triedCmdUpdateFirstTime && !isUpdatingCommander()) {
                            if (Utility.diffOfCurrTime(posUpdatetime, 1000)) {
                                if (!updating) {
                                    posUpdatetime = System.currentTimeMillis()
                                    var posUpdate = Util.getPosUpdate()
                                    if (!posUpdate.isNullOrEmpty()) {
                                        Log.i(TAG, "POS 업데이트 시작")
                                        checkProcess()
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (e: InterruptedException){
                Log.e(TAG, "Update Service interrupted", e)
            } catch (e: Exception){
                Log.e(TAG, "Update Service error", e)
                e.printStackTrace()
                setUpdateFinish()
                sleep(60000) //exception 발생 시 1분단위로 확인
            }
        }
        Log.i(TAG, "Update Service thread stopped")
    }

    /**
     * 업데이트 작업 수행
     */
    fun doWorkUpdate(): Boolean{

        // 1. version.ini 체킹
            val agentLast = Util.getCurrentAgentVersion()
            val agentUpdateInfo = Util.getUpdateAgentVersion()

            Log.i(TAG, "AGENT 업데이트 정보, 현재 버전 : $agentLast, 업데이트 버전 : $agentUpdateInfo")

            val vncLast = Util.getCurrentVncVersion()
            val vncUpdateInfo = Util.getUpdateVncVersion()
            Log.i(TAG, "VNC 업데이트 정보, 현재 버전 : $vncLast, 업데이트 버전 : $vncUpdateInfo")

            val cmdLast = Util.getCurrentCmdVersion()
            val cmdUpdateInfo = Util.getUpdateCmdVersion()
            Log.i(TAG, "CMD 업데이트 정보, 현재 버전 : $cmdLast, 업데이트 버전 : $cmdUpdateInfo")

            //업데이트 검사 최초 한번
            if (!triedAgentUpdateFirstTime) {
                triedAgentUpdateFirstTime = true

                //Agent 버전 체크
                if(agentUpdateInfo.isNullOrEmpty() || agentLast.isNullOrEmpty()) {
                    return false
                }

                if (hasUpdateVersion(
                        agentLast,
                        agentUpdateInfo
                    )
                ) {
                    Log.d(
                        TAG,
                        "Tried update of Agent application, $triedAgentUpdateFirstTime"
                    )
                    setUpdate()
                    var intent = Intent(_context, UpdateActivity::class.java)
                    intent.putExtra("MSG", "AGENT 업데이트 시작 $agentLast -> $agentUpdateInfo")
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    _context.startActivity(intent)

                    Log.i(TAG, "AGENT 업데이트 시작")

                        Log.i(TAG, "AGENT 업데이트 파일 다운로드 완료")
                        val i = Intent()
                        i.action = UpdateActivity.BROADCAST_STATUS_UPDATE
                        i.putExtra("MSG", "AGENT 업데이트 파일 다운로드 완료")
                        _context.sendBroadcast(i)

                        val backup = doBackupApk("$agentLast.apk")
                        if (!backup) {
                            Log.e(TAG, "AGENT 업데이트 종료")
                            val i = Intent()
                            i.action = UpdateActivity.BROADCAST_ERROR_UPDATE
                            i.putExtra("MSG", "AGENT 업데이트 종료 (백업 에러)")
                            _context.sendBroadcast(i)
                            return false
                        } else {
                            val backupAgent = Util.getAgentBackupVersion()
                            if (backupAgent.isNotEmpty()) {
                                Log.i(TAG, "AGENT 기존 백업 파일 삭제 $backupAgent")
                                val i = Intent()
                                i.action = UpdateActivity.BROADCAST_STATUS_UPDATE
                                i.putExtra("MSG", "AGENT 기존 백업 파일 삭제 $backupAgent")
                                _context.sendBroadcast(i)
                                val del = doDeleteBackup("$backupAgent.apk")
                                if (del) {
                                    Util.setAgentBackupVersion(agentLast)
                                } else {
                                    Log.e(TAG, "AGENT 업데이트 종료")
                                    val i = Intent()
                                    i.action = UpdateActivity.BROADCAST_ERROR_UPDATE
                                    i.putExtra("MSG", "AGENT 업데이트 종료 (기존 백업 삭제 에러)")
                                    _context.sendBroadcast(i)
                                    return false
                                }
                            } else {
                                Util.setAgentBackupVersion(agentLast)
                            }

                            val agentApk = Util.getUpdateAgentVersion().plus(".apk")


                            Log.i(TAG, "AGENT 설치, $agentApk")

                            val path = "${Constant.ROOT}${Constant.FILE_APK_PATH}/${agentApk}"
                            val cmd = "pm install -r -i com.lotte.mart.commander --user 0 $path"

                            Log.i(TAG, "AGENT 설치 명렁어, $cmd")
                            Util.setCurrentAgentVersion(
                                agentUpdateInfo
                            )
                            Util.setAgentUpdateFinish(0)

                            val url = "androidcommander://agent?type=Cmd&cmd=$cmd"
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            _context.startActivity(intent)

                            val i = Intent()
                            i.action = UpdateActivity.BROADCAST_STATUS_UPDATE
                            i.putExtra("MSG", "AGENT 업데이트 설치 진행중...")
                            _context.sendBroadcast(i)

                            val m = mHandler.obtainMessage()
                            m.what = 1
                            m.obj = "AGENT가 설치 후 재시작 됩니다. 잠시만 기다려주세요..."
                            mHandler.sendMessage(m)

                            Log.i(TAG, "AGENT 업데이트 진행, $cmd, $url")
                            ForegroundService.AlarmSet = false
                        }
                    } else {
                        EmsService.serverSwitching()
                        return false
                    }
                    return true

            }
            else if (!triedVncUpdateFirstTime) {
                triedVncUpdateFirstTime = true

                //Vnc 버전 체크
                if (vncLast.isEmpty()) {
                    Util.initIni(_context)
                    return false
                }

                //Vnc 버전 체크
                if (vncUpdateInfo.isNullOrEmpty()) {
                    return false
                }

                if (hasUpdateVersion(
                        vncLast,
                        vncUpdateInfo
                    )
                ) {
                    Log.d(
                        TAG,
                        "Tried update of Vnc application, $triedVncUpdateFirstTime"
                    )
                    setUpdate()

                    Log.i(TAG, "VNC 업데이트 시작")
                    if(UpdateActivity.isActive()){
                        val i = Intent()
                        i.action = UpdateActivity.BROADCAST_STATUS_UPDATE
                        i.putExtra("MSG", "VNC 업데이트 시작")
                        _context.sendBroadcast(i)
                    } else {
                        var i = Intent(_context, UpdateActivity::class.java)
                        i.putExtra("MSG", "VNC 업데이트 시작")
                        i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        _context.startActivity(i)
                    }

                    //vnc update
                        Log.i(TAG, "VNC 업데이트 파일 다운로드 완료")
                        val i = Intent()
                        i.action = UpdateActivity.BROADCAST_STATUS_UPDATE
                        i.putExtra("MSG", "VNC 업데이트 파일 다운로드 완료")
                        _context.sendBroadcast(i)

                        val backup = doBackupApk("$vncLast.apk")
                        if (!backup) {
                            Log.e(TAG, "VNC 업데이트 종료")
                            val i = Intent()
                            i.action = UpdateActivity.BROADCAST_ERROR_UPDATE
                            i.putExtra("MSG", "VNC 업데이트 종료 (백업 에러)")
                            _context.sendBroadcast(i)
                            return false
                        } else {
                            val backupVnc = Util.getVncBackupVersion()
                            if (backupVnc.isNotEmpty()) {
                                Log.i(TAG, "VNC 기존 백업 파일 삭제 $backupVnc")

                                val i = Intent()
                                i.action = UpdateActivity.BROADCAST_STATUS_UPDATE
                                i.putExtra("MSG", "VNC 기존 백업 파일 삭제 $backupVnc")
                                _context.sendBroadcast(i)

                                val del = doDeleteBackup("$backupVnc.apk")
                                if (del)
                                    Util.setVncBackupVersion(vncLast)
                                else {
                                    Log.e(TAG, "VNC 업데이트 종료")

                                    val i = Intent()
                                    i.action = UpdateActivity.BROADCAST_ERROR_UPDATE
                                    i.putExtra("MSG", "VNC 업데이트 종료 (기존 백업 삭제 에러)")
                                    _context.sendBroadcast(i)
                                    return false
                                }
                            } else {
                                Util.setVncBackupVersion(vncLast)
                            }

                            Worker.vncFinish(_context)
                            val vncApk = Util.getUpdateVncVersion().plus(".apk")


                            Log.i(TAG, "VNC 설치 버전, $vncApk")

                            val path = "${Constant.ROOT}${Constant.FILE_APK_PATH}/${vncApk}"
                            val cmd =
                                "pm install -r -i com.lotte.mart.commander --user 0 $path"

                            Log.i(TAG, "VNC 설치 명령어, $cmd")

                            Util.setCurrentVncVersion(
                                vncUpdateInfo
                            )

                            val url = "androidcommander://agent?type=Cmd&cmd=$cmd"
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            _context.startActivity(intent)

                            val i = Intent()
                            i.action = UpdateActivity.BROADCAST_STATUS_UPDATE
                            i.putExtra("MSG", "VNC 업데이트 설치 진행중...")
                            _context.sendBroadcast(i)

                            Log.i(TAG, "VNC 업데이트 진행, $cmd, $url")
                        }
                    return true
                }
            }
            else if (!triedCmdUpdateFirstTime) {
                triedCmdUpdateFirstTime = true

                //Commander 버전 체크
                if(cmdLast.isEmpty()){
                    Util.initIni(_context)
                    return false
                }

                //Commander 버전 체크
                if (cmdUpdateInfo.isNullOrEmpty()) {
                    return false
                }

                if (hasUpdateVersion(cmdLast, cmdUpdateInfo)) {
                    Log.d(
                        TAG,
                        "Tried update of Commander application, $triedCmdUpdateFirstTime"
                    )
                    setUpdate()
                    setUpdateCommander()

                    if(UpdateActivity.isActive()){
                        val i = Intent()
                        i.action = UpdateActivity.BROADCAST_STATUS_UPDATE
                        i.putExtra("MSG", "COMMANDER 업데이트 시작")
                        _context.sendBroadcast(i)
                    } else {
                        var i = Intent(_context, UpdateActivity::class.java)
                        i.putExtra("MSG", "COMMANDER 업데이트 시작")
                        i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        _context.startActivity(i)
                    }

                    //commander update
                        Log.i(TAG, "COMMANDER 업데이트 파일 다운로드 완료")
                        val i = Intent()
                        i.action = UpdateActivity.BROADCAST_STATUS_UPDATE
                        i.putExtra("MSG", "COMMANDER 업데이트 파일 다운로드 완료")
                        _context.sendBroadcast(i)

                        val backup = doBackupApk("$cmdLast.apk")
                        if (!backup) {
                            Log.e(TAG, "COMMANDER 업데이트 종료")
                            val i = Intent()
                            i.action = UpdateActivity.BROADCAST_ERROR_UPDATE
                            i.putExtra("MSG", "COMMANDER 업데이트 종료 (백업 에러)")
                            _context.sendBroadcast(i)
                            return false
                        } else {
                            val backupCmd = Util.getCmdBackupVersion()
                            if (backupCmd.isNotEmpty()) {
                                Log.i(TAG, "COMMANDER 기존 백업 파일 삭제 $backupCmd")
                                val i = Intent()
                                i.action = UpdateActivity.BROADCAST_STATUS_UPDATE
                                i.putExtra("MSG", "COMMANDER 기존 백업 파일 삭제 $backupCmd")
                                _context.sendBroadcast(i)

                                val del = doDeleteBackup("$backupCmd.apk")
                                if (del)
                                    Util.setCmdBackupVersion(cmdLast)
                                else {
                                    Log.e(TAG, "COMMANDER 업데이트 종료")
                                    val i = Intent()
                                    i.action = UpdateActivity.BROADCAST_ERROR_UPDATE
                                    i.putExtra("MSG", "COMMANDER 업데이트 종료 (기존 백업 삭제 에러)")
                                    _context.sendBroadcast(i)
                                    return false
                                }
                            } else {
                                Util.setCmdBackupVersion(cmdLast)
                            }

                            val cmdApk =Util.getUpdateCmdVersion().replace("ZIP","apk")

//                                unZip(
//                                "${_context.cacheDir.path}/${vncUpdateInfo}",
//                                "${Constant.ROOT}${Constant.UPDATE_PATH}"
//                            )

                            Log.i(TAG, "CMD 압축 해제, $cmdApk")

                            val path = "${Constant.ROOT}${Constant.FILE_APK_PATH}${cmdApk}"
                            val cmd =
                                "pm install -r -i com.lotte.mart.commander --user 0 $path"

                            Util.setLastCmdVersion(
                                cmdUpdateInfo.toUpperCase()
                                    .substring(0, cmdUpdateInfo.lastIndexOf('.'))
                            )

                            val url = "androidcommander://agent?type=Cmd&cmd=$cmd"
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            _context.startActivity(intent)

                            val i = Intent()
                            i.action = UpdateActivity.BROADCAST_STATUS_UPDATE
                            i.putExtra("MSG", "COMMANDER 업데이트 설치 진행중...")
                            _context.sendBroadcast(i)
                            Log.i(TAG, "COMMANDER 업데이트 진행, $cmd, $url")
                        }
                    return true
                }
            }


        return false
    }


    /**
     * POS 업데이트 작업 수행
     */
    private fun doPosUpdate(){
        //POS 앱 업데이트
        "APOS_0.0.17-20210428"
        var posUpdate =  Util.getPosUpdate()
        val cmd = "pm install -r -i com.lotte.mart.commander --user 0 $posUpdate"

        Log.i(TAG, "POS 업데이트 진행, $cmd")
        setUpdate()

        val i = Intent()
        i.action = UpdateActivity.BROADCAST_STATUS_UPDATE
        i.putExtra("MSG", "POS 업데이트 설치 진행중...")
        _context.sendBroadcast(i)
        
        val url = "androidcommander://agent?type=Cmd&cmd=$cmd"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        _context.startActivity(intent)
        appFinish = false

        triedPosUpdateFirstTime = true
    }

    /**
     * POS 앱 종료 여부 확인
     */
    private fun checkProcess() {
        val servicePackage = "com.lotte.mart.commander"
        val cmd = "dumpsys activity | grep 'Proc #' | grep -v 'cch-empty' | grep '${AgentIniUtil.getInstance()!!.getPosPackageName(
            "com.lotte.mart.cloudpos"
        )
        }'"

        if(!WatchService.isRunningCommander()) {
            Log.i(TAG, "Commander is not running")
            return
        }
        
        if(!appFinish){
            appFinish = true
            Worker.appFinish(_context)
        }

        if (!response) {
            if(UpdateActivity.isActive()) {
                val i = Intent()
                i.action = UpdateActivity.BROADCAST_STATUS_UPDATE
                i.putExtra("MSG", "POS 종료 대기중...")
                _context.sendBroadcast(i)
            } else {
                var i = Intent(_context, UpdateActivity::class.java)
                i.putExtra("MSG", "POS 업데이트 시작")
                i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                _context.startActivity(i)
            }

            response = true
            Log.d(TAG, "process check, $cmd")
            ClientMessengerService.release()
            ClientMessengerService.with(_context, servicePackage)?.cmdExec(
                cmd,
                object :
                    ResponseCallback {
                    override fun onSuccess(res: String) {
                        Log.i("d", "onSuccess $res")
                        if (res.isEmpty()) {
                            setUpdate()
                            doPosUpdate()
                            response = false
                        } else {
                            Log.d(TAG, "process arrive, $cmd")
                            Worker.appFinish(_context)
                            response = false
                        }
                        ClientMessengerService.release()
                    }

                    override fun onFail(err: String) {
                        //failed
                        val m = mHandler.obtainMessage()
                        m.what = 1
                        m.obj = "업데이트 실패"
                        mHandler.sendMessage(m)
                        Worker.appUpdateFinish(RESULT_FAIL, _context)
                        setUpdateFinish()
                        val i = Intent()
                        i.action = UpdateActivity.BROADCAST_ERROR_UPDATE
                        i.putExtra("MSG", "업데이트 실패 (종료 대기 실패)")
                        _context.sendBroadcast(i)
                        Util.setPosUpdate("")
                        appFinish = false
                        response = false
                        Log.d(TAG, "failed process check, $cmd")
                        ClientMessengerService.release()
                    }

                    override fun onResponse(msg: String) {
                        Log.i("d", "onResponse $msg")
                    }
                })
        } else {
            response = false
        }
    }

    /**
     * 압축 풀기
     * @param zipFilePath - zip파일 경로
     * @param targetPath - 압축 해제 경로
     */
    private fun unZip(zipFilePath: String, targetPath: String) : String {
        ZipFile(zipFilePath).use { zip ->
            zip.entries().asSequence().forEach { entry ->
                if(entry.isDirectory){
                    File(targetPath, entry.name).mkdirs()
                } else {
                    zip.getInputStream(entry).use { input ->
                        File(targetPath, entry.name).outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                }

                return entry.name
            }
        }

        return ""
    }

    /**
     * apk파일 백업
     * @param fileName - 백업 파일 명
     */
    private fun doBackupApk(fileName:String):Boolean{
        if (fileName.isNullOrEmpty()) {
            Log.d(TAG, "백업 파일 없음")
            return true
        }

        return try {
            val filePath: Path = Paths.get("${Constant.ROOT}${Constant.FILE_APK_PATH}/$fileName")
            val filePathToMove: Path = Paths.get("${Constant.ROOT}${Constant.BACKUP_PATH}/$fileName")
            Log.d(TAG, "백업 파일 $filePath, 이동 경로 $filePathToMove")
            if(Files.exists(filePathToMove)){
                //중복 파일 존재시 삭제
                Files.delete(filePathToMove)
            }
            
            if(Files.exists(filePath)) {
                Files.move(filePath, filePathToMove)
                Log.d(TAG, "백업 파일 생성")
            }
            true
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e(TAG, "파일 백업 실패", e)
            false
        }
    }

    /**
     * 이전 백업파일 삭제
     * @param backup - 백업 파일 명
     */
    private fun doDeleteBackup(backup:String):Boolean{
        if (backup.isNullOrEmpty()) {
            Log.d(TAG, "이전 백업된 파일 없음")
            return true
        }

        return try {
            var backupPath = Paths.get("${Constant.ROOT}${Constant.BACKUP_PATH}/$backup")
            if(Files.exists(backupPath)) {
                Log.d(TAG, "이전 백업 파일 삭제, $backupPath")
                Files.delete(backupPath)
            }
            true
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e(TAG, "파일 삭제 실패", e)
            false
        }
    }

    /**
     * 업데이트 버전 문자만 확인(0.0.1)
     * @param cur - 현재버전
     * @param update - 업데이트 파일 버전
     */
    private fun hasUpdateVersion(cur:String, update:String):Boolean{
        var hasUpdate = false
        try {
            val curVer = cur.split("-")[0].split("_")[1]    //1.0.1
            val updateVer = update.split("-")[0].split("_")[1]    //1.0.1

            if(curVer.isNotEmpty() && updateVer.isNotEmpty()){
                val curVerStrings = curVer.split(".")
                val updateVerStrings = updateVer.split(".")
                if(curVerStrings.size == updateVerStrings.size){
                    // RB 문자 포함될경우 롤백이므로 무조건 업데이트해서 앱을 엎어친다.
                    if (update.contains("RB")) {
                        for(i in curVerStrings.indices){
                            if(curVerStrings[i].toInt() != updateVerStrings[i].toInt()) {
                                hasUpdate = true
                            }
                        }
                    } else {
                        // 정상적인 경우
                        for(i in curVerStrings.indices){
                            if(curVerStrings[i].toInt() < updateVerStrings[i].toInt())
                                hasUpdate = true
                        }
                    }

                }

//                if((curVer != updateVer && updateVer == "0.0.1")){   //초기버전 무조건 업데이트
//                    hasUpdate = true
//                }

            } else
                hasUpdate = false
        }catch (e:Exception){
            Log.i(TAG, "$cur, $update")
            e.printStackTrace()
            hasUpdate = false
        } finally {
            return hasUpdate
        }
    }

    /**
     * 업데이트 전체 문자 확인(MON_0.0.1-20210323)
     * @param cur - 현재버전
     * @param update - 업데이트 파일 버전
     */
    private fun hasUpdateString(cur:String, update:String):Boolean{
        var hasUpdate = false
        try {
            if(cur.isNotEmpty() && update.isNotEmpty()){
                if(cur != update)
                    hasUpdate = true
            } else
                hasUpdate = false
        }catch (e:Exception){
            Log.i(TAG, "$cur, $update")
            e.printStackTrace()
            hasUpdate = false
        } finally {
            return hasUpdate
        }
    }
}