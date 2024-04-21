package br.dev.schirmer.ddd.kernel.domain.models

fun interface WorkUnit<T> {
    suspend fun invoke() : T
}