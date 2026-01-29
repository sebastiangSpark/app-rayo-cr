package com.rayo.rayoxml.cr.services.Loan

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.PUT

interface LoanService {
    @Headers("Content-Type: application/json", "Accept: application/json")
    @POST("NewSolicitud")
    suspend fun createLoan(
        @Header("Authorization") token: String,
        @Body request: CreateLoanRequest,
    ): Response<CreateLoanResponse>

    @Headers("Content-Type: application/json", "Accept: application/json")
    @POST("finalizarProcesoWS/")
    suspend fun completeLoan(
        @Header("Authorization") token: String,
        @Body request: CompleteLoanRequest,
    ): Response<CompleteLoanResponse>

    @Headers("Content-Type: application/json", "Accept: application/json")
    @POST("renovar/")
    suspend fun createLoanMiniRenewal(
        @Header("Authorization") token: String,
        @Body request: CreateLoanMiniRenewalRequest,
    ): Response<CreateLoanMiniRenewalResponse>

    @Headers("Content-Type: application/json", "Accept: application/json")
    @POST("finalizarProcesoWS/")
    suspend fun completeLoanMiniRenewal(
        @Header("Authorization") token: String,
        @Body request: CompleteLoanMiniRenewalRequest,
    ): Response<CompleteLoanMiniRenewalResponse>

    @Headers("Content-Type: application/json", "Accept: application/json")
    @POST("renovarRP/")
    suspend fun createLoanRpRenewal(
        @Header("Authorization") token: String,
        @Body request: CreateLoanRpRenewalRequest,
    ): Response<CreateLoanRpRenewalResponse>

    @Headers("Content-Type: application/json", "Accept: application/json")
    @POST("finalizarProcesoRayoPlusWS/")
    suspend fun completeLoanRpRenewal(
        @Header("Authorization") token: String,
        @Body request: CompleteLoanRpRenewalRequest,
    ): Response<CompleteLoanRpRenewalResponse>






    @Headers("Content-Type: application/json", "Accept: application/json")
    @POST("SalesForce/accessToken.php") // Agregar environments
    suspend fun sendData(
        @Header("X-Method") method: String,  // Custom header
        @Body request: LoanStepOneRequest,
    ): Response<LoanStepOneResponse>

    @Headers("Content-Type: application/json", "Accept: application/json")
    @PUT("SalesForce/accessToken.php") // Agregar environments
    suspend fun sendDataStepTwo(
        @Header("X-Method") method: String,  // Custom header
        @Body request: LoanStepTwoRequest,
    ): Response<LoanStepTwoResponse>

    @Headers("Content-Type: application/json", "Accept: application/json")
    @POST("SalesForce/accessToken.php") // Agregar environments
    suspend fun sendDataValidationStep(
        @Header("X-Method") method: String,  // Custom header
        @Body request: LoanValidationRequest,
    ): Response<LoanValidationResponse>

    // Step 4 load
    @Headers("Content-Type: application/json", "Accept: application/json")
    @POST("SalesForce/accessToken.php") // Agregar environments
    suspend fun sendDataStepFourLoad(
        @Header("X-Method") method: String,  // Custom header
        @Body request: LoanStepFourLoadRequest,
    ): Response<LoanStepFourLoadResponse>

    // Step 4 submit
    @Headers("Content-Type: application/json", "Accept: application/json")
    @PUT("SalesForce/accessToken.php") // Agregar environments
    suspend fun sendDataStepFourSubmit(
        @Header("X-Method") method: String,  // Custom header
        @Body request: LoanStepFourSubmitRequest,
    ): Response<LoanStepFourSubmitResponse>

    // Step 5 plus load
    @Headers("Content-Type: application/json", "Accept: application/json")
    @POST("SalesForce/accessToken.php") // Agregar environments
    suspend fun sendDataStepFivePlusLoad(
        @Header("X-Method") method: String,  // Custom header
        @Body request: LoanStepFivePlusLoadRequest,
    ): Response<LoanStepFivePlusLoadResponse>

