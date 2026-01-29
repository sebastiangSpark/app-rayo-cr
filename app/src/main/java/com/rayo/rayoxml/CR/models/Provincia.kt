package com.rayo.rayoxml.cr.models

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException

data class Provincia(
    val provincia: String,
    val cantones: List<Canton>
)

data class Canton(
    val nombre: String,
    val distritos: List<String>
)

fun loadProvinciasFromJson(context: Context): List<Provincia> {
    return try {
        val jsonString = context.assets.open("provinciasCR.json").bufferedReader().use { it.readText() }
        val provinciasList = object : TypeToken<List<Provincia>>() {}.type
        Gson().fromJson(jsonString, provinciasList) ?: emptyList()
    } catch (e: IOException) {
        emptyList()
    }
}