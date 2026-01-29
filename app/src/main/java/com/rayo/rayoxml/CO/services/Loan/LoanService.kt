package com.rayo.rayoxml.co.services.Loan

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Url

interface LoanService {
    @Headers("Content-Type: application/json", "Accept: application/json")
    @POST
    suspend fun sendData(
        @Url endpoint: String,
        @Header("X-Method") method: String,  // Custom header
        @Body request: LoanStepOneRequest,
    ): Response<LoanStepOneResponse>

    @Headers("Content-Type: application/json", "Accept: application/json")
    @PUT
    suspend fun sendDataStepTwo(
        @Url endpoint: String,
        @Header("X-Method") method: String,  // Custom header
        @Body request: LoanStepTwoRequest,
    ): Response<LoanStepTwoResponse>

    @Headers("Content-Type: application/json", "Accept: application/json")
    @POST
    suspend fun sendDataValidationStep(
        @Url endpoint: String,
        @Header("X-Method") method: String,  // Custom header
        @Body request: LoanValidationRequest,
    ): Response<LoanValidationResponse>

    // Step 4 load
    @Headers("Content-Type: application/json", "Accept: application/json")
    @POST
    suspend fun sendDataStepFourLoad(
        @Url endpoint: String,
        @Header("X-Method") method: String,  // Custom header
        @Body request: LoanStepFourLoadRequest,
    ): Response<LoanStepFourLoadResponse>

    // Step 4 submit
    @Headers("Content-Type: application/json", "Accept: application/json")
    @PUT
    suspend fun sendDataStepFourSubmit(
        @Url endpoint: String,
        @Header("X-Method") method: String,  // Custom header
        @Body request: LoanStepFourSubmitRequest,
    ): Response<LoanStepFourSubmitResponse>

    // Step 5 plus load
    @Headers("Content-Type: application/json", "Accept: application/json")
    @POST
    suspend fun sendDataStepFivePlusLoad(
        @Url endpoint: String,
        @Header("X-Method") method: String,  // Custom header
        @Body request: LoanStepFivePlusLoadRequest,
    ): Response<LoanStepFivePlusLoadResponse>

    // Step 5 plus load 2
    @Headers("Content-Type: application/json", "Accept: application/json")
    @POST
    suspend fun sendDataStepFivePlusLoadTwo(
        @Url endpoint: String,
        @Header("X-Method") method: String,  // Custom header
        @Body request: LoanStepFivePlusLoadRequest,
    ): Response<LoanStepFivePlusLoadTwoResponse>

    // Step 5 plus submit
    @Headers("Content-Type: application/json", "Accept: application/json")
    //@PUT // AMBIENTE TEST
    @POST  // AMBIENTE PROD
    suspend fun sendDataStepFivePlusSubmit(
        @Url endpoint: String,
        @Header("X-Method") method: String,  // Custom header
        @Body request: LoanStepFivePlusSubmitRequest,
    ): Response<LoanStepFivePlusSubmitResponse>

    // Step 5 load (primerizos)
    @Headers("Content-Type: application/json", "Accept: application/json")
    @POST
    suspend fun sendDataStepFiveLoad(
        @Url endpoint: String,
        @Header("X-Method") method: String,  // Custom header
        @Body request: LoanStepFiveLoadRequest,
    ): Response<LoanStepFiveLoadResponse>

    // Step 5 submit (primerizos)
    @Headers("Content-Type: application/json", "Accept: application/json")
    @POST
    suspend fun sendDataStepFiveSubmit(
        @Url endpoint: String,
        @Header("X-Method") method: String,  // Custom header
        @Body request: LoanStepFiveSubmitRequest,
    ): Response<LoanStepFiveSubmitResponse>

    // Desembolso Tumipay (primerizos)
    @Headers("Content-Type: application/json", "Accept: application/json")
    @POST
    suspend fun sendDataTumiPayDisbursement(
        @Url endpoint: String,
        @Header("X-Method") method: String,  // Custom header
        @Body request: LoanTumiPayRequest,
    ): Response<LoanTumiPayResponse>

    // Desembolso Tumipay plus
    @Headers("Content-Type: application/json", "Accept: application/json")
    @PUT
    suspend fun sendDataTumiPayPlusDisbursement(
        @Url endpoint: String,
        @Header("X-Method") method: String,  // Custom header
        @Body request: LoanTumiPayRequest,
    ): Response<LoanTumiPayResponse>

    // RENOVACION

    // Renovacion STEP4 mini Load
    @Headers("Content-Type: application/json", "Accept: application/json")
    @POST
    suspend fun sendDataRenewalProposalLoad(
        @Url endpoint: String,
        @Header("X-Method") method: String,  // Custom header
        @Body request: LoanRenewalProposalLoadRequest,
    ): Response<LoanRenewalProposalLoadResponse>

