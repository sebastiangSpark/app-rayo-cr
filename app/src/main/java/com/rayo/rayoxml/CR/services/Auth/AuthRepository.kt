package com.rayo.rayoxml.cr.services.Auth

import android.util.Log
import com.rayo.rayoxml.utils.EnvConfigCR
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AuthRepository {
    private val api: AuthService
    private val apiAuth: AuthService

    private var authToken = ""

    init {
        val retrofitAuth = Retrofit.Builder()
            .baseUrl(EnvConfigCR.BASE_URL_AUTH)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiAuth = retrofitAuth.create(AuthService::class.java)

        val retrofit = Retrofit.Builder()
            .baseUrl(EnvConfigCR.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        api = retrofit.create(AuthService::class.java)
    }

    suspend fun getToken():AuthResponse? {
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

    suspend fun loginUser(email: String, password: String): LoginResponse? {
        return try {

            if(authToken == "") {
                val authResponse = getToken()
                if (authResponse != null && authResponse.accessToken.isNotEmpty()) {
                    Log.d("AuthRepository CR", "Auth Data: ${authResponse}")
                    authToken = authResponse.accessToken
                }
            }else{
                Log.d("AuthRepository CR", "Auth Data present: ${authToken}")
            }

            val response = api.login(
                "Bearer $authToken",
                LoginRequest(email, password)
            )

            if (response.isSuccessful) {
                val authResult = response.body()
                if (authResult?.id?.isNotEmpty() == true) {
                    println("✅ Login Successful: ${authResult}")
                    response.body()  // Login successful
                } else {
                    println("❌ Login Failed: ${authResult ?: "Unknown error"}")
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

    suspend fun loginUserWithId(id: String): LoginResponse? {
        return try {
            if(authToken == "") {
                val authResponse = getToken()
                if (authResponse != null && authResponse.accessToken.isNotEmpty()) {
                    Log.d("AuthRepository CR", "Auth Data: ${authResponse}")
                    authToken = authResponse.accessToken
                }
            }else{
                Log.d("AuthRepository CR", "Auth Data present: ${authToken}")
            }

            val response = api.login(
                "Bearer $authToken",
                LoginRequest("", "", idCliente = id)
            )

            if (response.isSuccessful) {
                val authResult = response.body()
                if (authResult?.id?.isNotEmpty() == true) {
                    println("✅ Login Successful: ${authResult}")
                    response.body()  // Login successful
                } else {
                    println("❌ Login Failed: ${authResult ?: "Unknown error"}")
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