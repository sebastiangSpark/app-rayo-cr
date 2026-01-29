package com.rayo.rayoxml.cr.models

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException

data class Profesion(
    val code: Int,
    val name: String
)

fun loadProfesionesFromJson(context: Context): List<Profesion> {
    return try {
        val jsonString = context.assets.open("profesionesCR.json").bufferedReader().use { it.readText() }
        val profesionesList = object : TypeToken<List<Profesion>>() {}.type
        Gson().fromJson(jsonString, profesionesList) ?: emptyList()
    } catch (e: IOException) {
        emptyList()
    }
}