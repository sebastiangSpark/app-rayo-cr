package com.rayo.rayoxml.cr.services.Loan

import com.google.gson.annotations.SerializedName


data class CreateLoanResponse(
    @SerializedName("Estado") val estado: String,
    @SerializedName("Id") val id: String,
    @SerializedName("monto") val monto: String,
    @SerializedName("interes") val interes: String,
    @SerializedName("tecno") val tecnologia: String,
    @SerializedName("descuento") val descuento: String,
    @SerializedName("aval") val aval: String,
    @SerializedName("iva") val iva: String,
    @SerializedName("totalPagar") val totalPagar: String,
    @SerializedName("servicioFE") val servicioFE: String,
    @SerializedName("Mensaje") val mensaje: String
)

data class CompleteLoanResponse(
    @SerializedName("Estado") val estado: String
)

data class CreateLoanMiniRenewalResponse(
    @SerializedName("Estado") val estado: String,
    @SerializedName("Id") val id: String,
    @SerializedName("monto") val monto: String,
    @SerializedName("interes") val interes: String,
    @SerializedName("tecno") val tecnologia: String,
    @SerializedName("descuento") val descuento: String,
    @SerializedName("plazo") val plazo: String,
    @SerializedName("aval") val aval: String,
    @SerializedName("iva") val iva: String,
    @SerializedName("totalPagar") val totalPagar: String,
    @SerializedName("servicioFE") val servicioFE: String,
    @SerializedName("ad") val ad: String,
    @SerializedName("cantidadPrestamos") val cantidadPrestamos: String,
    @SerializedName("tipoTecnologia") val tipoTecnologia: String,
    @SerializedName("descuentoWOW") val descuentoWOW: String,
    @SerializedName("Mensaje") val mensaje: String
)

data class CompleteLoanMiniRenewalResponse(
    @SerializedName("Estado") val estado: String
)

data class CreateLoanRpRenewalResponse(
    @SerializedName("Estado") val estado: String,
    @SerializedName("Id") val id: String,
    @SerializedName("monto") val monto: String,
    @SerializedName("interes") val interes: String,
    @SerializedName("tecno") val tecnologia: String,
    @SerializedName("descuento") val descuento: String,
    @SerializedName("plazo") val plazo: String,
    @SerializedName("aval") val aval: String,
    @SerializedName("iva") val iva: String,
    @SerializedName("totalPagar") val totalPagar: String,
    @SerializedName("servicioFE") val servicioFE: String,
    @SerializedName("Mensaje") val mensaje: String
)

data class CompleteLoanRpRenewalResponse(
    @SerializedName("Estado") val estado: String
)








data class LoanStepOneResponse(
    @SerializedName("Solicitud") val solicitud: Solicitud
)

data class LoanStepTwoResponse(
    @SerializedName("Solicitud") val solicitud: Solicitud
)

data class LoanValidationResponse(
    @SerializedName("Solicitud") val solicitud: Validacion
)

data class Solicitud(
    @SerializedName("CODIGO") val codigo: String,
    @SerializedName("FORMULARIO") val formulario: String?,
    @SerializedName("RESULT") val result: String?
)

data class Validacion(
    @SerializedName("CODIGO") val codigo: String,
    @SerializedName("FORMULARIO") val formulario: String?,
    @SerializedName("PROPUESTA") val propuesta: String?,
    @SerializedName("STEP") val step: String?,
    @SerializedName("SCOREEXPERIAN") val scoreExperian: String?,
    @SerializedName("RESULT") val result: String?
)

// Step 4 Load
data class LoanStepFourLoadResponse(
    @SerializedName("Formulario") val formulario: Formulario
)

data class Formulario(
    @SerializedName("CODIGO") val codigo: String,
    @SerializedName("FORMULARIO") val formulario: String?,
    @SerializedName("SHOW") val show: Boolean?,
    @SerializedName("VI") val vi: String?,
    @SerializedName("RESULT") val result: String?
)

// Step 4 Submit
data class LoanStepFourSubmitResponse(
    @SerializedName("Solicitud") val solicitud: Solicitud
)

// Step 5 plus Load
data class LoanStepFivePlusLoadResponse(
    @SerializedName("Solicitud") val solicitud: SolicitudStepFivePlusLoad
)