    // Step 5 plus load 2
    @Headers("Content-Type: application/json", "Accept: application/json")
    @POST("SalesForce/accessToken.php") // Agregar environments
    suspend fun sendDataStepFivePlusLoadTwo(
        @Header("X-Method") method: String,  // Custom header
        @Body request: LoanStepFivePlusLoadRequest,
    ): Response<LoanStepFivePlusLoadTwoResponse>

    // Step 5 plus submit
    @Headers("Content-Type: application/json", "Accept: application/json")
    @PUT("SalesForce/accessToken.php") // Agregar environments
    suspend fun sendDataStepFivePlusSubmit(
        @Header("X-Method") method: String,  // Custom header
        @Body request: LoanStepFivePlusSubmitRequest,
    ): Response<LoanStepFivePlusSubmitResponse>

    // Step 5 load (primerizos)
    @Headers("Content-Type: application/json", "Accept: application/json")
    @POST("SalesForce/accessToken.php") // Agregar environments
    suspend fun sendDataStepFiveLoad(
        @Header("X-Method") method: String,  // Custom header
        @Body request: LoanStepFiveLoadRequest,
    ): Response<LoanStepFiveLoadResponse>

    // Step 5 submit (primerizos)
    @Headers("Content-Type: application/json", "Accept: application/json")
    @POST("SalesForce/accessToken.php") // Agregar environments
    suspend fun sendDataStepFiveSubmit(
        @Header("X-Method") method: String,  // Custom header
        @Body request: LoanStepFiveSubmitRequest,
    ): Response<LoanStepFiveSubmitResponse>

    // Desembolso Tumipay (primerizos)
    @Headers("Content-Type: application/json", "Accept: application/json")
    @POST("SalesForce/accessToken.php") // Agregar environments
    suspend fun sendDataTumiPayDisbursement(
        @Header("X-Method") method: String,  // Custom header
        @Body request: LoanTumiPayRequest,
    ): Response<LoanTumiPayResponse>

    // Desembolso Tumipay plus
    @Headers("Content-Type: application/json", "Accept: application/json")
    @PUT("SalesForce/accessToken.php") // Agregar environments
    suspend fun sendDataTumiPayPlusDisbursement(
        @Header("X-Method") method: String,  // Custom header
        @Body request: LoanTumiPayRequest,
    ): Response<LoanTumiPayResponse>

    // RENOVACION

    // Renovacion STEP4 mini Load
    @Headers("Content-Type: application/json", "Accept: application/json")
    @POST("SalesForce/accessToken.php") // Agregar environments
    suspend fun sendDataRenewalProposalLoad(
        @Header("X-Method") method: String,  // Custom header
        @Body request: LoanRenewalProposalLoadRequest,
    ): Response<LoanRenewalProposalLoadResponse>

    // Renovacion STEP4 mini Submit
    @Headers("Content-Type: application/json", "Accept: application/json")
    @POST("SalesForce/accessToken.php") // Agregar environments
    suspend fun sendDataRenewalProposalSubmit(
        @Header("X-Method") method: String,  // Custom header
        @Body request: LoanRenewalProposalSubmitRequest,
    ): Response<LoanRenewalProposalSubmitResponse>

    // Renovacion Info Step mini Load
    @Headers("Content-Type: application/json", "Accept: application/json")
    @POST("SalesForce/accessToken.php") // Agregar environments
    suspend fun sendDataRenewalInfoLoad(
        @Header("X-Method") method: String,  // Custom header
        @Body request: LoanRenewalInfoLoadRequest,
    ): Response<LoanRenewalInfoLoadResponse>


    // Renovacion STEP4 Plus Load
    @Headers("Content-Type: application/json", "Accept: application/json")
    @POST("SalesForce/accessToken.php") // Agregar environments
    suspend fun sendDataRenewalStepFourPlusLoad(
        @Header("X-Method") method: String,  // Custom header
        @Body request: LoanRenewalProposalLoadRequest,
    ): Response<LoanRenewalProposalLoadResponse>

