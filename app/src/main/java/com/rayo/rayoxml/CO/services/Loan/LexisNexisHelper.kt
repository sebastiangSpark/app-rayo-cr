package com.rayo.rayoxml.co.services.Loan
import java.security.MessageDigest

class LexisNexisHelper {

    fun applyHashCryptoJS(word: String): String {
        val md = MessageDigest.getInstance("MD5")
        return md.digest(word.toByteArray()).joinToString("") { "%02x".format(it) }
    }

    fun generateRandomString(length: Int): String {
        val allowedChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }

    fun generateSessionId(): String {
        val sessionId = "rayo_${System.currentTimeMillis()}_nolist_" +
                "${generateRandomString(8)}-${generateRandomString(4)}-${generateRandomString(4)}-${generateRandomString(16)}"

        return applyHashCryptoJS(sessionId)
    }

    fun lexisNexis(): String {
        val sessionId = generateSessionId()
        println("Enviando lexis")

        // Aquí se simula la llamada a threatmetrix.profile()
        val callback = "threatmetrix.profile('validacion.rayo.com.co', '2yf5pvfu', '$sessionId')"

        //println(callback)
        return sessionId
    }
}