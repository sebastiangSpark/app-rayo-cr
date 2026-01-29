package com.rayo.rayoxml.cr.services.User

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

data class UserResponse(
    @SerializedName("Solicitud") val solicitud: Solicitud
)

data class Solicitud(
    @SerializedName("CODIGO") val codigo: String,
    @SerializedName("CONTACTO") val contacto: String?,
    @SerializedName("NOMBRE") val nombre: String?,
    @SerializedName("EMAIL") val email: String?,
    @SerializedName("DOCUMENTO") val documento: String?,
    @SerializedName("APELLIDOS") val apellidos: String?,
    @SerializedName("CUENTA") val numeroCuenta: String?,
    @SerializedName("TIPOCUENTA") val tipoCuenta: String?,
    @SerializedName("AVATAR") val avatar: String?,
    @SerializedName("BANCO") val banco: String?,
    @SerializedName("CUENTAPRINCIPAL") val cuentaPrincipal: String?,
    @SerializedName("TIPOCUENTAPRINCIPAL") val tipoCuentaPrincipal: String?,
    @SerializedName("BANCOPRINCIPAL") val bancoPrincipal: String?,
    @SerializedName("ACTUALIZACUENTAPRINCIPAL") val actualizaCuentaPrincipal: String?,
    @SerializedName("NEQUIODAVIPLATAMARCADO") val nequiodaviplatamarcado: String?,
    @SerializedName("DESEMBOLSOREALIZADO") val desembolsorealizado: String?,
    @SerializedName("HISTORICONEQUIORDAVIPLATA") val historiconequiordaviplata: Boolean? = false,
    @SerializedName("LASTTIPOPRESTAMO") val lastTipoPrestamo: String?,
    @SerializedName("LASTFRMPRESTAMO") val lastFrmPrestamo: String?,
    @SerializedName("HASMORAPLP15DIAS") val HasMoraPlp15Dias: Boolean?,
    @SerializedName("TIPODOCUMENTO") val tipoDocumento: String?,
    @SerializedName("CELULAR") val celular: String?,
    @SerializedName("MORA") val mora: String?,
    @SerializedName("ESTADOCARTERAMORA") val estadoCarteraMora: Int?,
    @SerializedName("BOTONPLP") val BotonPLP: String?,
    @SerializedName("BOTONRP") val BotonRP: String?,
    @SerializedName("DESCUENTOADMINISTRATIVO") val descuentoAdministrativo: String?,
    @SerializedName("DESCUENTO10") val descuento10: String?,
    @SerializedName("CALIFICACION") val calificacion: String?,
    @SerializedName("TMAS9") val tmas9: String?,
    @SerializedName("FECHAINICIAL") val fechaInicial: String?,
    @SerializedName("PENDIENTES") val pendientes: String?,
    @SerializedName("PENDIENTESPLP") val pendientesPlp: String?,
    @SerializedName("NOLOCALIZADO") val noLocalizado: String?,
    @SerializedName("REFERENCIAPERSONAL") val referenciaPersonal: String?,
    @SerializedName("CELULARREFERENCIAPERSONAL") val celularReferenciaPersonal: String?,
    @SerializedName("BLOQUEADOCANTILLANO") val bloqueadoCantillano: Boolean?,
    @SerializedName("BLOQUEADOSINRENOVACION") val bloqueadoSinRenovacion: Boolean?,
    @SerializedName("MOSTRAR_TUMIPAY") val mostrarTumipay: Boolean?,
    @SerializedName("PRESTAMOS") val prestamos: List<Prestamo>?,
    @SerializedName("PRESTAMOSRP") val prestamosRP: List<Prestamo>?,
    @SerializedName("PRESTAMOSPLP") val prestamosPLP: List<Prestamo>?,
    @SerializedName("PRESTAMOSEXTRANJERO") val prestamosExtranjeros: List<Prestamo>?,
    @SerializedName("RESULT") val result: String?
)
@Parcelize
data class Prestamo(
    @Transient var tipo: String = "", // Generado en App para control interno
    @SerializedName("PRESTAMO") val prestamoId: String = "",
    @SerializedName("CODIGO") val codigo: String = "",
    @SerializedName("SALDO") val saldo: String = "",
    @SerializedName("PLAZO") val plazo: String = "",
    @SerializedName("DESCUENTO") val descuento: String = "",
    @SerializedName("MONTOPRESTADO") val montoPrestado: String = "",
    @SerializedName("FECHADESEMBOLSO") val fechaDesembolso: String = "",
    @SerializedName("NUMEROCUOTAS") val numeroCuotas: String = "",
    @SerializedName("ESTADO") val estado: String = "",
    @SerializedName("TECNOLOGIA") val tecnologia: String = "",
    @SerializedName("ADMINISTRACION") val administracion: String = "",
    @SerializedName("TOTALPAGARTYC") val totalPagarTYC: String = "",
    @SerializedName("TOTALAPAGARDESPUESDESEMBOLSO") val totalAPagarDespuesDesembolso: String = "",
    @SerializedName("SUMAINTERESESMORA") val sumaInteresesMora: String = "",
    @SerializedName("SALDOREAL") val saldoReal: String = "",
    @SerializedName("SALDOINTERESESMORA") val saldoInteresesMora: String = "",
    @SerializedName("GASTOSCOBRANZA") val gastosCobranza: String = "",
    @SerializedName("GASTOSCOBRANZACONDESCUENTO") val gastosCobranzaConDescuento: String = "",
    @SerializedName("IVAGASTOSCOBRANZA") val ivaGastosCobranza: String = "",
    @SerializedName("TOTALPAGARFINALGAC") val totalPagarFinalGAC: String = "",
    @SerializedName("IVA") val iva: String = "",
    @SerializedName("INTERESES") val intereses: String = "",
    @SerializedName("FIANZAEIVA") val fianzaEIVA: String = "",
    @SerializedName("DESCUENTOAPLICADO") val descuentoAplicado: String = "",
    @SerializedName("PAGOS") val pagos: List<Pago> = emptyList(),
    @SerializedName("CUOTASPENDIENTES") val cuotasPendientes: String = ""
): Parcelable

