package com.lotte.mart.messengerlib.messenger.constant

object MessengerConstant{

    //핸들러
    val FAILED: Int get() = 0                       //실패
    val SUCCEED: Int get() = 1                      //성공
    val POWER_OFF: Int get() = 101                  //전원 OFF
    val REBOOT: Int get() = 102                     //재부팅
    val APP_UPDATE: Int get() = 201                 //앱 업데이트
    val SET_IPADDRESS_BOUND: Int get() = 301        //아이피 허용 영역 설정
    val CMD_EXEC: Int get() = 401                   //CMD명령 실행
    val SET_TIME: Int get() = 501                   //시간설정
    val SET_DATETIME: Int get() = 502               //일시설정

    //응답 콜백 데이터 명
    val CALLBACK: String get() = "callback"

    //핸들러 데이터 명
    val DATA_VERSION: String get() = "VERSION"      //버전
    val DATA_IPS: String get() = "IPS"              //아이피 대역
    val DATA_RESULT: String get() = "RESULT"        //결과
    val DATA_CMD: String get() = "CMD"              //명령

    val DATA_YEAR: String get() = "YEAR"            //년
    val DATA_MONTH: String get() = "MONTH"          //월
    val DATA_DAY: String get() = "DAY"              //일
    val DATA_HOUR: String get() = "HOUR"            //시
    val DATA_MINUTE: String get() = "MINUTE"        //분
    val DATA_SECOND: String get() = "SECOND"        //초

    val DATA_CALENDAR: String get() = "CALENDAR"
    val DATA_DATE: String get() = "DATE"
    val DATA_HOST: String get() = "HOST"
}