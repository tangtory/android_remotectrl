package com.lotte.mart.vncserver.data

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class Response(
    @SerializedName("Header")
    @Expose
    var header: Header? = null,
    @SerializedName("Body")
    @Expose
    var responseBody: ResponseBody? = null
){
    companion object {
        val CODE_OK: String = "0000"           //정상 응답 코드
        val CODE_PARSING_ERR: String = "5001"  //파싱 에러 응답 코드
        val CODE_ERR: String = "9999"          //기타 에러 응답 코드
    }
}