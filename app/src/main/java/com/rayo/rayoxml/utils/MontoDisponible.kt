package com.rayo.rayoxml.utils

data class MontoDisponible(
    val amount: Int,
    val disabled: Boolean
)

data class RangoMonto(
    val minSalario: Int,
    val maxSalario: Int,
    val cantidadPrestamos: Int,
    val listMontos: List<MontoDisponible>
)

val MONTOS = listOf(
    RangoMonto(
        minSalario = 280000,
        maxSalario = 349999,
        cantidadPrestamos = 2,
        listMontos = listOf(
            MontoDisponible(amount = 30000, disabled = false),
            MontoDisponible(amount = 35000, disabled = false)
        )
    ),
    RangoMonto(
        minSalario = 350000,
        maxSalario = 424999,
        cantidadPrestamos = 4,
        listMontos = listOf(
            MontoDisponible(amount = 35000, disabled = false),
            MontoDisponible(amount = 40000, disabled = false)
        )
    ),
    RangoMonto(
        minSalario = 425000,
        maxSalario = 499999,
        cantidadPrestamos = 5,
        listMontos = listOf(
            MontoDisponible(amount = 40000, disabled = false),
            MontoDisponible(amount = 45000, disabled = false)
        )
    ),
    RangoMonto(
        minSalario = 500000,
        maxSalario = 574999,
        cantidadPrestamos = 7,
        listMontos = listOf(
            MontoDisponible(amount = 45000, disabled = false),
            MontoDisponible(amount = 50000, disabled = false)
        )
    ),
    RangoMonto(
        minSalario = 575000,
        maxSalario = 624999,
        cantidadPrestamos = 7,
        listMontos = listOf(
            MontoDisponible(amount = 45000, disabled = false),
            MontoDisponible(amount = 50000, disabled = false)
        )
    ),
    RangoMonto(
        minSalario = 625000,
        maxSalario = 699999,
        cantidadPrestamos = 8,
        listMontos = listOf(
            MontoDisponible(amount = 55000, disabled = false),
            MontoDisponible(amount = 60000, disabled = false)
        )
    ),
    RangoMonto(
        minSalario = 700000,
        maxSalario = 849999,
        cantidadPrestamos = 8,
        listMontos = listOf(
            MontoDisponible(amount = 80000, disabled = false),
            MontoDisponible(amount = 85000, disabled = false)
        )
    ),
    RangoMonto(
        minSalario = 850000,
        maxSalario = 1249999,
        cantidadPrestamos = 7,
        listMontos = listOf(
            MontoDisponible(amount = 120000, disabled = false),
            MontoDisponible(amount = 130000, disabled = false)
        )
    ),
    RangoMonto(
        minSalario = 1250000,
        maxSalario = 1749999,
        cantidadPrestamos = 7,
        listMontos = listOf(
            MontoDisponible(amount = 120000, disabled = false),
            MontoDisponible(amount = 130000, disabled = false),
            MontoDisponible(amount = 140000, disabled = false)
        )
    ),
    RangoMonto(
        minSalario = 1750000,
        maxSalario = Int.MAX_VALUE,
        cantidadPrestamos = 5,
        listMontos = listOf(
            MontoDisponible(amount = 120000, disabled = false),
            MontoDisponible(amount = 130000, disabled = false),
            MontoDisponible(amount = 140000, disabled = false),
            MontoDisponible(amount = 150000, disabled = false)
        )
    )
)
