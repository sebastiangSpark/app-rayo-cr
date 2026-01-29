package com.rayo.rayoxml.co.services.Loan

import com.rayo.rayoxml.utils.EnvConfigCO
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
//import okhttp3.logging.HttpLoggingInterceptor

class LoanRepository {
    private val api: LoanService

    init {
        // Logger
        /*val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BODY)*/

        // Cambiar user agent
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val newRequest = chain.request().newBuilder()
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36")
                    .build()
                chain.proceed(newRequest)
            }
            //.addInterceptor(logging) // logs
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(EnvConfigCO.BASE_URL)
            .client(client) // Cambiar user agent
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        api = retrofit.create(LoanService::class.java)
    }

    suspend fun getData(data: LoanStepOneRequest): LoanStepOneResponse? {
        return try {
            val response = api.sendData(
                EnvConfigCO.API_URL,
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
                EnvConfigCO.API_URL,
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
                EnvConfigCO.API_URL,
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
                EnvConfigCO.API_URL,
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
        println("Req: $data")
        return try {
            val response = api.sendDataStepFourSubmit(
                EnvConfigCO.API_URL,
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
                    archivo = data.archivo,
                    archivoContentType = data.archivoContentType,
                    archivoName = data.archivoName
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
                EnvConfigCO.API_URL,
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
                EnvConfigCO.API_URL,
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
        println("Req: $data")
        return try {
            val response = api.sendDataStepFivePlusSubmit(
                EnvConfigCO.API_URL,
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
            //println("❗ Exception: ${e.printStackTrace()}")
            null
        }
    }

    // Step 5 load (primerizos)
    suspend fun getDataStepFiveLoad(data: LoanStepFiveLoadRequest): LoanStepFiveLoadResponse? {
        return try {
            val response = api.sendDataStepFiveLoad(
                EnvConfigCO.API_URL,
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
                EnvConfigCO.API_URL,
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
                EnvConfigCO.API_URL,
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
                EnvConfigCO.API_URL,
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
                EnvConfigCO.API_URL,
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
                EnvConfigCO.API_URL,
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
                EnvConfigCO.API_URL,
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
                EnvConfigCO.API_URL,
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
                EnvConfigCO.API_URL,
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
                EnvConfigCO.API_URL,
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
                EnvConfigCO.API_URL,
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
        println("Req: $data")
        return try {
            val response = api.sendDataPlpStep3(
                EnvConfigCO.API_URL,
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
                    //numeroTelefonoResidencia = data.numeroTelefonoResidencia, // Variable AMBIENTE TEST
                    numeroTelefonoRecidencia = data.numeroTelefonoRecidencia,   // Variable AMBIENTE PROD
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
            println("❗ Exception: ${e.printStackTrace()}")
            null
        }
    }

    // STEP 4
    suspend fun getDataPlpStep4(data: PLPLoanStep4Request): PlpLoanStep4Response? {
        return try {
            val response = api.sendDataPlpStep4(
                EnvConfigCO.API_URL,
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
        println("Req: $data")
        return try {
            val response = api.sendDataPlpStep5(
                EnvConfigCO.API_URL,
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
            println("❗ Exception: ${e.printStackTrace()}")
            null
        }
    }

    // OTP

    suspend fun getDataOTPVerify(data: OTPVerifyRequest): OTPVerifyResponse? {
        println("Req: $data")
        return try {
            val response = api.sendDataOTPVerify(
                EnvConfigCO.API_URL,
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
        println("Req: $data")
        return try {
            val response = api.sendDataOTPProcess(
                EnvConfigCO.API_URL,
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
        println("Req: $data")
        return try {
            val response = api.sendDataQuestions(
                EnvConfigCO.API_URL,
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
                EnvConfigCO.API_URL,
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
                EnvConfigCO.API_URL,
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