@Parcelize
data class Pago(
    @SerializedName("PAGO") val pagoId: String = "",
    @SerializedName("CODIGO") val codigo: String = "",
    @SerializedName("ESTADO") val estado: String = "",
    @SerializedName("FECHAPAGO") val fechaPago: String = "",
    @SerializedName("MONTOPAGOACTUAL") val montoPagoActual: String = "",
    @SerializedName("MONTOPAGAR") val montoPagar: String = "",
    @SerializedName("MONTOBRUTOPRESTADO") val montoBrutoPrestado: String = "",
    @SerializedName("TECNOLOGIA") val tecnologia: String = "",
    @SerializedName("ADMINISTRACION") val administracion: String = "",
    @SerializedName("IVA") val iva: String = "",
    @SerializedName("INTERESESCORRIENTES") val interesesCorrientes: String = "",
    @SerializedName("DESCUENTOCLIENTE") val descuentoCliente: String = "",
    @SerializedName("TOTALPAGARTYC") val totalPagarTYC: String = "",
    @SerializedName("INTERESESMORA") val interesesMora: String = "",
    @SerializedName("SALDOREAL") val saldoReal: String = "",
    @SerializedName("GASTOSCOBRANZA") val gastosCobranza: String = "",
    @SerializedName("IVAGASTOSCOBRANZA") val ivaGastosCobranza: String = "",
    @SerializedName("GASTOSCOBRANZACONDESCUENTO") val gastosCobranzaConDescuento: String = "",
    @SerializedName("TOTALPAGARGAC") val totalPagarGAC: String = ""
): Parcelable
