package br.dev.schirmer.ddd.kernel.domain.models

fun interface workUnit<T> {
    suspend fun invoke() : T
}