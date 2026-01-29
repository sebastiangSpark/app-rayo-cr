package com.rayo.rayoxml.co.models

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException

data class Bank(val name: String)

fun loadBanksFromJson(context: Context): List<Bank> {
    return try {
        val json = context.assets.open("banks.json").bufferedReader().use { it.readText() }
        val listType = object : TypeToken<List<Bank>>() {}.type
        Gson().fromJson(json, listType) ?: emptyList()
    } catch (e: IOException) {
        emptyList()
    }
}