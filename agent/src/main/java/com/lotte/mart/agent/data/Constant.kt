package com.lotte.mart.agent.data

object Constant{
    val SCHEMA_PDA : String get() = "cloudpos"
    val SCHEMA_REMOTE : String get() = "androidremote"
    val SCHEMA_AGENT : String get() = "androidagent"
    val SCHEMA_VNC : String get() = "androidvnc"
    val SCHEMA_COMMANDER : String get() = "androidcommander"

    val HOST : String get() = "host"
    val HOST_PDA : String get() = "pda"
    val HOST_REMOTE : String get() = "deeplink"
    val HOST_AGENT : String get() = "agent"
    val HOST_VNC : String get() = "vnc"
    val HOST_COMMANDER : String get() = "commander"

    val TYPE : String get() = "type"
    val TYPE_CMD : String get() = "Cmd"
    val TYPE_APPUPDATE : String get() = "AppUpdate"
    val TYPE_APPFINISH : String get() = "AppFinish"
    val TYPE_APPUPDATEFINISH : String get() = "AppUpdateFinish"
    val TYPE_VNCCONNECTION_INFO : String get() = "VncConnectionInfo"
    val TYPE_SFTPCONNECTION_INFO : String get() = "SftpConnectionInfo"

    val TYPE_SETTIME : String get() = "SetTime"
    val TYPE_REBOOT : String get () = "Reboot"
    val REBOOT : String get() = "reboot"
    val TIME : String get() = "time"
    val FILE : String get() = "file"
    val CMD : String get() = "cmd"
    val DATA : String get() = "data"
    val RESULT : String get() = "result"
    val RESULT_SUCCESS : String get() = "onSuccess"
    val RESULT_FAIL : String get() = "onFail"
    val INFO : String get() = "info"

    val URL_PDA : String get() = "$SCHEMA_PDA://$HOST_REMOTE"
    val URL_AGENT : String get() = "$SCHEMA_AGENT://$HOST_AGENT"
    val URL_VNC : String get() = "$SCHEMA_VNC://$HOST_VNC"
    val ERROR : String get() = "ERROR"

    fun getUpdateFinishURL(result:String) : String{
        return "$URL_PDA?" +
                "$TYPE=$TYPE_APPUPDATEFINISH&" +
                "$RESULT=$result"
    }

    fun getAgentUpdateFinishURL(result:String) : String{
        return "$URL_AGENT?" +
                "$TYPE=$TYPE_APPUPDATEFINISH&" +
                "$RESULT=$result"
    }

    fun getVncUpdateFinishURL(result:String) : String{
        return "$URL_VNC?" +
                "$TYPE=$TYPE_APPUPDATEFINISH&" +
                "$RESULT=$result"
    }

    fun getVncFinishURL() : String{
        return "$URL_VNC?" +
                "$TYPE=$TYPE_APPFINISH"
    }

    fun getFinishURL() : String{
        return "$URL_PDA?" +
                "$TYPE=$TYPE_APPFINISH"
    }

    fun getSetTimeURL(result:String) : String {
        return "$URL_PDA?" +
                "$TYPE=$TYPE_SETTIME&" +
                "$RESULT=$result"
    }

    fun getRebootURL(reboot:Boolean) : String {
        return "$URL_PDA?" +
                "$TYPE=$TYPE_REBOOT&" +
                "$REBOOT=$reboot"
    }
}