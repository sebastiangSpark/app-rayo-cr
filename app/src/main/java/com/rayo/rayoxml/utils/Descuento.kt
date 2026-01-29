package com.rayo.rayoxml.utils


data class Descuento(
    val cantidad: Int,
    val descuento: Int
)

data class Monto(
    val monto: Int,
    val listCantidadPrestamos: List<Descuento>
)

data class DescuentoTecnologia(
    val nCuotas: Int,
    val listMontos: List<Monto>
)

val DESCUENTOS = listOf(
    DescuentoTecnologia(
        nCuotas = 1,
        listMontos = listOf(
            Monto(monto = 30000, listCantidadPrestamos = listOf(
                Descuento(cantidad = 12, descuento = 7379),
                Descuento(cantidad = 3, descuento = 5453)
            )),
            Monto(monto = 35000, listCantidadPrestamos = listOf(
                Descuento(cantidad = 12, descuento = 7923),
                Descuento(cantidad = 3, descuento = 5675)
            )),
            Monto(monto = 40000, listCantidadPrestamos = listOf(
                Descuento(cantidad = 12, descuento = 8468),
                Descuento(cantidad = 3, descuento = 5897)
            )),
            Monto(monto = 45000, listCantidadPrestamos = listOf(
                Descuento(cantidad = 12, descuento = 9012),
                Descuento(cantidad = 3, descuento = 6118)
            )),
            Monto(monto = 50000, listCantidadPrestamos = listOf(
                Descuento(cantidad = 12, descuento = 9557),
                Descuento(cantidad = 3, descuento = 6340)
            )),
            Monto(monto = 55000, listCantidadPrestamos = listOf(
                Descuento(cantidad = 12, descuento = 10101),
                Descuento(cantidad = 3, descuento = 6562)
            )),
            Monto(monto = 60000, listCantidadPrestamos = listOf(
                Descuento(cantidad = 12, descuento = 10646),
                Descuento(cantidad = 3, descuento = 6784)
            )),
            Monto(
                monto = 80000,
                listCantidadPrestamos = listOf(
                    Descuento(12, 13137),
                    Descuento(3, 8319)
                )
            ),
            Monto(
                monto = 120000,
                listCantidadPrestamos = listOf(
                    Descuento(12, 12850),
                    Descuento(3, 6426)
                )
            ),
            Monto(
                monto = 130000,
                listCantidadPrestamos = listOf(
                    Descuento(12, 13350),
                    Descuento(3, 6926)
                )
            ),
            Monto(
                monto = 140000,
                listCantidadPrestamos = listOf(
                    Descuento(12, 13850),
                    Descuento(3, 7426)
                )
            ),
            Monto(
                monto = 150000,
                listCantidadPrestamos = listOf(
                    Descuento(12, 14350),
                    Descuento(3, 7926)
                )
            ),
            Monto(
                monto = 85000,
                listCantidadPrestamos = listOf(
                    Descuento(12, 13137),
                    Descuento(3, 8819)
                )
            )
        )
    ),
    DescuentoTecnologia(
        nCuotas = 2,
        listMontos = listOf(
            Monto(monto = 30000, listCantidadPrestamos = listOf(
                Descuento(cantidad = 12, descuento = 11505),
                Descuento(cantidad = 3, descuento = 9580)
            )),
            Monto(monto = 35000, listCantidadPrestamos = listOf(
                Descuento(cantidad = 12, descuento = 12271),
                Descuento(cantidad = 3, descuento = 9940)
            )),
            Monto(monto = 40000, listCantidadPrestamos = listOf(
                Descuento(cantidad = 12, descuento = 13037),
                Descuento(cantidad = 3, descuento = 10299)
            )),
            Monto(monto = 45000, listCantidadPrestamos = listOf(
                Descuento(cantidad = 12, descuento = 13803),
                Descuento(cantidad = 3, descuento = 10659)
            )),
            Monto(monto = 50000, listCantidadPrestamos = listOf(
                Descuento(cantidad = 12, descuento = 14569),
                Descuento(cantidad = 3, descuento = 11018)
            )),
            Monto(monto = 55000, listCantidadPrestamos = listOf(
                Descuento(cantidad = 12, descuento = 15335),
                Descuento(cantidad = 3, descuento = 11378)
            )),
            Monto(monto = 60000, listCantidadPrestamos = listOf(
                Descuento(cantidad = 12, descuento = 16101),
                Descuento(cantidad = 3, descuento = 11737)
            )),
            Monto(
                monto = 80000,
                listCantidadPrestamos = listOf(
                    Descuento(12, 17637),
                    Descuento(3, 12818)
                )
            ),
            Monto(
                monto = 120000,
                listCantidadPrestamos = listOf(
                    Descuento(12, 14350),
                    Descuento(3, 7925)
                )
            ),
            Monto(
                monto = 130000,
                listCantidadPrestamos = listOf(
                    Descuento(12, 14850),
                    Descuento(3, 8425)
                )
            ),
            Monto(
                monto = 140000,
                listCantidadPrestamos = listOf(
                    Descuento(12, 15350),
                    Descuento(3, 8925)
                )
            ),
            Monto(
                monto = 150000,
                listCantidadPrestamos = listOf(
                    Descuento(12, 15850),
                    Descuento(3, 9425)
                )
            ),
            Monto(
                monto = 85000,
                listCantidadPrestamos = listOf(
                    Descuento(12, 18137),
                    Descuento(3, 13818)
                )
            )
        )
    ),
    DescuentoTecnologia(
        nCuotas = 3,
        listMontos = listOf(
            Monto(monto = 30000, listCantidadPrestamos = listOf(
                Descuento(cantidad = 12, descuento = 15630),
                Descuento(cantidad = 3, descuento = 13702)
            )),
            Monto(monto = 35000, listCantidadPrestamos = listOf(
                Descuento(cantidad = 12, descuento = 16619),
                Descuento(cantidad = 3, descuento = 14338)
            )),
            Monto(monto = 40000, listCantidadPrestamos = listOf(
                Descuento(cantidad = 12, descuento = 17608),
                Descuento(cantidad = 3, descuento = 14975)
            )),
            Monto(monto = 45000, listCantidadPrestamos = listOf(
                Descuento(cantidad = 12, descuento = 18597),
                Descuento(cantidad = 3, descuento = 15611)
            )),
            Monto(monto = 50000, listCantidadPrestamos = listOf(
                Descuento(cantidad = 12, descuento = 19586),
                Descuento(cantidad = 3, descuento = 16248)
            )),
            Monto(monto = 55000, listCantidadPrestamos = listOf(
                Descuento(cantidad = 12, descuento = 20575),
                Descuento(cantidad = 3, descuento = 16884)
            )),
            Monto(monto = 60000, listCantidadPrestamos = listOf(
                Descuento(cantidad = 12, descuento = 21564),
                Descuento(cantidad = 3, descuento = 17521)
            )),
            Monto(
                monto = 80000,
                listCantidadPrestamos = listOf(
                    Descuento(12, 22137),
                    Descuento(3, 17319)
                )
            ),
            Monto(
                monto = 120000,
                listCantidadPrestamos = listOf(
                    Descuento(12, 15849),
                    Descuento(3, 9426)
                )
            ),
            Monto(
                monto = 130000,
                listCantidadPrestamos = listOf(
                    Descuento(12, 16349),
                    Descuento(3, 9926)
                )
            ),
            Monto(
                monto = 140000,
                listCantidadPrestamos = listOf(
                    Descuento(12, 16849),
                    Descuento(3, 10426)
                )
            ),
            Monto(
                monto = 150000,
                listCantidadPrestamos = listOf(
                    Descuento(12, 17349),
                    Descuento(3, 10926)
                )
            ),
            Monto(
                monto = 85000,
                listCantidadPrestamos = listOf(
                    Descuento(12, 22637),
                    Descuento(3, 17819)
                )
            )
        )
    )
)


