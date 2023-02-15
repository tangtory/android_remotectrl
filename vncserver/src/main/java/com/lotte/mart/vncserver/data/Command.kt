package com.lotte.mart.vncserver.data

import com.google.gson.annotations.SerializedName

data class Command (
    @SerializedName("CmdGbn")
    var CmdGbn : String? = null,
    @SerializedName("PosSx")
    var PosSx : String? = null,
    @SerializedName("PosSy")
    var PosSy : String? = null,
    @SerializedName("PosEx")
    var PosEx : String? = null,
    @SerializedName("PosEy")
    var PosEy : String? = null,
    @SerializedName("Msg")
    var Msg : String? = null,
    @SerializedName("Qrate")
    var Qrate : String? = null,
    @SerializedName("Hwkey")
    var Hwkey : String? = null
){
    companion object {
        val CMD_SINGLE_CLICK: String = "0001"          //터치
        val CMD_SLIDE: String = "0002"                 //슬라이드(스와이프)
        val CMD_LONG_CLICK: String = "0003"            //롱터치
        val CMD_SHUTDOWN: String = "0004"              //기기 종료
        val CMD_REBOOT: String = "0005"                //기기 재시작
        val CMD_POS_APP_FINISH: String = "0006"        //포스 앱 종료
        val CMD_SYSINFO: String = "0007"               //시스템 정보
        val CMD_MSG: String = "0008"                   //메시지
        val CMD_LOCK: String = "0009"                  //스크린 Lock
        val CMD_QRATE: String = "0010"                  //압축 품질
        val CMD_HWKEY: String = "0011"                  //하드웨어 키
        val CMD_KBKEY: String = "0012"                  //하드웨어 키

        val QRATE_LOW: String = "00"                    //압축(저화질)
        val QRATE_MID: String = "01"                    //압축(중화질)
        val QRATE_HIG: String = "02"                    //압축(고화질)

        val HWKEY_MENU: String = "00"                   //물리키(앱관리)
        val HWKEY_HOME: String = "01"                   //물리키(홈키)
        val HWKEY_BACK: String = "02"                   //물리키(백키)
        val HWKEY_SCREEN: String = "03"                 //물리키(스크린 온오프)

        val KBKEY_BACKSPACE: String = "67"              //키보드(백스페이스)
        val KBKEY_ENTER: String = "66"                  //키보드(줄바꿈)
        val KBKEY_TAB: String = "61"                    //키보드(탭)
        val KBKEY_SPACE: String = "62"                  //키보드(스페이스)
        val KBKEY_SEMICOLON: String = "74"              //키보드(세미클론)
        val KBKEY_AT: String = "77"                     //키보드(@)
    }
}