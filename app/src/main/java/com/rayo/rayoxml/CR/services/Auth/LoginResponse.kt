package com.rayo.rayoxml.cr.services.Auth

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
//import com.rayo.rayoxml.cr.services.User.Prestamo
import kotlinx.parcelize.Parcelize

data class AuthResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("instance_url") val instanceUrl: String,
    @SerializedName("id") val id: String,
    @SerializedName("token_type") val tokenType: String,
    @SerializedName("issued_at") val issuedAt: String,
    @SerializedName("signature") val signature: String,
)

data class LoginResponse(
    @SerializedName("id") val id: String,
    @SerializedName("salarioReportado") val salario: String,
    @SerializedName("provincia") val provincia: String,
    @SerializedName("profesion") val profesion: String,
    @SerializedName("plazo") val plazo: String,
    @SerializedName("phone") val telefono: String,
    @SerializedName("pais") val pais: String,
    @SerializedName("ObligarReseteo") val obligarReseteo: String,
    @SerializedName("numPrestamosFirmados") val numPrestamosFirmados: String,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("moraMayorMiniPrestamos") val moraMayorMiniPrestamos: String,
    @SerializedName("mobilePhone") val celular: String = "",
    @SerializedName("lugarDeTrabajo") val lugarDeTrabajo: String,
    @SerializedName("fechaExpiracionCedula") val fechaExpiracionCedula: String,
    @SerializedName("email") val email: String,
    @SerializedName("distrito") val distrito: String,
    @SerializedName("DisponibleRPL") val DisponibleRPL: String,
    @SerializedName("direccionExacta") val direccionExacta: String,
    @SerializedName("DescuentoWOW") val descuentoWOW: String,
    @SerializedName("cuentaIban") val cuentaIban: String? = null,
    @SerializedName("codigoSalida") val codigoSalida: String,
    @SerializedName("Cobranza") val cobranza: String? = null,
    @SerializedName("cedula") val cedula: String,
    @SerializedName("caton") val canton: String,
    @SerializedName("cantidadPrestamosRPLS") val cantidadPrestamosRPLS: String,
    @SerializedName("CantidadPrestamosMiniPlus") val CantidadPrestamosMiniPlus: String,
    @SerializedName("Avatar") val avatar: String? = null,
    @SerializedName("actividadEconomica") val actividadEconomica: String,
    @SerializedName("prestamos") val prestamos: List<Prestamo>?,
    @SerializedName("prestamosRP") val prestamosRP: List<Prestamo>?,
    @SerializedName("prestamosPLP") val prestamosPLP: List<Prestamo>?,
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

@Parcelize
data class Prestamo(
    @Transient var tipo: String = "", // Generado en App para control interno
    @SerializedName("totalPagar") val totalPagar: String = "",
    @SerializedName("tipoDescuento") val tipoDescuento: String = "",
    @SerializedName("tecnologia") val tecnologia: String = "",
    @SerializedName("plazo") val plazo: String = "",
    @SerializedName("pagos") val pagos: List<Pago> = emptyList(),
    @SerializedName("montoPrestamo") val montoPrestado: String = "",
    @SerializedName("iva") val iva: String? = null,
    @SerializedName("interes") val intereses: String = "",
    @SerializedName("fechaDeposito") val fechaDeposito: String = "",
    @SerializedName("fecha1") val fecha1: String = "",
    @SerializedName("fecha2") val fecha2: String = "",
    @SerializedName("fecha3") val fecha3: String = "",
    @SerializedName("descuento") val descuento: String = "",
    @SerializedName("codigoPrestamo") val codigoPrestamo: String = "",
    @SerializedName("aval") val aval: String = ""
): Parcelable

@Parcelize
data class Pago(
    @SerializedName("totalMora") val totalMora: String = "",
    @SerializedName("id") val id: String = "",
    @SerializedName("name") val codigo: String = "",
    @SerializedName("mora") val mora: String = "",
    @SerializedName("montoPagar") val montoPagar: String = "",
    @SerializedName("estado") val estado: String = "",
    @SerializedName("fechaPago") val fechaPago: String = ""
): Parcelable