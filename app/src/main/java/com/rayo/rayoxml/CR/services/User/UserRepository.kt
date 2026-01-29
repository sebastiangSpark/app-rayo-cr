package com.rayo.rayoxml.cr.services.User

import com.rayo.rayoxml.cr.services.Auth.AuthService
import com.rayo.rayoxml.cr.services.Auth.LoginRequest
import com.rayo.rayoxml.cr.services.Auth.LoginResponse
import com.rayo.rayoxml.utils.EnvConfigCR
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class UserRepository {
    private val apiAuth: AuthService
    private val api: UserService

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

        api = retrofit.create(UserService::class.java)
    }

    /*suspend fun getData(token: String, contactId: String): LoginResponse? {
        return try {
            val response = apiAuth.login(
                "Bearer $token",
                LoginRequest("", "", idCliente = contactId)
            )
            if (response.isSuccessful){
                println("✅ Successful: ${response.body()?.solicitud?.result}")
                println("User Data: ${response.body()?.solicitud}")
                response.body()?.solicitud?.prestamos?.forEach { prestamo ->
                    println("📌 Prestamo ID: ${prestamo.prestamoId}, Estado: ${prestamo.estado}")
                }
                response.body() // retornar datos
            } else null
        } catch (e: Exception) {
            println("❗ Exception: ${e.message}")
            null
        }
    }*/

    /*suspend fun getDataAlt(contactId: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getData(
                    "contacto/",  // Valor de header x-token
                    UserRequest(contactId)
                )

                if (response.isSuccessful) {
                    val data = response.body()
                    return@withContext if (data?.solicitud?.contacto == contactId) {
                        println("✅ Successful: ${data?.solicitud?.result}")
                        println("User Data: ${data?.solicitud}")
                        data?.solicitud?.prestamos?.forEach { prestamo ->
                            println("📌 Prestamo ID: ${prestamo.prestamoId}, Estado: ${prestamo.estado}")
                        }
                        true  // successful
                    } else {
                        println("🚨 Error: ${response.errorBody()?.string()}")
                        false  // failed
                    }
                } else {
                    println("🚨 API Error: ${response.errorBody()?.string()}")
                    return@withContext false
                }
            } catch (e: Exception) {
                println("❗ Exception: ${e.message}")
                return@withContext false
            }
        }
    }*/
}