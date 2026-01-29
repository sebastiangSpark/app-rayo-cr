package com.rayo.rayoxml.co.services.Payment

import com.google.gson.annotations.SerializedName

data class PaymentLinkResponse(
    @SerializedName("Solicitud") val solicitud: LinkData
)

data class LinkData(
    @SerializedName("CODIGO") val codigo: String,
    @SerializedName("RESULT") val result: String,
    @SerializedName("LINKPAGO") val linkPago: String? = "",
    @SerializedName("DO") val accion: String? = ""
)