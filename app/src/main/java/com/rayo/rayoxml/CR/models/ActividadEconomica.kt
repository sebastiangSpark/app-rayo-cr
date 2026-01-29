package com.rayo.rayoxml.cr.models

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException

data class ActividadEconomica(
    val code: Int,
    val name: String
)

fun loadActividadesEconomicasiasFromJson(context: Context): List<ActividadEconomica> {
    return try {
        val jsonString = context.assets.open("ActividadesEconomicasCR.json").bufferedReader().use { it.readText() }
        val actividadesEconomicasList = object : TypeToken<List<ActividadEconomica>>() {}.type
        Gson().fromJson(jsonString, actividadesEconomicasList) ?: emptyList()
    } catch (e: IOException) {
        emptyList()
    }
}