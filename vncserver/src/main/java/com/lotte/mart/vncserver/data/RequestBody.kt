package com.lotte.mart.vncserver.data

import com.google.gson.annotations.SerializedName

//요청 바디
data class RequestBody(
    @SerializedName("Command")
    var Command : Command
)