data class SolicitudStepFivePlusLoad(
    @SerializedName("CODIGO") val codigo: String,
    @SerializedName("FORMULARIO") val formulario: String?,
    @SerializedName("NOMBRE") val nombre: String?,
    @SerializedName("APELLIDO") val apellido: String?,
    @SerializedName("CELULAR") val celular: String?,
    @SerializedName("EMAIL") val email: String?,
    @SerializedName("CODIGOSEGURIDAD") val codigoSeguridad: String?,
    @SerializedName("REGISTROVALIDACION") val registroValidacion: String?,
    @SerializedName("IDTRANSACCION") val idTransaccion: String?
)

// Step 5 plus Load two
data class LoanStepFivePlusLoadTwoResponse(
    @SerializedName("Solicitud") val solicitud: SolicitudStepFivePlusLoadTwo
)

data class SolicitudStepFivePlusLoadTwo(
    @SerializedName("CODIGO") val codigo: String,
    @SerializedName("FORMULARIO") val formulario: String,
    @SerializedName("NOMBRE") val nombre: String,
    @SerializedName("APELLIDO") val apellido: String,
    @SerializedName("CELULAR") val celular: String,
    @SerializedName("DIRECCION") val direccion: String,
    @SerializedName("EMAIL") val email: String,
    @SerializedName("PLAZO") val plazo: Int,
    @SerializedName("BANCO") val banco: String,
    @SerializedName("CUENTABANCARIA") val cuentaBancaria: String,
    @SerializedName("PROPUESTASUGERIDA") val propuestaSugerida: String,
    @SerializedName("INTERES") val interes: String,
    @SerializedName("ADMINISTRACION") val administracion: String,
    @SerializedName("TECNOLOGIA") val tecnologia: String,
    @SerializedName("IVA") val iva: String,
    @SerializedName("FIANZA") val fianza: String,
    @SerializedName("CEDULA") val cedula: String,
    @SerializedName("PORCENTAJEFIANZA") val porcentajeFianza: String,
    @SerializedName("VI") val vi: String,
    @SerializedName("TOTAL") val total: String,
    @SerializedName("DESCUENTO") val descuento: String,
    @SerializedName("RESULT") val result: String,
    @SerializedName("DESEMBOLSOAUTO") val desembolsoAuto: String? = null
)

// Step 5 plus submit
data class LoanStepFivePlusSubmitResponse(
    //@SerializedName("Solicitud") val solicitud: SolicitudStepFivePlusSubmit
    @SerializedName("Solicitud") val solicitud: SolicitudStepFiveSubmit
)

data class SolicitudStepFivePlusSubmit(
    @SerializedName("CODIGO") val codigo: String,
    @SerializedName("FORMULARIO") val formulario: String?,
    @SerializedName("PROPUESTA") val propuesta: String?,
    @SerializedName("STEP") val step: String?,
    @SerializedName("SCOREEXPERIAN") val scoreExperian: String?,
    @SerializedName("RESULT") val result: String?
)

// Step 5 Load (primerizos)
data class LoanStepFiveLoadResponse(
    @SerializedName("Solicitud") val solicitud: SolicitudStepFiveLoad
)

data class SolicitudStepFiveLoad(
    @SerializedName("CODIGO") val codigo: String,
    @SerializedName("FORMULARIO") val formulario: String,
    @SerializedName("CHECKTECNOLOGIA") val checkTecnologia: String,
    @SerializedName("NOMBRE") val nombre: String,
    @SerializedName("APELLIDO") val apellido: String,
    @SerializedName("DIRECCION") val direccion: String,
    @SerializedName("TELEFONO") val telefono: String,
    @SerializedName("CELULAR") val celular: String,
    @SerializedName("EMAIL") val email: String,
    @SerializedName("CODIGOSEGURIDAD") val codigoSeguridad: String?,
    @SerializedName("REGISTROVALIDACION") val registroValidacion: String?,
    @SerializedName("IDTRANSACCION") val idTransaccion: String?,
    @SerializedName("CEDULA") val cedula: String,
    @SerializedName("PORCENTAJEFIANZA") val porcentajeFianza: String,
    @SerializedName("DESEMBOLSOAUTO") val desembolsoAuto: String? = null
)

