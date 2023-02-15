package com.lotte.mart.agent.data

import com.google.gson.annotations.SerializedName

data class RequestBody(
    @SerializedName("input")
    var input : Input
)