package com.lotte.mart.agent.utils

import android.os.Environment
import com.lotte.mart.agent.utils.AgentIniUtil.Constant.FILE_SETTING_PATH
import com.lotte.mart.agent.utils.Util.getLastPosLogUploadKey
import com.lotte.mart.agent.utils.Util.getPosLogUploadKey
import com.lotte.mart.agent.utils.Util.getPosUpdate
import com.lotte.mart.agent.utils.Util.getUpdateAgentVersion
import com.lotte.mart.agent.utils.Util.getUpdateCmdVersion
import com.lotte.mart.agent.utils.Util.getUpdateVncVersion
import com.lotte.mart.agent.utils.Util.setLastPosLogUploadKey
import com.lotte.mart.agent.utils.Util.setPosLogUploadKey
import com.lotte.mart.agent.utils.Util.setPosUpdate
import com.lotte.mart.agent.utils.Util.setUpdateAgentVersion
import com.lotte.mart.agent.utils.Util.setUpdateCmdVersion
import com.lotte.mart.agent.utils.Util.setUpdateVncVersion
import com.lotte.mart.commonlib.utility.InIUtils
import java.io.File

/**
 * AgentConfig.ini 유틸
 */
class AgentIniUtil {
    object Constant{
        //로컬 디비 디렉토리 경로
        val PATH_LOCAL_DB  = Environment.getExternalStorageDirectory().toString() + "/lottepos/db/"
        //로컬 로그 디렉토리 경로
        val PATH_LOCAL_LOG  = Environment.getExternalStorageDirectory().toString() + "/lottepos/log/"

        const val settingJson = "setting.json"
        const val FILE_ROOT_PATH = "/lottepos"
        const val FILE_DIR_SETTING = "/setting"
        val FILE_SETTING_PATH = Environment.getExternalStorageDirectory().toString() + FILE_ROOT_PATH + FILE_DIR_SETTING + File.separator + settingJson
        //POS 패키지명
        const val POS_PACKAGE  = "posPackage"
        //POS 감시 대상 여부(감시 대상 설정시 로깅)
        const val POS_WATCH = "posWatch"
        //VNC 패키지명
        const val VNC_PACKAGE  = "vncPackage"
        //POS 종료 상태시 자동 시작(POS_WATCH값 세팅 필수)
        const val POS_AUTO_START  = "posAutoStart"
        //감시 주기(인터벌) 설정
        const val WATCHER_INTERVAL  = "watcherInterval"
        //업데이트 확인 주기(인터벌) 설정 - 현재는 앱 시작시에만 체크
        const val UPDATE_INTERVAL  = "updateInterval"
        //점서버 로그 전송 주기(인터벌) 설정
        const val EMSLOG_INTERVAL  = "emsLogInterval"
        //점서버 포트
        const val SERVER_PORT  = "serverPort"
        //원격 접속 포트
        const val VNC_PORT = "vncPort"
        //원격 접속 비밀번호
        const val VNC_PWD  = "vncPwd"
        //SFTP 접속 포트
        const val VNC_SFTP_PORT  = "sftpPort"
        //SFTP 접속 아이디
        const val VNC_SFTP_ID  = "sftpId"
        //SFTP 접속 비밀번호
        const val VNC_SFTP_PWD  = "sftpPwd"
        const val POS_NO = "posNo"
        const val POS_STRCD = "strCd"
        const val PREF_KEY_MY_SERVER_IP1 = "serverIP1"
        const val PREF_KEY_MY_POS_MFR = "manufacturer"

        // 로그
        const val POS_PROCESS_UNIT   = "posProcessUnit"    // ;EMS 로그전송 처리 단위(POS)
        const val AGENT_PROCESS_UNIT  = "agentProcessUnit" // ;AGENT 로그전송 처리 단위(POS)
        const val ERROR_PROCESS_UNIT = "errorProcessUnit"  // ;EMS 로그전송 처리 단위(POS)
        const val ERROR_AUTO_RETRY  = "errorAutoRetry" // //ERROR 데이터 자동 전송 시도 여부


