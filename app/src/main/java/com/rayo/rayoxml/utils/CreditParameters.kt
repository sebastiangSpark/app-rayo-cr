package com.rayo.rayoxml.utils
import com.google.gson.annotations.SerializedName

// Credit Parameters

data class CreditParameters(
    val minAmount: Int,
    val maxAmount: Int,
    val ranges: Int,
    val maxQuotes: Int,
    val interestRate: Double,
    val tecnology: Double,
    val tax: Double,
    val taxConfe: Double
)

data class CountryCreditParameters(
    @SerializedName("co") val co: CreditParameters,
    @SerializedName("mx") val mx: CreditParameters,
    @SerializedName("cr") val cr: CreditParameters
)

// Credit Information

data class CreditInformation(
    val interest: String,
    val tecnology: String,
    val taxConfe: String,
    val important: String
)

data class CountryCreditInformation(
    @SerializedName("co") val co: CreditInformation,
    @SerializedName("mx") val mx: CreditInformation,
    @SerializedName("cr") val cr: CreditInformation
)