    // Renovacion STEP4 mini Submit
    @Headers("Content-Type: application/json", "Accept: application/json")
    @POST
    suspend fun sendDataRenewalProposalSubmit(
        @Url endpoint: String,
        @Header("X-Method") method: String,  // Custom header
        @Body request: LoanRenewalProposalSubmitRequest,
    ): Response<LoanRenewalProposalSubmitResponse>

    // Renovacion Info Step mini Load
    @Headers("Content-Type: application/json", "Accept: application/json")
    @POST
    suspend fun sendDataRenewalInfoLoad(
        @Url endpoint: String,
        @Header("X-Method") method: String,  // Custom header
        @Body request: LoanRenewalInfoLoadRequest,
    ): Response<LoanRenewalInfoLoadResponse>


    // Renovacion STEP4 Plus Load
    @Headers("Content-Type: application/json", "Accept: application/json")
    @POST
    suspend fun sendDataRenewalStepFourPlusLoad(
        @Url endpoint: String,
        @Header("X-Method") method: String,  // Custom header
        @Body request: LoanRenewalProposalLoadRequest,
    ): Response<LoanRenewalProposalLoadResponse>

    // Renovacion STEP4 mini Submit
    @Headers("Content-Type: application/json", "Accept: application/json")
    @POST
    suspend fun sendDataRenewalStepFourPlusSubmit(
        @Url endpoint: String,
        @Header("X-Method") method: String,  // Custom header
        @Body request: LoanRenewalStepFourPlusSubmitRequest,
    ): Response<LoanRenewalProposalSubmitResponse>

    // PLP

    @Headers("Content-Type: application/json", "Accept: application/json")
    @POST
    suspend fun sendDataPlpStep1(
        @Url endpoint: String,
        @Header("X-Method") method: String,  // Custom header
        @Body request: PLPLoanStep1Request,
    ): Response<PlpLoanStep1Response>

    @Headers("Content-Type: application/json", "Accept: application/json")
    @POST
    suspend fun sendDataPlpStep2(
        @Url endpoint: String,
        @Header("X-Method") method: String,  // Custom header
        @Body request: PLPLoanStep2Request,
    ): Response<PlpLoanStep2Response>

    @Headers("Content-Type: application/json", "Accept: application/json")
    @PUT
    suspend fun sendDataPlpStep3(
        @Url endpoint: String,
        @Header("X-Method") method: String,  // Custom header
        @Body request: PLPLoanStep3Request,
    ): Response<PlpLoanStep3Response>

    @Headers("Content-Type: application/json", "Accept: application/json")
    @POST
    suspend fun sendDataPlpStep4(
        @Url endpoint: String,
        @Header("X-Method") method: String,  // Custom header
        @Body request: PLPLoanStep4Request,
    ): Response<PlpLoanStep4Response>

    @Headers("Content-Type: application/json", "Accept: application/json")
    @POST
    suspend fun sendDataPlpStep5(
        @Url endpoint: String,
        @Header("X-Method") method: String,  // Custom header
        @Body request: PLPLoanStep5Request,
    ): Response<PlpLoanStep5Response>

    // OTP

    @Headers("Content-Type: application/json", "Accept: application/json")
    @POST
    suspend fun sendDataOTPVerify(
        @Url endpoint: String,
        @Header("X-Method") method: String,  // Custom header
        @Body request: OTPVerifyRequest,
    ): Response<OTPVerifyResponse>

    @Headers("Content-Type: application/json", "Accept: application/json")
    @POST
    suspend fun sendDataOTPProcess(
        @Url endpoint: String,
        @Header("X-Method") method: String,  // Custom header
        @Body request: OTPProcessRequest,
    ): Response<OTPVerifyResponse>

    // QUESTIONS

    @Headers("Content-Type: application/json", "Accept: application/json")
    @POST
    suspend fun sendDataQuestions(
        @Url endpoint: String,
        @Header("X-Method") method: String,  // Custom header
        @Body request: QuestionsRequest,
    ): Response<QuestionResponse>

    @Headers("Content-Type: application/json", "Accept: application/json")
    @POST
    suspend fun sendDataQuestionsValidation(
        @Url endpoint: String,
        @Header("X-Method") method: String,  // Custom header
        @Body request: QuestionsValidationRequest,
    ): Response<QuestionValidationResponse>

    @Headers("Content-Type: application/json", "Accept: application/json")
    @POST
    suspend fun sendDataQuestionsVerification(
        @Url endpoint: String,
        @Header("X-Method") method: String,  // Custom header
        @Body request: QuestionsRequest,
    ): Response<QuestionVerificationResponse>

}