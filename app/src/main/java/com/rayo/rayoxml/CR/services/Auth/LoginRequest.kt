package com.rayo.rayoxml.cr.services.Auth

data class AuthRequest(
    val username: String,
    val password: String,
    val grant_type: String,
    val client_id: String,
    val client_secret: String
)

data class LoginRequest(
    val email: String,
    val pass: String,
    val idCliente: String = ""
)