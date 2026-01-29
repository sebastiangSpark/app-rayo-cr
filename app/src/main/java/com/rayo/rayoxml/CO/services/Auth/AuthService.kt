package com.rayo.rayoxml.co.services.Auth

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Url

interface AuthService {
    @Headers("Content-Type: application/json", "Accept: application/json")
    @POST
    suspend fun login(
        @Url endpoint: String,
        @Header("X-Method") method: String,  // Custom header
        @Body request: LoginRequest,
    ): Response<LoginResponse>

    @Headers("Content-Type: application/json", "Accept: application/json")
    @POST
    suspend fun passwordRecovery(
        @Url endpoint: String,
        @Header("X-Method") method: String,  // Custom header
        @Body request: PasswordRecoveryRequest,
    ): Response<PasswordRecoveryResponse>
}