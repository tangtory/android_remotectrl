package com.lotte.mart.vncserver.data

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

//요청 전문 구성 (헤더, 요청 바디)
data class Request(
    @SerializedName("Header")
    @Expose
    var header: Header? = null,
    @SerializedName("Body")
    @Expose
    var requestBody: RequestBody? = null
)