// Step 5 submit (primerizos)
data class LoanStepFiveSubmitResponse(
    @SerializedName("Solicitud") val solicitud: SolicitudStepFiveSubmit
)

data class SolicitudStepFiveSubmit(
    @SerializedName("CODIGO") val codigo: String,
    @SerializedName("FORMULARIO") val formulario: String?,
    @SerializedName("PRESTAMO") val prestamo: String?,
    @SerializedName("RESULT") val result: String?
)

// Desembolso Tumipay
data class LoanTumiPayResponse(
    @SerializedName("Prestamo") val prestamo: PrestamoTumiPay
)

data class PrestamoTumiPay(
    @SerializedName("CODIGO") val codigo: String,
    @SerializedName("RESULT") val result: String?
)

// RENOVACION

// STEP4 mini Load
/*data class LoanRenewalStepFourMiniLoadResponse(
    @SerializedName("Solicitud") val solicitud: Solicitud
)*/

// STEP4 mini Load y STEP4 Plus Load
data class LoanRenewalProposalLoadResponse(
    @SerializedName("Solicitud") val solicitud: RenewalStepFourLoad
)

data class RenewalStepFourLoad(
    @SerializedName("CODIGO") val codigo: String,
    @SerializedName("PROPUESTASUGERIDA") val propuestaSugerida: String,
    @SerializedName("DESCUENTO") val descuento: String,
    @SerializedName("PORCENTAJEDESCUENTO") val porcentajeDescuento: String,
    @SerializedName("PAGOMES") val pagoMes: String,
    @SerializedName("SCORE") val score: String,
    @SerializedName("HASMORA") val hasMora: String,
    @SerializedName("SHOW") val show: Boolean,
    @SerializedName("RESULT") val result: String,
    // Campos sólo para step 4 mini
    @SerializedName("CANTIDAD") val cantidad: String? = null,
    @SerializedName("VALORINGRESO") val valorIngreso: String? = null,
    @SerializedName("CALIFICACION") val calificacion: String? = null,
)

// STEP4 mini Submit y STEP4 Plus Submit
data class LoanRenewalProposalSubmitResponse(
    @SerializedName("Solicitud") val solicitud: Solicitud
)

// Information step mini Load
data class LoanRenewalInfoLoadResponse(
    @SerializedName("Solicitud") val solicitud: RenewalInfo
)

data class RenewalInfo(
    @SerializedName("CODIGO") val codigo: String,
    @SerializedName("FORMULARIO") val formulario: String,
    @SerializedName("NOMBRE") val nombre: String,
    @SerializedName("APELLIDO") val apellido: String,
    @SerializedName("CELULAR") val celular: String,
    @SerializedName("DIRECCION") val direccion: String,
    @SerializedName("EMAIL") val email: String,
    @SerializedName("PLAZO") val plazo: String,
    @SerializedName("BANCO") val banco: String,
    @SerializedName("CUENTABANCARIA") val cuentaBancaria: String,
    @SerializedName("PROPUESTASUGERIDA") val propuestaSugerida: String,
    @SerializedName("INTERES") val interes: String,
    @SerializedName("ADMINISTRACION") val administracion: String,
    @SerializedName("TECNOLOGIA") val tecnologia: String,
    @SerializedName("IVA") val iva: String,
    @SerializedName("FIANZA") val fianza: String,
    @SerializedName("VI") val vi: String,
    @SerializedName("TOTAL") val total: String,
    @SerializedName("DESCUENTO") val descuento: String,
    @SerializedName("RESULT") val result: String
)

// PLP

// Step 1
data class PlpLoanStep1Response(
    @SerializedName("Solicitud") val solicitud: SolicitudPlpStep1
)

