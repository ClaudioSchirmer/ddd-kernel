package br.dev.schirmer.ddd.kernel.domain.models

fun interface UnitOfWork<T> {
    suspend fun invoke() : T
}