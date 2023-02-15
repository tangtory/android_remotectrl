package com.lotte.mart.vncserver.data

import com.google.gson.annotations.SerializedName

//전문 헤더
data class Header(
    @SerializedName("Class")
    var Class : String,
    @SerializedName("Func")
    var Func : String,
    @SerializedName("RespCd")
    var RespCd : String
)