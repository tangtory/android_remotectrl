package com.lotte.mart.agent.utils

import android.content.Context
import android.os.Build
import android.util.Log
import com.lotte.mart.agent.service.EmsService.Companion.TAG
import com.lotte.mart.agent.utils.AgentIniUtil.Constant.OPT_LOG_AGENT_UPLOAD_KEY
import com.lotte.mart.agent.utils.AgentIniUtil.Constant.OPT_LOG_LAST_AGENT_UPLOAD_KEY
import com.lotte.mart.agent.utils.AgentIniUtil.Constant.OPT_LOG_LAST_POS_UPLOAD_KEY
import com.lotte.mart.agent.utils.AgentIniUtil.Constant.OPT_LOG_POS_UPLOAD_KEY
import com.lotte.mart.agent.utils.AgentIniUtil.Constant.OPT_UPDATE_CMD
import com.lotte.mart.agent.utils.AgentIniUtil.Constant.OPT_UPDATE_CMD_BACKUP
import com.lotte.mart.agent.utils.AgentIniUtil.Constant.OPT_UPDATE_MON
import com.lotte.mart.agent.utils.AgentIniUtil.Constant.MON_BACKUP
import com.lotte.mart.agent.utils.AgentIniUtil.Constant.MON_UPDATED
import com.lotte.mart.agent.utils.AgentIniUtil.Constant.OPT_UPDATE_POS_UPDATE
import com.lotte.mart.agent.utils.AgentIniUtil.Constant.OPT_UPDATE_RMT
import com.lotte.mart.agent.utils.AgentIniUtil.Constant.OPT_UPDATE_RMT_BACKUP
import com.lotte.mart.agent.utils.AgentIniUtil.Constant.POS_NO
import com.lotte.mart.agent.utils.AgentIniUtil.Constant.POS_STRCD
import com.lotte.mart.commonlib.utility.InIUtils
import com.lotte.mart.commonlib.utility.Utility
import java.io.File
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

object Util {


    fun initIni(context: Context){
        // todo 확인 필요
        setCurrentAgentVersion(Utility.getVersionInfo(context.packageName, context))

        if(AgentIniUtil.getInstance().getVncPackageName("com.lotte.mart.vncserver") != "" && Utility.getInstallPackage(AgentIniUtil.getInstance().getVncPackageName("com.lotte.mart.vncserver"), context))
            setCurrentVncVersion(Utility.getVersionInfo(AgentIniUtil.getInstance().getVncPackageName("com.lotte.mart.vncserver"), context))

        if(Utility.getInstallPackage("com.lotte.mart.commander", context))
            setLastCmdVersion(Utility.getVersionInfo("com.lotte.mart.commander", context))
    }


    fun isExistAgentIni():Boolean {
        return InIUtils.isExist(AgentIniUtil.Constant.FILE_SETTING_PATH)
    }

    //AGENT 업데이트 진행
    fun getAgentUpdateFinish(): Int? {
        var res = InIUtils.loadData(AgentIniUtil.Constant.FILE_SETTING_PATH,MON_UPDATED,"")
        return res.toIntOrNull()
    }

    //AGENT 업데이트 진행
    fun setAgentUpdateFinish(res: Int){
        InIUtils.saveData(AgentIniUtil.Constant.FILE_SETTING_PATH, MON_UPDATED, res.toString())
    }

    //업데이트 버전
    fun getUpdateAgentVersion():String{
        return InIUtils.loadData(
            AgentIniUtil.Constant.FILE_SETTING_PATH,
            AgentIniUtil.Constant.UPDATE_MON,
            ""
        )
    }
    //업데이트 버전
    fun setUpdateAgentVersion(default : String){
        InIUtils.saveData(
            AgentIniUtil.Constant.FILE_SETTING_PATH,
            AgentIniUtil.Constant.UPDATE_MON,
            ""
        )
    }


