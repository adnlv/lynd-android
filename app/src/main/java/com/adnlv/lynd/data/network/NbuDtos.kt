package com.adnlv.lynd.data.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NbuPaymentDto(
    @SerialName("pay_date")
    val payDate: String? = null,

    @SerialName("pay_type")
    val payType: String? = null,

    @SerialName("pay_val")
    val payVal: Double? = null
)

@Serializable
data class NbuSecurityDto(
    @SerialName("cpcode")
    val cpcode: String? = null,

    @SerialName("nominal")
    val nominal: Double? = null,

    @SerialName("auk_proc")
    val aukProc: Double? = null,

    @SerialName("pgs_date")
    val pgsDate: String? = null,

    @SerialName("val_code")
    val valCode: String? = null,

    @SerialName("emit_name")
    val emitName: String? = null,

    @SerialName("payments")
    val payments: List<NbuPaymentDto>? = null
)
