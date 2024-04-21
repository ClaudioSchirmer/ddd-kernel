package br.dev.schirmer.ddd.kernel.infrastructure.log

data class Export(
    val header: Header,
    val data: Any
)