        //POS 로그 DB 경로
        const val PATH_POS_LOG_DB = "pathPosLogdb"
        //Agent 로그 DB 경로
        const val PATH_AGENT_LOG_DB  = "pathAgentLogdb"
        //Error 로그 DB 경로
        const val LOG_PATH_ERROR_LOG_DB  = "pathErrorLogdb"
        //로그파일 디렉토리 경로
        const val LOG_PATH = "logPath"
        //로그파일 유효 기간(삭제일)
        const val LOG_DAYS  = "logDays"
        //로그디비 유효 기간(삭제일) - Agent, Error
        const val DB_LOG_DAYS  = "dbLogDays"
        const val SFTP_ID   = "sftpId"
        const val SFTP_PWD   = "sftpPwd"
        const val CMD   = "CMD"
        const val MON   = "MON"
        const val RMT   = "RMT"
        const val UPDATE_MON   = "updateMON"
        const val UPDATE_RMT   = "updateRMT"
        const val UPDATE_CMD   = "updateCMD"
        const val POS_UPDATE   = "posUpdate"
        const val OPT_LOG_POS_UPLOAD_KEY = "posUploadKey"
        const val OPT_LOG_LAST_POS_UPLOAD_KEY = "lastPosUploadKey"
        const val OPT_LOG_AGENT_UPLOAD_KEY = "agentUploadKey"
        const val OPT_LOG_LAST_AGENT_UPLOAD_KEY  = "lastAgentUploadKey"
        const val MON_BACKUP  = "monBackup"
        const  val OPT_UPDATE_RMT_BACKUP  = "rmtBackup"
        const  val OPT_UPDATE_CMD_BACKUP  = "cmdBackup"
        const val OPT_UPDATE_MON = "MON"
        const val OPT_UPDATE_RMT = "RMT"
        const val OPT_UPDATE_CMD = "CMD"

        const val MON_UPDATED = "monUpdated"
        const val OPT_UPDATE_POS_UPDATE  = "posUpdate"


    }

    companion object {
        @Volatile
        private var instance : AgentIniUtil? = null

        fun getInstance() : AgentIniUtil =
            instance ?: synchronized(AgentIniUtil::class.java) {
                  instance ?: AgentIniUtil().also {
                      instance = it
                  }
        }
    }

    /**
     * ini 초기화
     * 값이 존재하지 않을 경우 기본값 설정
     */
    fun initIni(){
        if(getPosProcessUnit("10").isEmpty()) setPosProcessUnit("10")

        if(getAgentProcessUnit("10").isEmpty()) setAgentProcessUnit("10")

        if(getErrorProcessUnit("10").isEmpty()) setErrorProcessUnit("10")

        if(getErrorAutoRetry("1").isEmpty()) setErrorAutoRetry("1")

        if(getPathPosLogDb("${Constant.PATH_LOCAL_DB}AmsLogData.db").isEmpty()) setPathPosLogDb("${Constant.PATH_LOCAL_DB}AmsLogData.db")

        if(getPathAgentLogDb("${Constant.PATH_LOCAL_DB}AgentLogData.db").isEmpty()) setPathAgentLogDb("${Constant.PATH_LOCAL_DB}AgentLogData.db")

        if(getLogPath(Constant.PATH_LOCAL_LOG).isEmpty()) setLogPath(Constant.PATH_LOCAL_LOG)

        if(getLogDays("7").isEmpty()) setLogDays("7")

        if(getDbLogDays("7").isEmpty()) setDbLogDays("7")

        if(getPosPackageName("com.lotte.mart.cloudpos").isEmpty()) setPosPackageName("com.lotte.mart.cloudpos")

        if(getVncPackageName("com.lotte.mart.vncserver").isEmpty()) setVncPackageName("com.lotte.mart.vncserver")

        if(getPosWatch("0").isEmpty()) setPosWatch("0")

        if(getPosAutoStart("0").isEmpty()) setPosAutoStart("0")

//        if(getVncAutoStart().isEmpty()) {
//            setVncAutoStart("1")
//        }
        if(getWatcherInterval("30000").isEmpty()) setWatcherInterval("30000")

        if(getUpdateInterval("120000").isEmpty()) setUpdateInterval("120000")

        if(getEmsLogInterval("30000").isEmpty()) setEmsLogInterval("30000")

//        if(getServerIp().isEmpty()){
//            setServerIp("10.12.21.135")
//        }

        if(getServerPort("31070").isEmpty()) setServerPort("31070")

        if(getVncPort("31079").isEmpty()) setVncPort("31079")

        if(getVncPwd("3333").isEmpty()) setVncPwd("3333")

        if(getVncSftpPort("2222").isEmpty()) setVncSftpPort("2222")

        if(getVncSftpId("lotteds").isEmpty()) setVncSftpId("lotteds")

        if(getVncSftpPwd("1234").isEmpty()) setVncSftpPwd("1234")

        if(getPosUpdate().isEmpty()) setPosUpdate("")

        if(getLastPosLogUploadKey().isEmpty()) setLastPosLogUploadKey("")

        if(getPosLogUploadKey().isEmpty()) setPosLogUploadKey("")

        if(getUpdateCmdVersion().isEmpty()) setUpdateCmdVersion("")

        if(getUpdateVncVersion().isEmpty()) setUpdateVncVersion("")

        if(getUpdateAgentVersion().isEmpty()) setUpdateAgentVersion("")




//        if(getUpdateType().isEmpty()){
//            setUpdateType("D")
//        }
    }



