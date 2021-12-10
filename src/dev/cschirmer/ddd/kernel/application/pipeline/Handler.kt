package dev.cschirmer.ddd.kernel.application.pipeline

interface Handler<TResult, TRequest: Request<TResult>> {
	operator fun invoke(request: TRequest) : TResult
}