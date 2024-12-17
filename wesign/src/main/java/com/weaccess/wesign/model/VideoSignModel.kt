package com.weaccess.wesign.model

import com.google.gson.annotations.SerializedName

data class VideoSignModel (
    @SerializedName("data"   ) var data   : ArrayList<SignModel> = arrayListOf(),
    @SerializedName("status" ) var status : Boolean?        = null
)

data class SignModel (
    @SerializedName("st" ) var st : Double? = null,
    @SerializedName("et" ) var et : Double? = null,
    @SerializedName("vu" ) var vu : String? = null,
    @SerializedName("vd" ) var vd : Double? = null,
    @SerializedName("s"  ) var s  : String? = null,
    @SerializedName("q"  ) var q  : Int?    = null
)