package br.dev.schirmer.ddd.kernel.application.pipeline

import br.dev.schirmer.ddd.kernel.application.configuration.AppContext

abstract class Handler<TResult, TRequest : Request<TResult>> {
    protected lateinit var appContext: AppContext
        private set

    abstract suspend fun invoke(request: TRequest): TResult
    fun setContext(appContext: AppContext) {
        if (!this::appContext.isInitialized) {
            this.appContext = appContext
        }
    }
}