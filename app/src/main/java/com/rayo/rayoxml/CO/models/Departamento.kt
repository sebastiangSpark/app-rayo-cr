package com.rayo.rayoxml.cr.models

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException

data class Departamento(
    val departamento: String,
    val municipios: List<String>
)

fun loadDepartamentosFromJson(context: Context): List<Departamento> {
    return try {
        val jsonString = context.assets.open("departamentos.json").bufferedReader().use { it.readText() }
        val departamentoList = object : TypeToken<List<Departamento>>() {}.type
        Gson().fromJson(jsonString, departamentoList) ?: emptyList()
    } catch (e: IOException) {
        emptyList()
    }
}