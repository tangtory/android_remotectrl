package com.lotte.mart.vncserver.data

import com.google.gson.annotations.SerializedName

data class ResponseBody(
    @SerializedName("Sysinfo")
    var Sysinfo : Sysinfo
)