package com.rayo.rayoxml.mx.services.Auth

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("Solicitud") val solicitud: Solicitud
)

data class ErrorResponse(
    val errorCode: String,
    val message: String
)

data class Solicitud(
    @SerializedName("CODIGO") val codigo: String,
    @SerializedName("ACCESO") val acceso: String,
    @SerializedName("CONTACTO") val contacto: String? = null,
    @SerializedName("OBLIGAR_RESETEO") val obligarReseteo: Boolean? = null,
    @SerializedName("RESULT") val result: String
)