    /**
     * POS 로그 처리 단위 저장
     */
    fun setPosProcessUnit(unit:String){
        InIUtils.saveData(FILE_SETTING_PATH, Constant.POS_PROCESS_UNIT, unit)
    }

    /**
     * POS 로그 처리 단위
     */
    fun getPosProcessUnit(default: String = "10"):String{
        return InIUtils.loadData(FILE_SETTING_PATH, Constant.POS_PROCESS_UNIT,default)
    }

    /**
     * Agent 로그 처리 단위 저장
     */
    fun setAgentProcessUnit(unit:String){
        InIUtils.saveData(FILE_SETTING_PATH, Constant.AGENT_PROCESS_UNIT, unit)
    }

    /**
     * Agent 로그 처리 단위
     */
    fun getAgentProcessUnit(default: String):String{
        return InIUtils.loadData(FILE_SETTING_PATH,Constant.AGENT_PROCESS_UNIT,default)
    }

    /**
     * ERROR 로그 처리 단위 저장
     */
    fun setErrorProcessUnit(unit:String){
        InIUtils.saveData(FILE_SETTING_PATH, Constant.ERROR_PROCESS_UNIT, unit)
    }

    /**
     * ERROR 로그 처리 단위
     */
    fun getErrorProcessUnit(default: String):String{
        return InIUtils.loadData(FILE_SETTING_PATH,Constant.ERROR_PROCESS_UNIT,default)
    }

    /**
     * ERROR 로그 자동 전송 여부 저장
     */
    fun setErrorAutoRetry(unit:String){
        InIUtils.saveData(FILE_SETTING_PATH, Constant.ERROR_AUTO_RETRY, unit)
    }

    /**
     * ERROR 로그 자동 전송 여부
     */
    fun getErrorAutoRetry(default: String):String{
        return InIUtils.loadData(FILE_SETTING_PATH, Constant.ERROR_AUTO_RETRY,default)
    }

    /**
     * POS 로그DB 경로 저장
     */
    fun setPathPosLogDb(unit:String){
        InIUtils.saveData(FILE_SETTING_PATH, Constant.PATH_POS_LOG_DB, unit)
    }

    /**
     * POS 로그DB 경로
     */
    fun getPathPosLogDb(default: String):String{
        return InIUtils.loadData(FILE_SETTING_PATH, Constant.PATH_POS_LOG_DB,default)
    }

    /**
     * AGENT 로그DB 경로 저장
     */
    fun setPathAgentLogDb(unit:String){
        InIUtils.saveData(FILE_SETTING_PATH, Constant.PATH_AGENT_LOG_DB, unit)
    }

    /**
     * AGENT 로그DB 경로
     */
    fun getPathAgentLogDb(default: String):String{
        return InIUtils.loadData(FILE_SETTING_PATH,Constant.PATH_AGENT_LOG_DB,default)
    }

