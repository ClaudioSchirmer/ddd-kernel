package br.dev.schirmer.ddd.kernel.application.pipeline

import br.dev.schirmer.ddd.kernel.application.configuration.Context

abstract class Handler<TResult, TRequest : Request<TResult>> {
    protected lateinit var context: Context
        private set

    abstract suspend fun invoke(request: TRequest): TResult
    fun setContext(context: Context) {
        if (!this::context.isInitialized) {
            this.context = context
        }
    }
}