package com.rayo.rayoxml.mx.models

data class LoanCard(
    val hasActiveLoan: Boolean,
    val title: String,
    val state: String,
    val amount: String? = null,
    val payment: String? = null,
    val date: String )