data class SolicitudPlpStep1(
    @SerializedName("CODIGO") val codigo: String,
    @SerializedName("NOMBRE") val nombre: String,
    @SerializedName("APELLIDO") val apellido: String,
    @SerializedName("NUMERODOCUMENTO") val numeroDocumento: String,
    @SerializedName("TIPODOCUMENTO") val tipoDocumento: String,
    @SerializedName("GENERO") val genero: String,
    @SerializedName("CORREO") val correo: String,
    @SerializedName("CAN_OTP") val canOtp: String,
    @SerializedName("BLOQUEO_OTP") val bloqueoOtp: String,
    @SerializedName("DEPENDIENTES") val dependientes: String,
    @SerializedName("HIJOS") val hijos: String,
    @SerializedName("ANTIGUEDAD") val antiguedad: String,
    @SerializedName("INGRESOS") val ingresos: String,
    @SerializedName("FECHAEXPEDICION") val fechaExpedicion: String,
    @SerializedName("CELULAR") val celular: String,
    @SerializedName("RESULT") val result: String
)

// STEP 2
data class PlpLoanStep2Response(
    @SerializedName("Solicitud") val solicitud: SolicitudPlpStep2
)

data class SolicitudPlpStep2(
    @SerializedName("CODIGO") val codigo: String,
    @SerializedName("FORMULARIO") val formulario: String?,
    @SerializedName("RESULT") val result: String?
)

// STEP 3
data class PlpLoanStep3Response(
    @SerializedName("Solicitud") val solicitud: SolicitudPlpStep3
)

data class SolicitudPlpStep3(
    @SerializedName("CODIGO") val codigo: String,
    @SerializedName("FORMULARIO") val formulario: String?,
    @SerializedName("PROPUESTA") val propuesta: String,
    @SerializedName("SCORE") val score: String,
    @SerializedName("HASMORA") val hasMora: String,
    @SerializedName("RESULT") val result: String
)

data class PlpLoanStep3ErrorResponse(
    val Estado: String,
    val Mensaje: String
)

// STEP 4
data class PlpLoanStep4Response(
    @SerializedName("Solicitud") val solicitud: SolicitudPlpStep4
)

data class SolicitudPlpStep4(
    @SerializedName("CODIGO") val codigo: String,
    @SerializedName("FORMULARIO") val formulario: String?,
    @SerializedName("VI") val vi: String?,
    @SerializedName("RESULT") val result: String?
)

// STEP 5
data class PlpLoanStep5Response(
    @SerializedName("Solicitud") val solicitud: SolicitudPlpStep5
)

// PENDIENTE VALIDAR ***********
data class SolicitudPlpStep5(
    @SerializedName("CODIGO") val codigo: String,
    @SerializedName("FORMULARIO") val formulario: String?,
    @SerializedName("RESULT") val result: String?
)

data class PlpLoanStep5ErrorResponse(
    @SerializedName("Solicitud") val solicitud: SolicitudPlpStep5Error
)

data class SolicitudPlpStep5Error(
    @SerializedName("CODIGO") val codigo: String,
    @SerializedName("FORMULARIO") val formulario: String?,
    @SerializedName("RESULT") val result: String?
)

// OTP

data class OTPVerifyResponse(
    @SerializedName("Solicitud") val solicitud: SolicitudOTP
)

data class OTPProcessResponse(
    @SerializedName("Solicitud") val solicitud: SolicitudOTP
)

data class SolicitudOTP(
    @SerializedName("CODIGO") val codigo: String,
    @SerializedName("FORMULARIO") val formulario: String?,
    @SerializedName("RESULT") val result: String?,
    @SerializedName("RESULTADOVALIDACION") val resultadoValidacion: String? = "",
)

// PREGUNTAS

data class Pregunta(
    val ordenPregunta: Int,
    val pregunta: String
)

// Obtener
data class QuestionResponse(
    @SerializedName("CODIGO") val codigo: String,
    @SerializedName("preguntas") val preguntas: List<Pregunta>
)

// Validar
data class QuestionValidationResponse(
    @SerializedName("Solicitud") val solicitud: SolicitudValidacionRespuestas
)

data class SolicitudValidacionRespuestas(
    @SerializedName("CODIGO") val codigo: String,
    @SerializedName("mensaje") val mensaje: String
)

// Verificar
data class QuestionVerificationResponse(
    @SerializedName("Solicitud") val solicitud: SolicitudVerificacionRespuestas
)

data class SolicitudVerificacionRespuestas(
    @SerializedName("CODIGO") val codigo: String,
    @SerializedName("RESULTADOCUESTIONARIO") val resultado: String
)