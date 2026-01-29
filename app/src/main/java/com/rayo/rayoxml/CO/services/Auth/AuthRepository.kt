package com.rayo.rayoxml.co.services.Auth

import com.rayo.rayoxml.utils.EnvConfigCO
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AuthRepository {
    private val api: AuthService

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl(EnvConfigCO.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        api = retrofit.create(AuthService::class.java)
    }

    suspend fun loginUser(email: String, password: String): LoginResponse? {
        return try {
            val response = api.login(
                EnvConfigCO.API_URL,
                "login/",  // Valor de header x-token
                LoginRequest(email, password)
            )

            if (response.isSuccessful) {
                val authResult = response.body()?.solicitud
                if (authResult?.acceso == "aceptado") {
                    println("✅ Login Successful: ${authResult.result}")
                    response.body()  // Login successful
                } else {
                    println("❌ Login Failed: ${authResult?.result ?: "Unknown error"}")
                    null  // Login failed
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

    suspend fun passwordRecovery(email: String): PasswordRecoveryResponse? {
        return try {
            val response = api.passwordRecovery(
                EnvConfigCO.API_URL,
                "resetPassword/",  // Valor de header x-token
                PasswordRecoveryRequest(email)
            )

            if (response.isSuccessful) {
                val authResult = response.body()
                if (authResult?.codigo == "002") {
                    println("✅ Password Reset Successful: ${authResult.mensajeSalida}")
                    response.body()
                } else {
                    println("❌ Password Reset Failed: ${authResult ?: "Unknown error"}")
                    null
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
}