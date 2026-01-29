package com.rayo.rayoxml.utils
import com.rayo.rayoxml.BuildConfig

object EnvConfigCO {
    private const val IS_PROD = true

    val BASE_URL: String
        get() = BuildConfig.API_URL

    val API_URL: String
        get() = if (IS_PROD) {
            "SalesForce/jwt-command.php"         // ✅ Path producción
        } else {
            "SalesForce/accessToken.php"          // 🔧 Path testing
        }

    val PAYMENT_URL: String
        get() = if (IS_PROD) {
            "https://rayocol.my.site.com/RecaudosRayoColombia/"
        } else {
            "https://rayocol--develop.sandbox.my.site.com/RecaudosRayoColombia/"
        }
}

object EnvConfigCR {

    val BASE_URL_AUTH: String
        get() = BuildConfig.CR_BASE_URL_AUTH

    val BASE_URL: String
        get() = BuildConfig.CR_BASE_URL
    // Token
    val USERNAME: String
        get() = BuildConfig.CR_USERNAME_TOKEN

    val PASSWORD: String
        get() = BuildConfig.CR_PASSWORD_TOKEN

    val CLIENT_ID: String
        get() = BuildConfig.CR_CLIENT_ID_TOKEN

    val CLIENT_SECRET: String
        get() = BuildConfig.CR_CLIENT_SECRET_TOKEN
    // Payment
    val PAYMENT_URL: String
        get() = BuildConfig.CR_PAYMENT_URL
}

object EnvConfigMX {
    private const val IS_PROD = false

    val BASE_URL: String
        get() = if (IS_PROD) {
            ""
        } else {
            ""
        }

    val API_URL: String
        get() = if (IS_PROD) {
            "SalesForce/jwt-command.php"
        } else {
            "SalesForce/accessToken.php"
        }
}
