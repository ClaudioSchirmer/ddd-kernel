package dev.cschirmer.ddd.kernel.application.pipeline

interface Handler<TResult, TRequest: Request<TResult>> {
	suspend operator fun invoke(request: TRequest) : TResult
}