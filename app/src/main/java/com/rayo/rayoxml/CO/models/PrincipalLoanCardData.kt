package com.rayo.rayoxml.co.models

data class PrincipalLoanCardData (
    val amount: String,
    val state: String,
    val payment: String,
    val date: String,
    val loanId: String,
    val paymentId: String,
    val loanType: String,
    val disbursementDate: String = "",
    val code: String
)