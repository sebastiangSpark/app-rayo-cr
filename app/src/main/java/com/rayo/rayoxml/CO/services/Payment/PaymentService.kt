package com.rayo.rayoxml.co.services.Payment

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Url

interface PaymentService {
    @Headers("Content-Type: application/json", "Accept: application/json")
    @POST
    suspend fun getPaymentLink(
        @Url endpoint: String,
        @Header("X-Method") method: String,  // Custom header
        @Body request: PaymentLinkRequest,
    ): Response<PaymentLinkResponse>

    @Headers("Content-Type: application/json", "Accept: application/json")
    @POST
    suspend fun createPaymentLink(
        @Url endpoint: String,
        @Header("X-Method") method: String,  // Custom header
        @Body request: CreatePaymentLinkRequest,
    ): Response<PaymentLinkResponse>
}