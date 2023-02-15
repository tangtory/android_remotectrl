package com.lotte.mart.vncserver.data

object Constant{
    //스키마 AGENT
    val SCHEMA_AGENT : String get() = "androidagent"
    //스키마 VNC
    val SCHEMA_VNC : String get() = "androidvnc"

    //스키마 HOST AGENT
    val HOST_AGENT : String get() = "agent"

    //TYPE 구분
    val TYPE : String get() = "type"
    //앱 종료 타입
    val TYPE_APPFINISH : String get() = "AppFinish"
    //앱 업데이트 완료 타입
    val TYPE_APPUPDATEFINISH : String get() = "AppUpdateFinish"
    //VNC 연결 정보 타입
    val TYPE_VNCCONNECTION_INFO : String get() = "VncConnectionInfo"
    //VNC SFTP 연결 정보 타입
    val TYPE_SFTPCONNECTION_INFO : String get() = "SftpConnectionInfo"

    //결과 구분
    val RESULT : String get() = "result"
    //성공 결과
    val RESULT_SUCCESS : String get() = "onSuccess"

    //정보
    val INFO : String get() = "info"
    //에이전트 스키마 URL
    val URL_AGENT : String get() = "$SCHEMA_AGENT://$HOST_AGENT"
    //에러
    val ERROR : String get() = "ERROR"

    //VNC 정보 URL 취득
    fun getAgentURLVncInfo(info:String) : String{
        return "$URL_AGENT?" +
                "$TYPE=$TYPE_VNCCONNECTION_INFO&" +
                "$INFO=$info"
    }

    //VNC SFTP URL 취득
    fun getAgentURLSftpInfo(info:String) : String{
        return "$URL_AGENT?" +
                "$TYPE=$TYPE_SFTPCONNECTION_INFO&" +
                "$INFO=$info"
    }
}