    //업데이트 버전
    fun getUpdateVncVersion():String{
        return InIUtils.loadData(
            AgentIniUtil.Constant.FILE_SETTING_PATH,
            AgentIniUtil.Constant.UPDATE_RMT,
            ""
        )
    }

    //업데이트 버전
    fun setUpdateVncVersion(default : String){
         InIUtils.saveData(
            AgentIniUtil.Constant.FILE_SETTING_PATH,
            AgentIniUtil.Constant.UPDATE_RMT,
             default
        )
    }



    //업데이트 버전
    fun getUpdateCmdVersion():String{
        return InIUtils.loadData(
            AgentIniUtil.Constant.FILE_SETTING_PATH,
            AgentIniUtil.Constant.UPDATE_CMD,
            ""
        )
    }

    //업데이트 버전
    fun setUpdateCmdVersion(default : String){
         InIUtils.saveData(
            AgentIniUtil.Constant.FILE_SETTING_PATH,
            AgentIniUtil.Constant.UPDATE_CMD,
            ""
        )
    }


    // 현재버전
    fun getCurrentAgentVersion():String{
        return InIUtils.loadData(AgentIniUtil.Constant.FILE_SETTING_PATH, AgentIniUtil.Constant.MON,"")
    }

    // 현재버전
    fun getCurrentVncVersion():String{
        return InIUtils.loadData(
            AgentIniUtil.Constant.FILE_SETTING_PATH,
            AgentIniUtil.Constant.RMT,
            ""
        )
    }

    //현재버전
    fun getCurrentCmdVersion():String{
        return InIUtils.loadData(AgentIniUtil.Constant.FILE_SETTING_PATH, AgentIniUtil.Constant.CMD
        ,"")
    }

    //AGENT 백업 버전정보
    fun getAgentBackupVersion():String{
        return InIUtils.loadData(AgentIniUtil.Constant.FILE_SETTING_PATH, MON_BACKUP,"")
    }

    //VNC 백업 버전정보
    fun getVncBackupVersion():String{
        return InIUtils.loadData(AgentIniUtil.Constant.FILE_SETTING_PATH, OPT_UPDATE_RMT_BACKUP,"")
    }

    //Commandre 백업 버전정보
    fun getCmdBackupVersion():String{
        return InIUtils.loadData(AgentIniUtil.Constant.FILE_SETTING_PATH, OPT_UPDATE_CMD_BACKUP,"")
    }

    //대기중인 POS앱 업데이트 정보
    fun getPosUpdate():String{
        return InIUtils.loadData(
            AgentIniUtil.Constant.FILE_SETTING_PATH,
            AgentIniUtil.Constant.POS_UPDATE,
            ""
        )
    }

    //AGENT 업데이트 된 버전 저장
    fun setCurrentAgentVersion(ver:String){
        InIUtils.saveData(AgentIniUtil.Constant.FILE_SETTING_PATH, OPT_UPDATE_MON, ver)
    }

    //AGENT 백업 버전 저장
    fun setAgentBackupVersion(ver:String){
        InIUtils.saveData(AgentIniUtil.Constant.FILE_SETTING_PATH, MON_BACKUP, ver)
    }

    //VNC 업데이트 된 버전 저장
    fun setCurrentVncVersion(ver:String){
        InIUtils.saveData(AgentIniUtil.Constant.FILE_SETTING_PATH, OPT_UPDATE_RMT, ver)
    }

    //Commander 업데이트 된 버전 저장
    fun setLastCmdVersion(ver:String){
        InIUtils.saveData(AgentIniUtil.Constant.FILE_SETTING_PATH, OPT_UPDATE_CMD, ver)
    }

    //VNC 백업 버전 저장
    fun setVncBackupVersion(ver:String){
        InIUtils.saveData(AgentIniUtil.Constant.FILE_SETTING_PATH, OPT_UPDATE_RMT_BACKUP, ver)
    }

    //Commander 백업 버전 저장
    fun setCmdBackupVersion(ver:String){
        InIUtils.saveData(AgentIniUtil.Constant.FILE_SETTING_PATH, OPT_UPDATE_CMD_BACKUP, ver)
    }

