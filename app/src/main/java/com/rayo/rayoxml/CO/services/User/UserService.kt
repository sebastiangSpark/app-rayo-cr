package com.rayo.rayoxml.co.services.User

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Url

interface UserService {
    @Headers("Content-Type: application/json", "Accept: application/json")
    @POST
    suspend fun getData(
        @Url endpoint: String,
        @Header("X-Method") method: String,  // Custom header
        @Body request: UserRequest,
    ): Response<UserResponse>
}