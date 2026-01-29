package com.rayo.rayoxml.co.services.Payment

data class PaymentLinkRequest (
    val idPago: String,
    val tipoPrestamo: String
)

data class CreatePaymentLinkRequest (
    val idPago: String,
    val metodoPago: String,
    val notificacionEmail: Boolean,
    val notificacionWhatsapp: Boolean,
    val tipoPrestamo: String
)