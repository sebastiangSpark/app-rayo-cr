package com.rayo.rayoxml.cr.services.Loan

import android.util.Log
import com.rayo.rayoxml.cr.services.Auth.AuthResponse
import com.rayo.rayoxml.cr.services.Auth.AuthService
import com.rayo.rayoxml.utils.EnvConfigCR
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class LoanRepository {
    private val api: LoanService
    private val apiAuth: AuthService

    private var authToken = ""

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl(EnvConfigCR.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        api = retrofit.create(LoanService::class.java)

        val retrofitAuth = Retrofit.Builder()
            .baseUrl(EnvConfigCR.BASE_URL_AUTH)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiAuth = retrofitAuth.create(AuthService::class.java)
    }

    suspend fun getToken(): AuthResponse? {
        return try {
            val params = mapOf(
                "username" to EnvConfigCR.USERNAME,
                "password" to EnvConfigCR.PASSWORD,
                "grant_type" to "password",
                "client_id" to EnvConfigCR.CLIENT_ID,
                "client_secret" to EnvConfigCR.CLIENT_SECRET,
            )
            val response = apiAuth.getToken(
                params = params
            )

            if (response.isSuccessful) {
                val authResult = response.body()
                if (authResult?.tokenType == "Bearer") {
                    println("✅ Auth Successful: ${authResult.accessToken}")
                    response.body()  // Auth successful
                } else {
                    println("❌ Auth Failed: ${authResult ?: "Unknown error"}")
                    null  // Auth failed
                }
            } else {
                println("🚨 API Error: ${response.errorBody()?.string()}")
                null
            }
        } catch (e: Exception) {
            println("❗ Exception: ${e.message}")
            null
        }
    }

    suspend fun createLoan(token: String, data: CreateLoanRequest): CreateLoanResponse? {
        println("Token: $token")
        println("Req: $data")
        return try {
            val response = api.createLoan(
                "Bearer $token",
                data
            )
            if (response.isSuccessful){
                println("✅ Successful: ${response.body()?.mensaje}")
                response.body() // retornar datos
            } else{
                println("Unsuccessful: ${response.body()}")
                null
            }
        } catch (e: Exception) {
            println("❗ Exception: ${e.message}")
            null
        }
    }

    suspend fun completeLoan(token: String, data: CompleteLoanRequest): CompleteLoanResponse? {
        println("Token: $token")
        println("Req: $data")
        return try {
            val response = api.completeLoan(
                "Bearer $token",
                data
            )
            if (response.isSuccessful){
                println("✅ Successful: ${response.body()?.estado}")
                response.body() // retornar datos
            } else{
                println("Unsuccessful: ${response.body()}")
                null
            }
        } catch (e: Exception) {
            println("❗ Exception: ${e.message}")
            null
        }
    }

    suspend fun createLoanMiniRenewal(data: CreateLoanMiniRenewalRequest): CreateLoanMiniRenewalResponse? {
        if(authToken == "") {
            val authResponse = getToken()
            if (authResponse != null && authResponse.accessToken.isNotEmpty()) {
                Log.d("LoanRepository CR", "Auth Data: ${authResponse}")
                authToken = authResponse.accessToken
            }
        }else{
            Log.d("LoanRepository CR", "Auth Data present: ${authToken}")
        }

        println("Req: $data")
        return try {
            val response = api.createLoanMiniRenewal(
                "Bearer $authToken",
                data
            )
            if (response.isSuccessful){
                println("✅ Successful: ${response.body()?.mensaje}")
                response.body() // retornar datos
            } else{
                println("Unsuccessful: ${response.body()}")
                null
            }
        } catch (e: Exception) {
            println("❗ Exception: ${e.message}")
            null
        }
    }

    suspend fun completeLoanMiniRenewal(data: CompleteLoanMiniRenewalRequest): CompleteLoanMiniRenewalResponse? {
        if(authToken == "") {
            val authResponse = getToken()
            if (authResponse != null && authResponse.accessToken.isNotEmpty()) {
                Log.d("LoanRepository CR", "Auth Data: ${authResponse}")
                authToken = authResponse.accessToken
            }
        }else{
            Log.d("LoanRepository CR", "Auth Data present: ${authToken}")
        }

        println("Req: $data")
        return try {
            val response = api.completeLoanMiniRenewal(
                "Bearer $authToken",
                data
            )
            if (response.isSuccessful){
                println("✅ Successful: ${response.body()?.estado}")
                response.body() // retornar datos
            } else{
                println("Unsuccessful: ${response.body()}")
                null
            }
        } catch (e: Exception) {
            println("❗ Exception: ${e.message}")
            null
        }
    }

    suspend fun createLoanRpRenewal(data: CreateLoanRpRenewalRequest): CreateLoanRpRenewalResponse? {
        if(authToken == "") {
            val authResponse = getToken()
            if (authResponse != null && authResponse.accessToken.isNotEmpty()) {
                Log.d("LoanRepository CR", "Auth Data: ${authResponse}")
                authToken = authResponse.accessToken
            }
        }else{
            Log.d("LoanRepository CR", "Auth Data present: ${authToken}")
        }

        println("Req: $data")
        return try {
            val response = api.createLoanRpRenewal(
                "Bearer $authToken",
                data
            )
            if (response.isSuccessful){
                println("✅ Successful: ${response.body()?.estado}")
                response.body() // retornar datos
            } else{
                println("Unsuccessful: ${response.body()}")
                null
            }
        } catch (e: Exception) {
            println("❗ Exception: ${e.message}")
            null
        }
    }

    suspend fun completeLoanRpRenewal(data: CompleteLoanRpRenewalRequest): CompleteLoanRpRenewalResponse? {
        if(authToken == "") {
            val authResponse = getToken()
            if (authResponse != null && authResponse.accessToken.isNotEmpty()) {
                Log.d("LoanRepository CR", "Auth Data: ${authResponse}")
                authToken = authResponse.accessToken
            }
        }else{
            Log.d("LoanRepository CR", "Auth Data present: ${authToken}")
        }

        println("Req: $data")
        return try {
            val response = api.completeLoanRpRenewal(
                "Bearer $authToken",
                data
            )
            if (response.isSuccessful){
                println("✅ Successful: ${response.body()?.estado}")
                response.body() // retornar datos
            } else{
                println("Unsuccessful: ${response.body()}")
                null
            }
        } catch (e: Exception) {
            println("❗ Exception: ${e.message}")
            null
        }
    }







    suspend fun getData(data: LoanStepOneRequest): LoanStepOneResponse? {
        return try {
            val response = api.sendData(
                "solicitud-mc/",  // Valor de header x-token
                LoanStepOneRequest(
                    contacto = data.contacto,
                    nombre = data.nombre,
                    primerApellido = data.primerApellido,
                    numeroDocumento = data.numeroDocumento,
                    fechaNacimiento = data.fechaNacimiento,
                    celular = data.celular,
                    password = data.password,
                    empleadoFormal = data.empleadoFormal,
                    nombreCuentaBancaria = data.nombreCuentaBancaria,
                    correoElectronico = data.correoElectronico,
                    correoElectronico2 = data.correoElectronico2,
                    //referido_cliente_cedula = data.referido_cliente_cedula,
                    //referido_cliente = data.referido_cliente,
                    //referido_cliente_id = data.referido_cliente_id,
                    tipoDocumento = data.tipoDocumento,
                    plazoSeleccionado = data.plazoSeleccionado,
                    valorSeleccionado = data.valorSeleccionado,
                    //ingresoMensual = data.ingresoMensual,
                    fechaExpedicion = data.fechaExpedicion,
                    sessionId = data.sessionId,
                    genero = data.genero
                )
            )
            if (response.isSuccessful){
                println("✅ Successful: ${response.body()?.solicitud?.result}")
                println("Loan step one Data: ${response.body()?.solicitud}")
                response.body() // retornar datos
            } else null
        } catch (e: Exception) {
            println("❗ Exception: ${e.message}")
            null
        }
    }

    suspend fun getDataStepTwo(data: LoanStepTwoRequest): LoanStepTwoResponse? {
        return try {
            val response = api.sendDataStepTwo(
                "solicitud-mc/",  // Valor de header x-token
                LoanStepTwoRequest(
                    formulario = data.formulario,
                    departamento = data.departamento,
                    ciudadDepartamento = data.ciudadDepartamento,
                    direccionExacta = data.direccionExacta,
                    banco = data.banco,
                    tipoCuenta = data.tipoCuenta,
                    referenciaBancaria = data.referenciaBancaria,
                    referenciaBancaria2 = data.referenciaBancaria2,
                    plazoSeleccionado = data.plazoSeleccionado,
                    telefonoEmpresa = data.telefonoEmpresa,
                    contadorActualizado = data.contadorActualizado,
                    showModal = data.showModal,
                    checkTecnologia = data.checkTecnologia
                )
            )
            if (response.isSuccessful){
                println("✅ Successful: ${response.body()?.solicitud?.result}")
                println("Loan step two Data: ${response.body()?.solicitud}")
                response.body() // retornar datos
            } else null
        } catch (e: Exception) {
            println("❗ Exception: ${e.message}")
            null
        }
    }

    suspend fun getDataValidationStep(data: LoanValidationRequest): LoanValidationResponse? {
        return try {
            val response = api.sendDataValidationStep(
                "validaciones-mc/",  // Valor de header x-token
                LoanValidationRequest(
                    formulario = data.formulario
                )
            )
            if (response.isSuccessful){
                println("✅ Successful: ${response.body()?.solicitud?.result}")
                println("Loan validation step Data: ${response.body()?.solicitud}")
                response.body() // retornar datos
            } else null
        } catch (e: Exception) {
            println("❗ Exception: ${e.message}")
            null
        }
    }

    // Step 4 load
    suspend fun getDataStepFourLoad(data: LoanStepFourLoadRequest): LoanStepFourLoadResponse? {
        return try {
            val response = api.sendDataStepFourLoad(
                "consulta-frm/",  // Valor de header x-token
                LoanStepFourLoadRequest(
                    formulario = data.formulario
                )
            )
            if (response.isSuccessful){
                println("✅ Successful: ${response.body()?.formulario?.result}")
                println("Loan step four load Data: ${response.body()?.formulario}")
                response.body() // retornar datos
            } else null
        } catch (e: Exception) {
            println("❗ Exception: ${e.message}")
            null
        }
    }

    // Step 4 submit
    suspend fun getDataStepFourSubmit(data: LoanStepFourSubmitRequest): LoanStepFourSubmitResponse? {
        return try {
            val response = api.sendDataStepFourSubmit(
                "solicitud-mc/",  // Valor de header x-token
                LoanStepFourSubmitRequest(
                    formulario = data.formulario,
                    departamento = data.departamento,
                    ciudadDepartamento = data.ciudadDepartamento,
                    direccionExacta = data.direccionExacta,
                    banco = data.banco,
                    tipoCuenta = data.tipoCuenta,
                    referenciaBancaria = data.referenciaBancaria,
                    referenciaBancaria2 = data.referenciaBancaria2,
                    plazoSeleccionado = data.plazoSeleccionado,
                    telefonoEmpresa = data.telefonoEmpresa,
                    contadorActualizado = data.contadorActualizado,
                    showModal = data.showModal,
                    checkTecnologia = data.checkTecnologia,
                    debito = data.debito,
                    archivo = data.archivo
                )
            )
            if (response.isSuccessful){
                println("✅ Successful: ${response.body()?.solicitud?.result}")
                println("Loan step four submit Data: ${response.body()?.solicitud}")
                response.body() // retornar datos
            } else null
        } catch (e: Exception) {
            println("❗ Exception: ${e.message}")
            null
        }
    }

    // Step 5 plus load
    suspend fun getDataStepFivePlusLoad(data: LoanStepFivePlusLoadRequest): LoanStepFivePlusLoadResponse? {
        return try {
            val response = api.sendDataStepFivePlusLoad(
                "formulario-rp/",  // Valor de header x-token
                LoanStepFivePlusLoadRequest(
                    formulario = data.formulario
                )
            )
            if (response.isSuccessful){
                println("✅ Successful: ${response.body()?.solicitud?.codigo}")
                println("Loan step five plus load Data: ${response.body()?.solicitud}")
                response.body() // retornar datos
            } else null
        } catch (e: Exception) {
            println("❗ Exception: ${e.message}")
            null
        }
    }

    // Step 5 plus load 2
    suspend fun getDataStepFivePlusLoadTwo(data: LoanStepFivePlusLoadRequest): LoanStepFivePlusLoadTwoResponse? {
        return try {
            val response = api.sendDataStepFivePlusLoadTwo(
                "informacion-solicitud-rp/",  // Valor de header x-token
                LoanStepFivePlusLoadRequest(
                    formulario = data.formulario
                )
            )
            if (response.isSuccessful){
                println("✅ Successful: ${response.body()?.solicitud?.codigo}")
                println("Loan step five plus load two Data: ${response.body()?.solicitud}")
                response.body() // retornar datos
            } else null
        } catch (e: Exception) {
            println("❗ Exception: ${e.message}")
            null
        }
    }

    // Step 5 plus submit
    suspend fun getDataStepFivePlusSubmit(data: LoanStepFivePlusSubmitRequest): LoanStepFivePlusSubmitResponse? {
        return try {
            val response = api.sendDataStepFivePlusSubmit(
                "prestamos-rp/",  // Valor de header x-token
                LoanStepFivePlusSubmitRequest(
                    checkTecnologia = data.checkTecnologia,
                    formulario = data.formulario,
                    plazoSeleccionado = data.plazoSeleccionado
                )
            )
            if (response.isSuccessful){
                println("✅ Successful: ${response.body()?.solicitud?.result}")
                println("Loan step five plus submit Data: ${response.body()?.solicitud}")
                response.body() // retornar datos
            } else null
        } catch (e: Exception) {
            println("❗ Exception: ${e.message}")
            null
        }
    }

    // Step 5 load (primerizos)
    suspend fun getDataStepFiveLoad(data: LoanStepFiveLoadRequest): LoanStepFiveLoadResponse? {
        return try {
            val response = api.sendDataStepFiveLoad(
                "formulario-mini/",  // Valor de header x-token
                LoanStepFiveLoadRequest(
                    formulario = data.formulario
                )
            )
            if (response.isSuccessful){
                println("✅ Successful: ${response.body()?.solicitud?.codigo}")
                println("Loan step five load Data: ${response.body()?.solicitud}")
                response.body() // retornar datos
            } else null
        } catch (e: Exception) {
            println("❗ Exception: ${e.message}")
            null
        }
    }

    // Step 5 submit (primerizos)
    suspend fun getDataStepFiveSubmit(data: LoanStepFiveSubmitRequest): LoanStepFiveSubmitResponse? {
        return try {
            val response = api.sendDataStepFiveSubmit(
                "prestamos/",  // Valor de header x-token
                LoanStepFiveSubmitRequest(
                    formulario = data.formulario,
                    plazoSeleccionado = data.plazoSeleccionado
                )
            )
            if (response.isSuccessful){
                println("✅ Successful: ${response.body()?.solicitud?.result}")
                println("Loan step five submit Data: ${response.body()?.solicitud}")
                response.body() // retornar datos
            } else null
        } catch (e: Exception) {
            println("❗ Exception: ${e.message}")
            null
        }
    }

    // Desembolso Tumipay (primerizos)
    suspend fun getDataTumiPayDisbursement(data: LoanTumiPayRequest): LoanTumiPayResponse? {
        return try {
            val response = api.sendDataTumiPayDisbursement(
                "topup/",  // Valor de header x-token
                LoanTumiPayRequest(
                    idPrestamo = data.idPrestamo
                )
            )
            if (response.isSuccessful){
                println("✅ Successful: ${response.body()?.prestamo?.result}")
                println("Loan TumiPay disbursement Data: ${response.body()?.prestamo}")
                response.body() // retornar datos
            } else null
        } catch (e: Exception) {
            println("❗ Exception: ${e.message}")
            null
        }
    }

    // Desembolso Tumipay plus
    suspend fun getDataTumiPayPlusDisbursement(data: LoanTumiPayRequest): LoanTumiPayResponse? {
        return try {
            val response = api.sendDataTumiPayPlusDisbursement(
                "topup/",  // Valor de header x-token
                LoanTumiPayRequest(
                    idPrestamo = data.idPrestamo
                )
            )
            if (response.isSuccessful){
                println("✅ Successful: ${response.body()?.prestamo?.result}")
                println("Loan TumiPay disbursement Data: ${response.body()?.prestamo}")
                response.body() // retornar datos
            } else null
        } catch (e: Exception) {
            println("❗ Exception: ${e.message}")
            null
        }
    }

    // RENOVACION

    // Proposal Step mini Load
    suspend fun getDataRenewalProposalLoad(data: LoanRenewalProposalLoadRequest): LoanRenewalProposalLoadResponse? {
        return try {
            val response = api.sendDataRenewalProposalLoad(
                "propuesta-mc/",  // Valor de header x-token
                LoanRenewalProposalLoadRequest(
                    idContacto = data.idContacto
                )
            )
            if (response.isSuccessful){
                println("✅ Successful: ${response.body()?.solicitud?.result}")
                println("Loan nenewal proposal load Data: ${response.body()?.solicitud}")
                response.body() // retornar datos
            } else null
        } catch (e: Exception) {
            println("❗ Exception: ${e.message}")
            null
        }
    }

    // Proposal Step mini Submit
    suspend fun getDataRenewalProposalSubmit(data: LoanRenewalProposalSubmitRequest): LoanRenewalProposalSubmitResponse? {
        return try {
            val response = api.sendDataRenewalProposalSubmit(
                "solicititud-renovacion-mc/",  // Valor de header x-token
                LoanRenewalProposalSubmitRequest(
                    checkTecnologia = data.checkTecnologia,
                    plazoSeleccionado = data.plazoSeleccionado,
                    showModal = data.showModal,
                    debito = data.debito,
                    archivo = data.archivo,
                    archivoContentType = data.archivoContentType,
                    archivoName = data.archivoName,
                    descuento10 = data.descuento10,
                    descuentoAdministracion = data.descuentoAdministracion,
                    idContacto = data.idContacto,
                    montoDescuento = data.montoDescuento,
                    porcentajeDescuento = data.porcentajeDescuento,
                    propuestaSugerida = data.propuestaSugerida
                )
            )
            if (response.isSuccessful){
                println("✅ Successful: ${response.body()?.solicitud?.result}")
                println("Loan nenewal proposal submit Data: ${response.body()?.solicitud}")
                response.body() // retornar datos
            } else null
        } catch (e: Exception) {
            println("❗ Exception: ${e.message}")
            null
        }
    }

    // Information step mini Load
    suspend fun getDataRenewalInfoLoad(data: LoanRenewalInfoLoadRequest): LoanRenewalInfoLoadResponse? {
        return try {
            val response = api.sendDataRenewalInfoLoad(
                "informacion-solicitud/",  // Valor de header x-token
                LoanRenewalInfoLoadRequest(
                    formulario = data.formulario
                )
            )
            if (response.isSuccessful){
                println("✅ Successful: ${response.body()?.solicitud?.result}")
                println("Loan nenewal info load Data: ${response.body()?.solicitud}")
                response.body() // retornar datos
            } else null
        } catch (e: Exception) {
            println("❗ Exception: ${e.message}")
            null
        }
    }

    // Step4 Plus Load
    suspend fun getDataRenewalStepFourPlusLoad(data: LoanRenewalProposalLoadRequest): LoanRenewalProposalLoadResponse? {
        return try {
            val response = api.sendDataRenewalStepFourPlusLoad(
                "propuesta-rp/",  // Valor de header x-token
                LoanRenewalProposalLoadRequest(
                    idContacto = data.idContacto
                )
            )
            if (response.isSuccessful){
                println("✅ Successful: ${response.body()?.solicitud?.result}")
                println("Loan nenewal step four plus load Data: ${response.body()?.solicitud}")
                response.body() // retornar datos
            } else null
        } catch (e: Exception) {
            println("❗ Exception: ${e.message}")
            null
        }
    }

    // Step4 plus Submit
    suspend fun getDataRenewalStepFourPlusSubmit(data: LoanRenewalStepFourPlusSubmitRequest): LoanRenewalProposalSubmitResponse? {
        //println("Req: $data")
        return try {
            val response = api.sendDataRenewalStepFourPlusSubmit(
                "solicititud-renovacion-rp/",  // Valor de header x-token
                LoanRenewalStepFourPlusSubmitRequest(
                    formulario = data.formulario,
                    checkTecnologia = data.checkTecnologia,
                    showModal = data.showModal,
                    debito = data.debito,
                    idContacto = data.idContacto,
                    montoDescuento = data.montoDescuento,
                    propuestaSugerida = data.propuestaSugerida,
                    fianzaRP = data.fianzaRP,
                    //contadorActualizado = data.contadorActualizado,
                    descuento10 = data.descuento10,
                    descuentoAdministracion = data.descuentoAdministracion,
                    plazoSeleccionado = data.plazoSeleccionado
                )
            )
            if (response.isSuccessful){
                println("✅ Successful: ${response.body()?.solicitud?.result}")
                println("Loan nenewal step four plus submit Data: ${response.body()?.solicitud}")
                response.body() // retornar datos
            } else null
        } catch (e: Exception) {
            println("❗ Exception: ${e.message}")
            null
        }
    }

    // PLP

    // STEP 1
    suspend fun getDataPlpStep1(data: PLPLoanStep1Request): PlpLoanStep1Response? {
        return try {
            val response = api.sendDataPlpStep1(
                "informacion-contacto/",  // Valor de header x-token
                PLPLoanStep1Request(
                    contacto = data.contacto
                )
            )
            if (response.isSuccessful){
                println("✅ Successful: ${response.body()?.solicitud?.result}")
                println("Loan PLP step 1 Data: ${response.body()?.solicitud}")
                response.body() // retornar datos
            } else null
        } catch (e: Exception) {
            println("❗ Exception: ${e.message}")
            null
        }
    }

    // STEP 2
    suspend fun getDataPlpStep2(data: PLPLoanStep2Request): PlpLoanStep2Response? {
        return try {
            val response = api.sendDataPlpStep2(
                "solicitud-plp/",  // Valor de header x-token
                PLPLoanStep2Request(
                    contacto = data.contacto,
                    antiguedadLaboral = data.antiguedadLaboral,
                    cantidadDependientes = data.cantidadDependientes,
                    cantidadHijos = data.cantidadHijos,
                    celular = data.celular,
                    correoElectronico = data.correoElectronico,
                    fechaExpedicion = data.fechaExpedicion,
                    ingresoMensual = data.ingresoMensual,
                    nombre = data.nombre,
                    numeroDocumento = data.numeroDocumento,
                    password = data.password,
                    primerApellido = data.primerApellido,
                    tipoDocumento = data.tipoDocumento
                )
            )
            if (response.isSuccessful){
                println("✅ Successful: ${response.body()?.solicitud?.result}")
                println("Loan PLP step 2 Data: ${response.body()?.solicitud}")
                response.body() // retornar datos
            } else null
        } catch (e: Exception) {
            println("❗ Exception: ${e.message}")
            null
        }
    }

    // STEP 3
    suspend fun getDataPlpStep3(data: PLPLoanStep3Request): PlpLoanStep3Response? {
        return try {
            val response = api.sendDataPlpStep3(
                "solicitud-plp/",  // Valor de header x-token
                PLPLoanStep3Request(
                    banco = data.banco,
                    ciudadDepartamento = data.ciudadDepartamento,
                    ciudadEmpresa = data.ciudadEmpresa,
                    comoEnteraste = data.comoEnteraste,
                    departamento = data.departamento,
                    direccionExacta = data.direccionExacta,
                    eps = data.eps,
                    formulario = data.formulario,
                    nitEmpresa = data.nitEmpresa,
                    nombreEmpresa = data.nombreEmpresa,
                    nombreReferenciaLaboral = data.nombreReferenciaLaboral,
                    nombreReferenciaPersonal = data.nombreReferenciaPersonal,
                    numeroTelefonoResidencia = data.numeroTelefonoResidencia,
                    planCelular = data.planCelular,
                    profesion = data.profesion,
                    referenciaBancaria = data.referenciaBancaria,
                    requirioAyuda = data.requirioAyuda,
                    salarioReportado = data.salarioReportado,
                    telefonoCelular = data.telefonoCelular,
                    telefonoEmpresa = data.telefonoEmpresa,
                    telefonoReferenciaLaboral = data.telefonoReferenciaLaboral,
                    telefonoReferenciaPersonal = data.telefonoReferenciaPersonal,
                    tipoAfiliado = data.tipoAfiliado,
                    tipoContratoLaboral = data.tipoContratoLaboral,
                    tipoCuenta = data.tipoCuenta
                )
            )
            if (response.isSuccessful){
                println("✅ Successful: ${response.body()?.solicitud?.result}")
                println("Loan PLP step 3 Data: ${response.body()?.solicitud}")
                response.body() // retornar datos
            } else null
        } catch (e: Exception) {
            println("❗ Exception: ${e.message}")
            null
        }
    }

    // STEP 4
    suspend fun getDataPlpStep4(data: PLPLoanStep4Request): PlpLoanStep4Response? {
        return try {
            val response = api.sendDataPlpStep4(
                "info-solicitud-plp/",  // Valor de header x-token
                PLPLoanStep4Request(
                    formulario = data.formulario
                )
            )
            if (response.isSuccessful){
                println("✅ Successful: ${response.body()?.solicitud?.result}")
                println("Loan PLP step 4 Data: ${response.body()?.solicitud}")
                response.body() // retornar datos
            } else null
        } catch (e: Exception) {
            println("❗ Exception: ${e.message}")
            null
        }
    }

    // STEP 5
    suspend fun getDataPlpStep5(data: PLPLoanStep5Request): PlpLoanStep5Response? {
        //println("Req: $data")
        return try {
            val response = api.sendDataPlpStep5(
                "prestamos-plp/",  // Valor de header x-token
                PLPLoanStep5Request(
                    formulario = data.formulario,
                    plazoSeleccionado = data.plazoSeleccionado
                )
            )
            if (response.isSuccessful){
                println("✅ Successful: ${response.body()?.solicitud?.result}")
                println("Loan PLP step 5 Data: ${response.body()?.solicitud}")
                response.body() // retornar datos
            } else null
        } catch (e: Exception) {
            println("❗ Exception: ${e.message}")
            null
        }
    }

    // OTP

    suspend fun getDataOTPVerify(data: OTPVerifyRequest): OTPVerifyResponse? {
        return try {
            val response = api.sendDataOTPVerify(
                "verificarOTP/",  // Valor de header x-token
                OTPVerifyRequest(
                    formulario = data.formulario,
                    codigoOTP = data.codigoOTP
                )
            )
            if (response.isSuccessful){
                println("✅ Successful: ${response.body()?.solicitud?.result}")
                println("OTP verification Data: ${response.body()?.solicitud}")
                response.body() // retornar datos
            } else null
        } catch (e: Exception) {
            println("❗ Exception: ${e.message}")
            null
        }
    }

    suspend fun getDataOTPProcess(data: OTPProcessRequest): OTPVerifyResponse? {
        return try {
            val response = api.sendDataOTPProcess(
                "procesarOTP/",  // Valor de header x-token
                OTPProcessRequest(
                    formulario = data.formulario
                )
            )
            if (response.isSuccessful){
                println("✅ Successful: ${response.body()?.solicitud?.result}")
                println("OTP processing Data: ${response.body()?.solicitud}")
                response.body() // retornar datos
            } else null
        } catch (e: Exception) {
            println("❗ Exception: ${e.message}")
            null
        }
    }

    // PREGUNTAS

    suspend fun getDataQuestions(data: QuestionsRequest): QuestionResponse? {
        return try {
            val response = api.sendDataQuestions(
                "preguntas/",  // Valor de header x-token
                QuestionsRequest(
                    formulario = data.formulario
                )
            )
            if (response.isSuccessful){
                println("✅ Successful: ${response.body()?.codigo}")
                println("Security questions Data: ${response.body()}")
                response.body() // retornar datos
            } else null
        } catch (e: Exception) {
            println("❗ Exception: ${e.message}")
            null
        }
    }

    suspend fun getDataQuestionsValidation(data: QuestionsValidationRequest): QuestionValidationResponse? {
        return try {
            val response = api.sendDataQuestionsValidation(
                "respuestas/",  // Valor de header x-token
                QuestionsValidationRequest(
                    formulario = data.formulario,
                    respuestas = data.respuestas
                )
            )
            if (response.isSuccessful){
                println("✅ Successful: ${response.body()?.solicitud?.codigo}")
                println("Security answers Data: ${response.body()}")
                response.body() // retornar datos
            } else null
        } catch (e: Exception) {
            println("❗ Exception: ${e.message}")
            null
        }
    }

    suspend fun getDataQuestionsVerification(data: QuestionsRequest): QuestionVerificationResponse? {
        return try {
            val response = api.sendDataQuestionsVerification(
                "validarResultadoCuestionario/",  // Valor de header x-token
                QuestionsRequest(
                    formulario = data.formulario
                )
            )
            if (response.isSuccessful){
                println("✅ Successful: ${response.body()?.solicitud?.codigo}")
                println("Security answers Data: ${response.body()}")
                response.body() // retornar datos
            } else null
        } catch (e: Exception) {
            println("❗ Exception: ${e.message}")
            null
        }
    }
}