    //POS 업데이트 대기 설정
    fun setPosUpdate(ver:String){
        InIUtils.saveData(AgentIniUtil.Constant.FILE_SETTING_PATH, OPT_UPDATE_POS_UPDATE, ver)
    }

    //완료된 POS LOG 업로드 키(seq-curdate-curtime) 저장
    fun setLastPosLogUploadKey(ver:String){
        InIUtils.saveData(AgentIniUtil.Constant.FILE_SETTING_PATH, OPT_LOG_LAST_POS_UPLOAD_KEY, ver)
    }

    //마지막 업로드 시도한 POS LOG 키(seq-curdate-curtime) 저장
    fun setPosLogUploadKey(ver:String){
        InIUtils.saveData(AgentIniUtil.Constant.FILE_SETTING_PATH, OPT_LOG_POS_UPLOAD_KEY, ver)
    }

    //완료된 POS LOG 업로드 키(seq-curdate-curtime)
    fun getLastPosLogUploadKey():String{
        return InIUtils.loadData(
            AgentIniUtil.Constant.FILE_SETTING_PATH,
            OPT_LOG_LAST_POS_UPLOAD_KEY,""
        )
    }

    //마지막 업로드 시도한 POS LOG 키(seq-curdate-curtime)
    fun getPosLogUploadKey():String{
        return InIUtils.loadData(
            AgentIniUtil.Constant.FILE_SETTING_PATH,
            OPT_LOG_POS_UPLOAD_KEY,
            ""
        )
    }

    //완료된 POS LOG 업로드 키(seq-curdate-curtime) 저장
    fun setLastAgentLogUploadKey(ver:String){
        InIUtils.saveData(AgentIniUtil.Constant.FILE_SETTING_PATH, OPT_LOG_LAST_AGENT_UPLOAD_KEY, ver)
    }

    //완료된 POS LOG 업로드 키(seq-curdate-curtime)
    fun getLastAgentLogUploadKey():String{
        return InIUtils.loadData(
            AgentIniUtil.Constant.FILE_SETTING_PATH, OPT_LOG_LAST_AGENT_UPLOAD_KEY,
            ""
        )
    }

    //마지막 업로드 시도한 POS LOG 키(seq-curdate-curtime) 저장
    fun setAgentLogUploadKey(ver:String){
        InIUtils.saveData(AgentIniUtil.Constant.FILE_SETTING_PATH, OPT_LOG_AGENT_UPLOAD_KEY, ver)
    }

    //마지막 업로드 시도한 POS LOG 키(seq-curdate-curtime)
    fun getAgentLogUploadKey():String{
        return InIUtils.loadData(
            AgentIniUtil.Constant.FILE_SETTING_PATH,
            OPT_LOG_AGENT_UPLOAD_KEY,
            ""
        )
    }


    fun getStrCd() = InIUtils.loadData(
        AgentIniUtil.Constant.FILE_SETTING_PATH, POS_STRCD,
        ""
    )

    fun getPosNo() = InIUtils.loadData(
        AgentIniUtil.Constant.FILE_SETTING_PATH, POS_NO,
        ""
    )


    fun isExistFile(path : String):Boolean {
        val iniFile = File(path)
        Log.i(TAG, "isExistFile, $iniFile")
        return iniFile.exists()
    }

   /*
    * 현재 날짜 및 시간 문자열(User pattern)
    */
    fun getCurrentDateTime(pattern: String): String {

        var formatted: String = ""
        var ouputPattern = "yyyyMMddHHmmss"

        if (pattern != null) ouputPattern = pattern

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val current = LocalDateTime.now().minusDays(0)
            val formatter = DateTimeFormatter.ofPattern(ouputPattern)
            formatted = current.format(formatter)
        } else {
            var date = Date()
            val formatter = SimpleDateFormat(ouputPattern)
            formatted = formatter.format(date)
        }

        return formatted
    }

}