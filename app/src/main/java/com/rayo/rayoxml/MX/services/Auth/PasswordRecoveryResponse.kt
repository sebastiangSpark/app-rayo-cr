package com.rayo.rayoxml.mx.services.Auth

import com.google.gson.annotations.SerializedName

data class PasswordRecoveryResponse(
    @SerializedName("codigo") val codigo: String,
    @SerializedName("mensajeSalida") val mensajeSalida: String,
    @SerializedName("correo") val correo: String = ""
)

data class PasswordRecoveryErrorResponse(
    val codigo: String,
    val mensajeSalida: String
)