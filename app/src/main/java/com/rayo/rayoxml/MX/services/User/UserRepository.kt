package com.rayo.rayoxml.mx.services.User

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class UserRepository {
    private val api: UserService

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://co.rayocredit.mx/API/") // Agregar environments
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        api = retrofit.create(UserService::class.java)
    }

    suspend fun getData(contactId: String): UserResponse? {
        return try {
            val response = api.getData(
                "contacto/",  // Valor de header x-token
                UserRequest(contactId)
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
    }

    suspend fun getDataAlt(contactId: String): Boolean {
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
    }
}