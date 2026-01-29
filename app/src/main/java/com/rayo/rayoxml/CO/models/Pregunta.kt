package com.rayo.rayoxml.co.models

data class Pregunta(
    val id: Int,
    val pregunta: String,
    val opciones: List<String>,
    val idPregunta: String,
    val ordenPregunta: Int
)
