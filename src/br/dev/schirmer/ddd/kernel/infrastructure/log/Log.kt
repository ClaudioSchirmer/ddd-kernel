package br.dev.schirmer.ddd.kernel.infrastructure.log

data class Log(
    val message: String,
    val values: Any?,
    val exception: Throwable?
)
