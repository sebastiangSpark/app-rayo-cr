package com.rayo.rayoxml.cr.services.Auth

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.QueryMap

interface AuthService {
    @Headers("Content-Type: application/x-www-form-urlencoded", "Accept: */*")
    @POST("oauth2/token")
    suspend fun getToken(
        @QueryMap params: Map<String, String>
    ): Response<AuthResponse>

    @Headers("Content-Type: application/json", "Accept: application/json")
    @POST("loginWS")
    suspend fun login(
        @Header("Authorization") token: String,
        @Body request: LoginRequest,
    ): Response<LoginResponse>

    @Headers("Content-Type: application/json", "Accept: application/json")
    @POST
    suspend fun passwordRecovery(
        @Header("X-Method") method: String,  // Custom header
        @Body request: PasswordRecoveryRequest,
    ): Response<PasswordRecoveryResponse>
}