// DESCUENTOS RAYO PLUS
val DESCUENTOS_RP = listOf(
    DescuentoTecnologia(
        nCuotas = 1,
        listMontos = listOf(
            Monto(monto = 30000, listCantidadPrestamos = listOf(
                Descuento(cantidad = 12, descuento = 7379),
                Descuento(cantidad = 3, descuento = 5453)
            )),
            Monto(monto = 35000, listCantidadPrestamos = listOf(
                Descuento(cantidad = 12, descuento = 7923),
                Descuento(cantidad = 3, descuento = 5675)
            )),
            Monto(monto = 40000, listCantidadPrestamos = listOf(
                Descuento(cantidad = 12, descuento = 9965),
                Descuento(cantidad = 3, descuento = 7395)
            )),
            Monto(monto = 45000, listCantidadPrestamos = listOf(
                Descuento(cantidad = 12, descuento = 10465),
                Descuento(cantidad = 3, descuento = 7895)
            )),
            Monto(monto = 50000, listCantidadPrestamos = listOf(
                Descuento(cantidad = 12, descuento = 10674),
                Descuento(cantidad = 3, descuento = 7462)
            )),
            Monto(monto = 55000, listCantidadPrestamos = listOf(
                Descuento(cantidad = 12, descuento = 11174),
                Descuento(cantidad = 3, descuento = 7962)
            )),
            Monto(monto = 60000, listCantidadPrestamos = listOf(
                Descuento(cantidad = 12, descuento = 11674),
                Descuento(cantidad = 3, descuento = 8462)
            )),
            Monto(monto = 80000, listCantidadPrestamos = listOf(
                Descuento(12, 13137),
                Descuento(3, 8319)
            )),
            Monto(monto = 120000, listCantidadPrestamos = listOf(
                    Descuento(12, 12850),
                    Descuento(3, 6426)
                )
            ),
            Monto(monto = 130000,listCantidadPrestamos = listOf(
                    Descuento(12, 13350),
                    Descuento(3, 6926)
                )
            ),
            Monto(monto = 140000, listCantidadPrestamos = listOf(
                    Descuento(12, 13850),
                    Descuento(3, 7426)
                )
            ),
            Monto(monto = 150000, listCantidadPrestamos = listOf(
                    Descuento(12, 14350),
                    Descuento(3, 7926)
                )
            ),
            Monto(monto = 85000, listCantidadPrestamos = listOf(
                    Descuento(12, 13137),
                    Descuento(3, 8819)
                )
            )
        )
    ),
    DescuentoTecnologia(
        nCuotas = 2,
        listMontos = listOf(
            Monto(monto = 30000, listCantidadPrestamos = listOf(
                Descuento(cantidad = 12, descuento = 11505),
                Descuento(cantidad = 3, descuento = 9580)
            )),
            Monto(monto = 35000, listCantidadPrestamos = listOf(
                Descuento(cantidad = 12, descuento = 12047),
                Descuento(cantidad = 3, descuento = 9800)
            )),
            Monto(monto = 40000, listCantidadPrestamos = listOf(
                Descuento(cantidad = 12, descuento = 15590),
                Descuento(cantidad = 3, descuento = 13020)
            )),
            Monto(monto = 45000, listCantidadPrestamos = listOf(
                Descuento(cantidad = 12, descuento = 16090),
                Descuento(cantidad = 3, descuento = 13520)
            )),
            Monto(monto = 50000, listCantidadPrestamos = listOf(
                Descuento(cantidad = 12, descuento = 15925),
                Descuento(cantidad = 3, descuento = 12713)
            )),
            Monto(monto = 55000, listCantidadPrestamos = listOf(
                Descuento(cantidad = 12, descuento = 16425),
                Descuento(cantidad = 3, descuento = 13213)
            )),
            Monto(monto = 60000, listCantidadPrestamos = listOf(
                Descuento(cantidad = 12, descuento = 16925),
                Descuento(cantidad = 3, descuento = 13713)
            )),
            Monto(monto = 80000, listCantidadPrestamos = listOf(
                    Descuento(12, 17637),
                    Descuento(3, 12818)
                )
            ),
            Monto(monto = 120000, listCantidadPrestamos = listOf(
                    Descuento(12, 14350),
                    Descuento(3, 7925)
                )
            ),
            Monto(monto = 130000, listCantidadPrestamos = listOf(
                    Descuento(12, 14850),
                    Descuento(3, 8425)
                )
            ),
            Monto(monto = 140000, listCantidadPrestamos = listOf(
                    Descuento(12, 15350),
                    Descuento(3, 8925)
                )
            ),
            Monto(monto = 150000, listCantidadPrestamos = listOf(
                    Descuento(12, 15850),
                    Descuento(3, 9425)
                )
            ),
            Monto(monto = 85000, listCantidadPrestamos = listOf(
                    Descuento(12, 18137),
                    Descuento(3, 13818)
                )
            )
        )
    ),
    DescuentoTecnologia(
        nCuotas = 3,
        listMontos = listOf(
            Monto(monto = 30000, listCantidadPrestamos = listOf(
                Descuento(cantidad = 12, descuento = 15630),
                Descuento(cantidad = 3, descuento = 13702)
            )),
            Monto(monto = 35000, listCantidadPrestamos = listOf(
                Descuento(cantidad = 12, descuento = 16172),
                Descuento(cantidad = 3, descuento = 13926)
            )),
            Monto(monto = 40000, listCantidadPrestamos = listOf(
                Descuento(cantidad = 12, descuento = 21215),
                Descuento(cantidad = 3, descuento = 18645)
            )),
            Monto(monto = 45000, listCantidadPrestamos = listOf(
                Descuento(cantidad = 12, descuento = 21715),
                Descuento(cantidad = 3, descuento = 19145)
            )),
            Monto(monto = 50000, listCantidadPrestamos = listOf(
                Descuento(cantidad = 12, descuento = 21175),
                Descuento(cantidad = 3, descuento = 17964)
            )),
            Monto(monto = 55000, listCantidadPrestamos = listOf(
                Descuento(cantidad = 12, descuento = 21675),
                Descuento(cantidad = 3, descuento = 18464)
            )),
            Monto(monto = 60000, listCantidadPrestamos = listOf(
                Descuento(cantidad = 12, descuento = 22175),
                Descuento(cantidad = 3, descuento = 18964)
            )),
            Monto(monto = 80000, listCantidadPrestamos = listOf(
                    Descuento(12, 22137),
                    Descuento(3, 17319)
                )
            ),
            Monto(monto = 120000, listCantidadPrestamos = listOf(
                    Descuento(12, 15849),
                    Descuento(3, 9426)
                )
            ),
            Monto(monto = 130000, listCantidadPrestamos = listOf(
                    Descuento(12, 16349),
                    Descuento(3, 9926)
                )
            ),
            Monto(monto = 140000, listCantidadPrestamos = listOf(
                    Descuento(12, 16849),
                    Descuento(3, 10426)
                )
            ),
            Monto(monto = 150000, listCantidadPrestamos = listOf(
                    Descuento(12, 17349),
                    Descuento(3, 10926)
                )
            ),
            Monto(monto = 85000, listCantidadPrestamos = listOf(
                    Descuento(12, 22637),
                    Descuento(3, 17819)
                )
            )
        )
    )
)