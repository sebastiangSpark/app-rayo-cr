package com.rayo.rayoxml.co.services.Payment

import com.rayo.rayoxml.utils.EnvConfigCO
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class PaymentRepository {
    private val api: PaymentService

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl(EnvConfigCO.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        api = retrofit.create(PaymentService::class.java)
    }

    suspend fun getPaymentUrlData(data: PaymentLinkRequest): PaymentLinkResponse? {
        return try {
            val response = api.getPaymentLink(
                EnvConfigCO.API_URL,
                "cobreStatusLink/",  // Valor de header x-token
                data
            )
            if (response.isSuccessful){
                println("✅ Successful: ${response.body()?.solicitud?.result}")
                println("Payment Link Data: ${response.body()?.solicitud}")
                response.body() // retornar datos
            } else null
        } catch (e: Exception) {
            println("❗ Exception: ${e.message}")
            null
        }
    }

    suspend fun createPaymentUrl(data: CreatePaymentLinkRequest): PaymentLinkResponse? {
        return try {
            val response = api.createPaymentLink(
                EnvConfigCO.API_URL,
                "cobre/",  // Valor de header x-token
                data
            )
            if (response.isSuccessful){
                println("✅ Successful: ${response.body()?.solicitud?.result}")
                println("Create Payment Link Data: ${response.body()?.solicitud}")
                response.body() // retornar datos
            } else null
        } catch (e: Exception) {
            println("❗ Exception: ${e.message}")
            null
        }
    }

    suspend fun recreatePaymentUrl(data: CreatePaymentLinkRequest): PaymentLinkResponse? {
        return try {
            val response = api.createPaymentLink(
                EnvConfigCO.API_URL,
                "cobreLink/",  // Valor de header x-token
                data
            )
            if (response.isSuccessful){
                println("✅ Successful: ${response.body()?.solicitud?.result}")
                println("Recreate Payment Link Data: ${response.body()?.solicitud}")
                response.body() // retornar datos
            } else null
        } catch (e: Exception) {
            println("❗ Exception: ${e.message}")
            null
        }
    }
}