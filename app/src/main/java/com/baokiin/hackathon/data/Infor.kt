package com.baokiin.hackathon.data

import com.google.gson.annotations.SerializedName

data class Info(
    @SerializedName("avatar_url") val avatar:String,
    @SerializedName("login") val login:String,
)
data class InfoResponse(
    @SerializedName("items") val items:List<Info>,
)
