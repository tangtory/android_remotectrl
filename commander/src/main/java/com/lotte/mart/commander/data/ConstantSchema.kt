package com.lotte.mart.commander.data

object ConstantSchema{
    val SCHEMA_PDA : String get() = "cloudpos"                //PDA <-> Agent간 PDA 스키마
    val SCHEMA_REMOTE : String get() = "androidremote"          //PDA <-> Agent간 Agent 스키마
    val SCHEMA_AGENT : String get() = "androidagent"            //Commander,Vnc <-> Agent간 Agent 스키마
    val SCHEMA_VNC : String get() = "androidvnc"                //Vnc 스키마
    val SCHEMA_COMMANDER : String get() = "androidcommander"    //Commander 스키마

    val HOST : String get() = "host"                    //스키마 host 필드명
    val HOST_PDA : String get() = "pda"                 //스키마 pda host
    val HOST_REMOTE : String get() = "remote"           //스키마 remote(agent) host
    val HOST_AGENT : String get() = "agent"             //스키마 agent
    val HOST_VNC : String get() = "vnc"                 //스키마 vnc
    val HOST_COMMANDER : String get() = "commander"     //스키마 commander

    val TYPE : String get() = "type"                    //스키마 타입
    val TYPE_CMD : String get() = "Cmd"                 //타입 필드명 - 명령
    val TYPE_SETTIME : String get() = "SetTime"         //타입 필드명 - 시간설정
    val TIME : String get() = "time"                    //시간설정 필드명
    val CMD : String get() = "cmd"                      //명령 필드명
    val RESULT : String get() = "result"                //결과 필드명
    val RESULT_RESPONSE : String get() = "onResponse"   //결과값 응답
    val RESULT_SUCCESS : String get() = "onSuccess"     //결과값 성공
    val RESULT_FAIL : String get() = "onFail"           //결과값 실패
    val DATA : String get() = "data"                    //데이터 필드명

    //PDA URL
    val URL_PDA : String get() = "$SCHEMA_PDA://$HOST_PDA"
    //Remote(agent pos용) URL
    val URL_REMOTE : String get() = "$SCHEMA_REMOTE://$HOST_REMOTE"
    //agent URL
    val URL_AGENT : String get() = "$SCHEMA_AGENT://$HOST_AGENT"
    //vnc URL
    val URL_VNC : String get() = "$SCHEMA_VNC://$HOST_VNC"
    //commander URL
    val URL_COMMANDER : String get() = "$SCHEMA_COMMANDER://$HOST_COMMANDER"

    //핸들러 구분값
    val HANDLER_SETTIME : Int get() = 1001  //시간설정
    val HANDLER_CMD : Int get() = 1002      //명령수행
    val HANDLER_ERROR : Int get() = 9999    //에러

    val ERROR : String get() = "ERROR"  //에러

    /**
     * 스키마 URL 취득
     * @param host - 스키마 host
     */
    private fun getSchemaURL(host:String) : String{
        when(host){
            HOST_PDA->{
                return URL_PDA
            }
            HOST_REMOTE->{
                return URL_REMOTE
            }
            HOST_AGENT->{
                return URL_AGENT
            }
            HOST_VNC->{
                return URL_VNC
            }
            HOST_COMMANDER->{
                return URL_COMMANDER
            }
            else->{
                return ERROR
            }
        }
    }

    /**
     * 시간 설정 스키마 url
     * @param host - 스키마 host
     * @param result - 결과값
     */
    fun getSetTimeURL(host: String, result:String) : String {
        val URL = getSchemaURL(host)
        if(URL == ERROR)
            return ERROR

        return "$URL?" +
                "$TYPE=$TYPE_SETTIME&" +
                "$RESULT=$result"
    }

    /**
     * 명령 수행 스키마 url
     * @param host - 스키마 host
     * @param result - 결과값
     * @param data - 데이터
     * @param cmd - 명령
     */
    fun getCmdURL(host: String, result:String, data:String, cmd:String) : String {
        val URL = getSchemaURL(host)
        if(URL == ERROR)
            return ERROR

        return "$URL?" +
                "$TYPE=$TYPE_CMD&" +
                "$RESULT=$result&" +
                "$DATA=$data&" +
                "$CMD=$cmd"
    }
}