package com.lotte.mart.vncserver.utils

import android.content.Context
import android.os.Environment
import android.util.Log
import com.lotte.mart.vncserver.utils.AgentIniUtil.Constant.FILE_SETTING_PATH

import com.lotte.mart.commonlib.utility.InIUtils
import com.lotte.mart.vncserver.service.DataParser.Companion.TAG
import com.lotte.mart.vncserver.utils.AgentIniUtil.Constant.settingJson
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**
 * AgentConfig.ini 유틸
 */
class AgentIniUtil {
    lateinit var path:String
    object Constant{
        //로컬 로그 디렉토리 경로
        val PATH_LOCAL_LOG  = Environment.getExternalStorageDirectory().toString() + "/lottepos/log/"

        const val settingJson = "setting.json"
        const val FILE_ROOT_PATH = "/lottepos"
        const val FILE_DIR_SETTING = "/setting"
        val FILE_SETTING_PATH = Environment.getExternalStorageDirectory().toString() + FILE_ROOT_PATH + FILE_DIR_SETTING + File.separator + settingJson
        //POS 패키지명
        const val POS_PACKAGE  = "posPackage"
        //원격 접속 포트
        const val VNC_PORT = "vncPort"
        //원격 접속 비밀번호
        const val VNC_PWD  = "vncPwd"
        //SFTP 접속 포트
        const val VNC_SFTP_PORT  = "sftpPort"
        const val PREF_KEY_MY_POS_MFR = "manufacturer"

        //로그파일 디렉토리 경로
        const val LOG_PATH = "logPath"
        const val DB_LOG_DAYS  = "dbLogDays"
        const val SFTP_ID   = "sftpId"
        const val SFTP_PWD   = "sftpPwd"


    }

    companion object {
        @Volatile
        private var instance : AgentIniUtil? = null

        fun getInstance(context : Context) : AgentIniUtil =
            instance ?: synchronized(AgentIniUtil::class.java) {
                instance ?: AgentIniUtil().also {
                    instance = it
                    instance!!.path = instance!!.copyToCache(context, FILE_SETTING_PATH)
                }
            }
    }

    //AgentConfig.ini 파일을 캐시로 복사
    private fun copyToCache(context: Context, path:String):String{
        if(!isExistJson()){
            return ""
        }

        return try {
            val filePath: Path = Paths.get(path)
            val filePathToMove: Path = Paths.get("${context.cacheDir.path}/${settingJson}")
            Log.i(TAG, "ini 파일 $filePath, 이동 경로 $filePathToMove")
            if(Files.exists(filePathToMove)){
                //중복 파일 존재시 삭제
                Files.delete(filePathToMove)
            }

            if(Files.exists(filePath)) {
                Files.copy(filePath, filePathToMove)
                Log.i(TAG, "ini 캐시 파일 생성")
            }

            "${context.cacheDir.path}/${settingJson}"
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e(TAG, "ini 캐시 파일 생성 실패", e)
            ""
        }
    }

    //AgentConfig.ini 파일 존재 여부
    fun isExistJson():Boolean {
        return InIUtils.isExist(FILE_SETTING_PATH)
    }

    //AgentConfig.ini 캐시 파일 존재 여부
    fun isExistJsonCache():Boolean {
        return InIUtils.isExist(path)
    }

    //데이터 체크
    fun hasData():Boolean{
        if(isExistJsonCache()){
            if(getPosPackageName().isEmpty())
                return false
            if(getVncPort("31079").isEmpty())
                return false
            if(getVncPwd("3333").isEmpty())
                return false
            if(getVncSftpId("lotteds").isEmpty())
                return false
            if(getVncSftpPort("2222").isEmpty())
                return false
            if(getVncSftpPwd("1234").isEmpty())
                return false
            if(getLogPath(Constant.PATH_LOCAL_LOG).isEmpty())
                return false
            return true
        }

        return false
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
     * POS 앱 패키지명
     */
    fun getPosPackageName(default: String = "com.lotte.mart.cloudpos"):String{
        return InIUtils.loadData(Constant.FILE_SETTING_PATH, Constant.POS_PACKAGE,"com.lotte.mart.cloudpos")
    }


    /**
     * VNC 접속 포트
     */
    fun getVncPort(default: String):String{
        return InIUtils.loadData(FILE_SETTING_PATH, Constant.VNC_PORT,default)
    }

    /**
     * VNC 접속 패스워드
     */
    fun getVncPwd(default: String):String{
        return InIUtils.loadData(FILE_SETTING_PATH, Constant.VNC_PWD,default)
    }

    /**
     * VNC Sftp 포트
     */
    fun getVncSftpPort(default: String):String{
        return InIUtils.loadData(FILE_SETTING_PATH, Constant.VNC_SFTP_PORT,default)
    }

    /**
     * VNC Sftp 접속 아이디
     */
    fun getVncSftpId(default: String):String{
        return InIUtils.loadData(FILE_SETTING_PATH, Constant.SFTP_ID,default)
    }

    /**
     * VNC Sftp 접속 패스워드
     */
    fun getVncSftpPwd(default: String):String{
        return InIUtils.loadData(FILE_SETTING_PATH, Constant.SFTP_PWD,default)
    }




}