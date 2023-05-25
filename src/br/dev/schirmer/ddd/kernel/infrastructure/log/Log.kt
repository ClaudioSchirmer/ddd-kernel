package br.dev.schirmer.ddd.kernel.infrastructure.log

import br.dev.schirmer.utils.kotlin.json.AlphabeticalSerialization

data class Log(
    val message: String,
    val values: Any?,
    val exception: Throwable?
) : AlphabeticalSerialization