    /**
     * ERROR 로그DB 경로 저장
     */
    fun setPathErrorLogDb(unit:String){
        InIUtils.saveData(FILE_SETTING_PATH, Constant.LOG_PATH_ERROR_LOG_DB, unit)
    }

    /**
     * ERROR 로그DB 경로
     */
    fun getPathErrorLogDb(default : String):String{
        return InIUtils.loadData(FILE_SETTING_PATH, Constant.LOG_PATH_ERROR_LOG_DB,default)
    }

    /**
     * LOG파일 경로 저장
     */
    fun setLogPath(unit:String){
        InIUtils.saveData(FILE_SETTING_PATH, Constant.LOG_PATH, unit)
    }

    /**
     * LOG파일 경로
     */
    fun getLogPath(default: String):String{
        return InIUtils.loadData(FILE_SETTING_PATH, Constant.LOG_PATH,default)
    }

    /**
     * LOG저장 일수 저장
     */
    fun setLogDays(unit:String){
        InIUtils.saveData(FILE_SETTING_PATH, Constant.LOG_DAYS, unit)
    }

    /**
     * LOG저장 일수
     */
    fun getLogDays(default: String = "7"):String{
        return InIUtils.loadData(FILE_SETTING_PATH, Constant.LOG_DAYS,default)
    }

    /**
     * DB(Agent, Error) LOG저장 일수 저장
     */
    fun setDbLogDays(unit:String){
        InIUtils.saveData(FILE_SETTING_PATH, Constant.DB_LOG_DAYS, unit)
    }

    /**
     * DB(Agent, Error) LOG저장 일수
     */
    fun getDbLogDays(default: String = "7"):String{
        return InIUtils.loadData(FILE_SETTING_PATH, Constant.DB_LOG_DAYS,default)
    }

    /**
     * POS 앱 패키지명 저장
     */
    fun setPosPackageName(unit:String){
        InIUtils.saveData(FILE_SETTING_PATH, Constant.POS_PACKAGE, unit)
    }

    /**
     * POS 앱 패키지명
     */
    fun getPosPackageName(default: String = "com.lotte.mart.cloudpos"):String{
        return InIUtils.loadData(Constant.FILE_SETTING_PATH, Constant.POS_PACKAGE,"com.lotte.mart.cloudpos")
    }

    /**
     * POS 앱 감시 여부(자동재시작) 저장
     */
    fun setPosWatch(unit:String){
        InIUtils.saveData(FILE_SETTING_PATH, Constant.POS_WATCH, unit)
    }

    /**
     * POS 앱 감시 여부(자동재시작)
     */
    fun getPosWatch(default: String):String{
        return InIUtils.loadData(FILE_SETTING_PATH, Constant.POS_WATCH,default)
    }

    /**
     * VNC 앱 패키지명 저장
     */
    fun setVncPackageName(unit:String){
        InIUtils.saveData(FILE_SETTING_PATH, Constant.VNC_PACKAGE, unit)
    }

    /**
     * VNC 앱 패키지명
     */
    fun getVncPackageName(default: String):String{
        return InIUtils.loadData(FILE_SETTING_PATH, Constant.VNC_PACKAGE,default)
    }

    /**
     * POS 앱 자동 재시작 여부 저장
     */
    fun setPosAutoStart(unit:String){
        InIUtils.saveData(FILE_SETTING_PATH, Constant.POS_AUTO_START, unit)
    }

    /**
     * POS 앱 자동 재시작 여부
     */
    fun getPosAutoStart(default: String):String{
        return InIUtils.loadData(FILE_SETTING_PATH, Constant.POS_AUTO_START,"com.lotte.mart.vncserver")
    }

    /**
     * 감시 주기 설정
     */
    fun setWatcherInterval(unit:String){
        InIUtils.saveData(FILE_SETTING_PATH, Constant.WATCHER_INTERVAL, unit)
    }

    /**
     * 감시 주기
     */
    fun getWatcherInterval(default: String):String{
        return if(InIUtils.loadData(FILE_SETTING_PATH, Constant.WATCHER_INTERVAL,"30000").isEmpty()){
            default
        }else{
            InIUtils.loadData(FILE_SETTING_PATH, Constant.WATCHER_INTERVAL,"30000")
        }
    }

