package com.rayo.rayoxml.co.services.User

import com.rayo.rayoxml.utils.EnvConfigCO
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class UserRepository {
    private val api: UserService

    init {
        // Cambiar user agent
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val newRequest = chain.request().newBuilder()
                    .header("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:136.0) Gecko/20100101 Firefox/136.0")
                    .build()
                chain.proceed(newRequest)
            }
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(EnvConfigCO.BASE_URL)
            .client(client) // Cambiar user agent
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        api = retrofit.create(UserService::class.java)
    }

    suspend fun getData(contactId: String): UserResponse? {
        return try {
            println("Req: $contactId")
            val response = api.getData(
                EnvConfigCO.API_URL,
                "contacto/",  // Valor de header x-token
                UserRequest(contactId)
            )
            if (response.isSuccessful){
                println("✅ Successful: ${response.body()?.solicitud?.result}")
                println("User Data: ${response.body()?.solicitud}")
                /*response.body()?.solicitud?.prestamos?.forEach { prestamo ->
                    println("📌 Prestamo ID: ${prestamo.prestamoId}, Estado: ${prestamo.estado}")
                }*/
                response.body() // retornar datos
            } else null
        } catch (e: Exception) {
            println("❗ Exception contacto: ${e.message}")
            null
        }
    }
}