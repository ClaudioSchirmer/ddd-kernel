package br.dev.schirmer.ddd.kernel.infrastructure.log

import java.util.*

data class Data(
    val id: UUID?,
    val fields: Any
)