    /**
     * 앱 업데이트 주기 설정
     */
    fun setUpdateInterval(unit:String){
        InIUtils.saveData(FILE_SETTING_PATH, Constant.UPDATE_INTERVAL, unit)
    }

    /**
     * 앱 업데이트 주기
     */
    fun getUpdateInterval(default: String):String{
        return if(InIUtils.loadData(FILE_SETTING_PATH, Constant.UPDATE_INTERVAL,default).isEmpty()){
            default
        }else{
            InIUtils.loadData(FILE_SETTING_PATH, Constant.UPDATE_INTERVAL,default)
        }
    }

    /**
     * EMS로그 전송 주기 설정
     */
    fun setEmsLogInterval(unit:String){
        InIUtils.saveData(FILE_SETTING_PATH, Constant.EMSLOG_INTERVAL, unit)
    }

    /**
     * EMS로그 전송 주기
     */
    fun getEmsLogInterval(default: String):String{
        return if(InIUtils.loadData(FILE_SETTING_PATH, Constant.EMSLOG_INTERVAL,"30000").isEmpty())
        {
            default
        }
        else{
            InIUtils.loadData(FILE_SETTING_PATH, Constant.EMSLOG_INTERVAL,"30000")
        }

    }

    /**
     * 점서버 접속 ip
     */
    fun getServerIp():String{
        return InIUtils.loadData(FILE_SETTING_PATH, Constant.PREF_KEY_MY_SERVER_IP1,"10.52.26.26")
    }



    /**
     * 점서버 접속 port 저장
     */
    fun setServerPort(unit:String){
        InIUtils.saveData(FILE_SETTING_PATH, Constant.SERVER_PORT, unit)
    }


    /**
     * 점서버 접속 port
     */
    fun getServerPort(default: String):String{
        return InIUtils.loadData(FILE_SETTING_PATH, Constant.SERVER_PORT,"36010")
    }

    /**
     * VNC 접속 포트 값 저장
     */
    fun setVncPort(unit:String){
        InIUtils.saveData(FILE_SETTING_PATH, Constant.VNC_PORT, unit)
    }

    /**
     * VNC 접속 포트
     */
    fun getVncPort(default: String):String{
        return InIUtils.loadData(FILE_SETTING_PATH, Constant.VNC_PORT,default)
    }

    /**
     * VNC 접속 패스워드 저장
     */
    fun setVncPwd(unit:String){
        InIUtils.saveData(FILE_SETTING_PATH, Constant.VNC_PWD, unit)
    }

    /**
     * VNC 접속 패스워드
     */
    fun getVncPwd(default: String):String{
        return InIUtils.loadData(FILE_SETTING_PATH, Constant.VNC_PWD,default)
    }

    /**
     * VNC Sftp 포트 저장
     */
    fun setVncSftpPort(unit:String){
        InIUtils.saveData(FILE_SETTING_PATH, Constant.VNC_SFTP_PORT, unit)
    }

    /**
     * VNC Sftp 포트
     */
    fun getVncSftpPort(default: String):String{
        return InIUtils.loadData(FILE_SETTING_PATH,Constant.VNC_SFTP_PORT,default)
    }

    /**
     * VNC Sftp 접속 아이디 저장
     */
    fun setVncSftpId(unit:String){
        InIUtils.saveData(FILE_SETTING_PATH, Constant.VNC_SFTP_ID, unit)
    }

    /**
     * VNC Sftp 접속 아이디
     */
    fun getVncSftpId(default: String):String{
        return InIUtils.loadData(FILE_SETTING_PATH,Constant.SFTP_ID,default)
    }

    /**
     * VNC Sftp 접속 패스워드 저장
     */
    fun setVncSftpPwd(unit:String){
        InIUtils.saveData(FILE_SETTING_PATH, Constant.VNC_SFTP_PWD, unit)
    }

    /**
     * VNC Sftp 접속 패스워드
     */
    fun getVncSftpPwd(default: String):String{
        return InIUtils.loadData(FILE_SETTING_PATH,Constant.SFTP_PWD,default)
    }

}