    // Renovacion STEP4 mini Submit
    @Headers("Content-Type: application/json", "Accept: application/json")
    @POST("SalesForce/accessToken.php") // Agregar environments
    suspend fun sendDataRenewalStepFourPlusSubmit(
        @Header("X-Method") method: String,  // Custom header
        @Body request: LoanRenewalStepFourPlusSubmitRequest,
    ): Response<LoanRenewalProposalSubmitResponse>

    // PLP

    @Headers("Content-Type: application/json", "Accept: application/json")
    @POST("SalesForce/accessToken.php") // Agregar environments
    suspend fun sendDataPlpStep1(
        @Header("X-Method") method: String,  // Custom header
        @Body request: PLPLoanStep1Request,
    ): Response<PlpLoanStep1Response>

    @Headers("Content-Type: application/json", "Accept: application/json")
    @POST("SalesForce/accessToken.php") // Agregar environments
    suspend fun sendDataPlpStep2(
        @Header("X-Method") method: String,  // Custom header
        @Body request: PLPLoanStep2Request,
    ): Response<PlpLoanStep2Response>

    @Headers("Content-Type: application/json", "Accept: application/json")
    @PUT("SalesForce/accessToken.php") // Agregar environments
    suspend fun sendDataPlpStep3(
        @Header("X-Method") method: String,  // Custom header
        @Body request: PLPLoanStep3Request,
    ): Response<PlpLoanStep3Response>

    @Headers("Content-Type: application/json", "Accept: application/json")
    @POST("SalesForce/accessToken.php") // Agregar environments
    suspend fun sendDataPlpStep4(
        @Header("X-Method") method: String,  // Custom header
        @Body request: PLPLoanStep4Request,
    ): Response<PlpLoanStep4Response>

    @Headers("Content-Type: application/json", "Accept: application/json")
    @POST("SalesForce/accessToken.php") // Agregar environments
    suspend fun sendDataPlpStep5(
        @Header("X-Method") method: String,  // Custom header
        @Body request: PLPLoanStep5Request,
    ): Response<PlpLoanStep5Response>

    // OTP

    @Headers("Content-Type: application/json", "Accept: application/json")
    @POST("SalesForce/accessToken.php") // Agregar environments
    suspend fun sendDataOTPVerify(
        @Header("X-Method") method: String,  // Custom header
        @Body request: OTPVerifyRequest,
    ): Response<OTPVerifyResponse>

    @Headers("Content-Type: application/json", "Accept: application/json")
    @POST("SalesForce/accessToken.php") // Agregar environments
    suspend fun sendDataOTPProcess(
        @Header("X-Method") method: String,  // Custom header
        @Body request: OTPProcessRequest,
    ): Response<OTPVerifyResponse>

    // QUESTIONS

    @Headers("Content-Type: application/json", "Accept: application/json")
    @POST("SalesForce/accessToken.php") // Agregar environments
    suspend fun sendDataQuestions(
        @Header("X-Method") method: String,  // Custom header
        @Body request: QuestionsRequest,
    ): Response<QuestionResponse>

    @Headers("Content-Type: application/json", "Accept: application/json")
    @POST("SalesForce/accessToken.php") // Agregar environments
    suspend fun sendDataQuestionsValidation(
        @Header("X-Method") method: String,  // Custom header
        @Body request: QuestionsValidationRequest,
    ): Response<QuestionValidationResponse>

    @Headers("Content-Type: application/json", "Accept: application/json")
    @POST("SalesForce/accessToken.php") // Agregar environments
    suspend fun sendDataQuestionsVerification(
        @Header("X-Method") method: String,  // Custom header
        @Body request: QuestionsRequest,
    ): Response<QuestionVerificationResponse>

}