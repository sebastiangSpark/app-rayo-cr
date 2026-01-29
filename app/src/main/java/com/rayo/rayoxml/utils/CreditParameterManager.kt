package com.rayo.rayoxml.utils

import android.util.Log
import com.google.gson.Gson
import org.json.JSONException
import org.json.JSONObject

// Credit Parameters

object CreditParameterManager {
    private var creditParameters: JSONObject? = null
    private var selectedCountry: String = "" // Default country

    fun saveRemoteConfigData(jsonString: String) {
        creditParameters = JSONObject(jsonString)
    }

    fun setSelectedCountry(country: String) {
        selectedCountry = country
    }

    fun getSelectedCountry(): String {
        return selectedCountry
    }

    fun getCreditParameters(): CreditParameters? {
/*        if (creditParameters == null) return null
        val gson = Gson()
        return gson.fromJson(
            creditParameters!!.getJSONObject(selectedCountry.lowercase()).toString(),
            CreditParameters::class.java
        )*/
        if (creditParameters == null) return null

        //val countryKey = "co"
        val countryKey = selectedCountry.lowercase()
        Log.d("CreditParameterManager", "Selected country ${getSelectedCountry()}")

        return try {
            val gson = Gson()
            gson.fromJson(
                creditParameters!!.getJSONObject(countryKey).toString(), // <---- Usa countryKey
                CreditParameters::class.java
            )
        } catch (e: JSONException) {
            Log.e("CreditParameterManager", "Clave '$countryKey' no encontrada. JSON keys: ${creditParameters!!.keys()}")
            null
        }
    }
}

// Credit Information

object CreditInformationManager {
    private var creditInformation: JSONObject? = null
    private var selectedCountry: String = "" // Default country

    fun saveRemoteConfigData(jsonString: String) {
        creditInformation = JSONObject(jsonString)
    }

    fun setSelectedCountry(country: String) {
        selectedCountry = country
    }

    fun getSelectedCountry(): String {
        return selectedCountry
    }

    fun getCreditInformation(): CreditInformation? {
        Log.d("CreditInformationManager", "Selected country ${selectedCountry}")
        if (creditInformation == null) return null
        val gson = Gson()
        return gson.fromJson(
            creditInformation!!.getJSONObject(selectedCountry).toString(),
            CreditInformation::class.java
        )
    }
}