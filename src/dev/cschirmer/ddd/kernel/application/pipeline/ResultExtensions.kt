package dev.cschirmer.ddd.kernel.application.pipeline

import dev.cschirmer.ddd.kernel.application.notifications.NotificationContextDTO

/* ResultOptions Configuration */
inline fun <reified TResult, TReturn> Result.Actions<TResult, TReturn>.ifSuccess(noinline action: TResult.() -> TReturn) =
    apply {
        doIfSuccess = action
    }

inline fun <reified TResult, TReturn> Result.Actions<TResult, TReturn>.ifFailure(noinline action: List<NotificationContextDTO>.() -> TReturn) =
    apply {
        doIfFailure = action
    }

inline fun <reified TResult, TReturn> Result.Actions<TResult, TReturn>.ifException(noinline action: Throwable.() -> TReturn) =
    apply {
        doIfException = action
    }

/* RESULT */
inline fun <reified TResult, TReturn> Result<TResult>.getFromResult(options: Result.Actions<TResult, TReturn>.() -> Unit): TReturn? {
    Result.Actions<TResult, TReturn>().apply(options).run {
        return when (this@getFromResult) {
            is Result.Success ->
                doIfSuccess?.invoke(this@getFromResult.value)
            is Result.Failure ->
                doIfFailure?.invoke(this@getFromResult.notificationContext)
            is Result.Exception ->
                doIfException?.invoke(this@getFromResult.exception)
        }
    }
}

inline fun <reified TResult> Result<TResult>.withResult(options: Result.Actions<TResult, Unit>.() -> Unit) {
    Result.Actions<TResult, Unit>().apply(options).run {
        when (this@withResult) {
            is Result.Success -> doIfSuccess?.invoke(this@withResult.value)
            is Result.Failure -> doIfFailure?.invoke(this@withResult.notificationContext)
            is Result.Exception -> doIfException?.invoke(this@withResult.exception)
        }
    }
}

/* RESULT LIST */
inline fun <reified TResult, TReturn> List<Result<TResult>>.getFromFirstResult(options: Result.Actions<TResult, TReturn>.() -> Unit) : TReturn? =
    first().getFromResult(options)

inline fun <reified TResult> List<Result<TResult>>.withFirstResult(options: Result.Actions<TResult, Unit>.() -> Unit) =
    first().withResult(options)

inline fun <reified TResult> List<Result<TResult>>.forEachResult(options: Result.Actions<TResult, Unit>.() -> Unit) {
    forEach {
        Result.Actions<TResult, Unit>().apply(options).run {
            when (it) {
                is Result.Success -> doIfSuccess?.invoke(it.value)
                is Result.Failure -> doIfFailure?.invoke(it.notificationContext)
                is Result.Exception -> doIfException?.invoke(it.exception)
            }
        }
    }
}