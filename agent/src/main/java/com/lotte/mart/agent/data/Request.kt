package com.lotte.mart.agent.data

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class Request(
    @SerializedName("header")
    @Expose
    var header: Header? = null,
    @SerializedName("reqBody")
    @Expose
    var requestBody: RequestBody? = null
)