package com.rayo.rayoxml.mx.services.Loan

data class LoanStepOneRequest(
    val contacto: String = "",
    val nombre: String,
    val primerApellido: String,
    val numeroDocumento: String,
    val fechaNacimiento: String,
    val celular: String,
    var password: String? = null,
    val empleadoFormal: String,
    val nombreCuentaBancaria: String,
    val correoElectronico: String,
    var correoElectronico2: String? = null,
    //val referido_cliente_cedula: String? = "",
    //val referido_cliente: String? = "",
    //val referido_cliente_id: String? = "",
    val tipoDocumento: String,
    val plazoSeleccionado: Int,
    val valorSeleccionado: Int,
    //val ingresoMensual: Int? = null,
    val fechaExpedicion: String,
    val sessionId: String,
    val genero: String
)

data class LoanStepTwoRequest(
    val formulario: String,
    val departamento: String,
    val ciudadDepartamento: String,
    val direccionExacta: String,
    val banco: String,
    val tipoCuenta: String,
    val referenciaBancaria: String,
    val referenciaBancaria2: String,
    val plazoSeleccionado: Int,
    val telefonoEmpresa: String,
    val contadorActualizado: Int,
    val showModal: Boolean = false,
    val checkTecnologia: Boolean = false,
)

data class LoanValidationRequest(
    val formulario: String
)

// Step 4 Load
data class LoanStepFourLoadRequest(
    val formulario: String,
    //
)

// Step 4 Submit
data class LoanStepFourSubmitRequest(
    val formulario: String,
    val checkTecnologia: Boolean = false,
    val banco: String,
    val ciudadDepartamento: String,
    val departamento: String,
    val direccionExacta: String,
    val contadorActualizado: Int,
    val plazoSeleccionado: Int,
    val referenciaBancaria: String,
    val referenciaBancaria2: String,
    val telefonoEmpresa: String,
    val tipoCuenta: String,
    val showModal: Boolean = false,
    // Nuevos con respecto a LoanStepTwoRequest
    val debito: Boolean = true,
    val archivo: String? = null
)

// Step 5 plus Load y Load 2
data class LoanStepFivePlusLoadRequest(
    val formulario: String
)

// Step 5 plus Submit
data class LoanStepFivePlusSubmitRequest(
    val checkTecnologia: Boolean,
    val formulario: String,
    val plazoSeleccionado: Int
)

// Step 5 Load (primerizos)
data class LoanStepFiveLoadRequest(
    val formulario: String
)

// Step 5 Submit (primerizos)
data class LoanStepFiveSubmitRequest(
    val formulario: String,
    val plazoSeleccionado: Int
)

// Tumipay mini
data class LoanTumiPayRequest(
    val idPrestamo: String
)

// RENOVACION

// STEP4 mini Load y STEP4 Plus Load
data class LoanRenewalProposalLoadRequest(
    val idContacto: String
)

// STEP4 mini Submit
data class LoanRenewalProposalSubmitRequest(
    val checkTecnologia: Boolean = false,
    val plazoSeleccionado: Int,
    val showModal: Boolean = false,
    val debito: Boolean = true,
    val archivo: String? = null,
    val archivoContentType: String? = null,
    val archivoName: String? = null,
    val descuento10: String? = null,
    val descuentoAdministracion: String? = null,
    val idContacto: String,
    val montoDescuento: String,
    val porcentajeDescuento: Int,
    val propuestaSugerida: Int,
)

// Information mini Load (primerizos)
data class LoanRenewalInfoLoadRequest(
    val formulario: String
)

// STEP4 Submit ?
data class LoanRenewalStepFourSubmitRequest(
    val formulario: String,
    val checkTecnologia: Boolean = false,
    val banco: String,
    val ciudadDepartamento: String,
    val departamento: String,
    val direccionExacta: String,
    val contadorActualizado: Int,
    val plazoSeleccionado: Int,
    val referenciaBancaria: String,
    val referenciaBancaria2: String,
    val telefonoEmpresa: String,
    val tipoCuenta: String,
    val showModal: Boolean = false,
    val debito: Boolean = true,
    val archivo: String? = null
)

// STEP4 Plus Submit
data class LoanRenewalStepFourPlusSubmitRequest(
    val formulario: String? = null,
    val idContacto: String,
    val montoDescuento: String,
    val propuestaSugerida: String,
    val fianzaRP: Int,
    //val contadorActualizado: String,
    val descuento10: String,
    val showModal: Boolean = false,
    val debito: Boolean = true,
    val checkTecnologia: Boolean = false,
    // Adicionales web
    val plazoSeleccionado: Int = 0,
    val descuentoAdministracion: String = "No"
)

// PLP

// STEP 1
data class PLPLoanStep1Request(
    val contacto: String
)

// STEP 2
data class PLPLoanStep2Request(
    val antiguedadLaboral: String,
    val cantidadDependientes: Int,
    val cantidadHijos: Int,
    val celular: String,
    val contacto: String,
    val correoElectronico: String,
    val fechaExpedicion: String,
    val ingresoMensual: String,
    val nombre: String,
    val numeroDocumento: String,
    val password: String,
    val primerApellido: String,
    val tipoDocumento: String
)

// STEP 3
data class PLPLoanStep3Request(
    val banco: String,
    val ciudadDepartamento: String,
    val ciudadEmpresa: String,
    val comoEnteraste: String,
    val departamento: String,
    val direccionExacta: String,
    val eps: String,
    var formulario: String,
    val nitEmpresa: String,
    val nombreEmpresa: String,
    val nombreReferenciaLaboral: String,
    val nombreReferenciaPersonal: String,
    val numeroTelefonoResidencia: String,
    val planCelular: String,
    val profesion: String,
    val referenciaBancaria: String,
    val requirioAyuda: String,
    val salarioReportado: String,
    val telefonoCelular: String,
    val telefonoEmpresa: String,
    val telefonoReferenciaLaboral: String,
    val telefonoReferenciaPersonal: String,
    val tipoAfiliado: String,
    val tipoContratoLaboral: String,
    val tipoCuenta: String
)

// STEP 4
data class PLPLoanStep4Request(
    val formulario: String
)

// STEP 5
data class PLPLoanStep5Request(
    val formulario: String,
    val plazoSeleccionado: Int
)

// OTP

// Verificar
data class OTPVerifyRequest(
    val formulario: String,
    val codigoOTP: String
)

// Procesar
data class OTPProcessRequest(
    val formulario: String
)

// PREGUNTAS

data class Respuesta(
    val idPregunta: Int,
    val idRespuesta: String
)

// Obtener y Enviar
data class QuestionsRequest(
    val formulario: String
)

// Validar
data class QuestionsValidationRequest(
    val formulario: String,
    val respuestas: List<Respuesta>
)