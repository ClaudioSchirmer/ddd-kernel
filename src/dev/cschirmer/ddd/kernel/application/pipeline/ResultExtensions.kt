package dev.cschirmer.ddd.kernel.application.pipeline

import dev.cschirmer.ddd.kernel.application.notifications.NotificationContextDTO

/* ResultOptions Configuration */
fun <TResult, TReturn> Result.Actions<TResult, TReturn>.ifSuccess(action: TResult.() -> TReturn) = apply {
        doIfSuccess = action
    }

fun <TResult, TReturn> Result.Actions<TResult, TReturn>.ifFailure(action: List<NotificationContextDTO>.() -> TReturn) =
    apply {
        doIfFailure = action
    }

fun <TResult, TReturn> Result.Actions<TResult, TReturn>.ifException(action: Throwable.() -> TReturn) = apply {
        doIfException = action
    }

/* RESULT */
inline fun <TResult> Result<TResult>.ifSuccess(action: TResult.() -> Unit) {
    if (this is Result.Success) {
        this.value.action()
    }
}

inline fun <TResult, TReturn> Result<TResult>.getFromResult(noinline defaultActionOnFailure: (() -> TReturn)? = null, actions: Result.Actions<TResult, TReturn>.() -> Unit): TReturn =
    runCatching {
        Result.Actions<TResult, TReturn>().apply(actions).run {
            when (this@getFromResult) {
                is Result.Success ->
                    doIfSuccess!!.invoke(this@getFromResult.value)
                is Result.Failure ->
                    doIfFailure!!.invoke(this@getFromResult.notificationContext)
                is Result.Exception ->
                    doIfException!!.invoke(this@getFromResult.exception)
            }
        }
    }.getOrElse {
        defaultActionOnFailure!!.invoke()
    }


inline fun <TResult> Result<TResult>.withResult(actions: Result.Actions<TResult, Unit>.() -> Unit) {
    Result.Actions<TResult, Unit>().apply(actions).run {
        when (this@withResult) {
            is Result.Success -> doIfSuccess?.invoke(this@withResult.value)
            is Result.Failure -> doIfFailure?.invoke(this@withResult.notificationContext)
            is Result.Exception -> doIfException?.invoke(this@withResult.exception)
        }
    }
}

/* RESULT LIST */
inline fun <TResult, TReturn> List<Result<TResult>>.getFromFirstResult(noinline defaultActionOnFailure: (() -> TReturn)? = null, actions: Result.Actions<TResult, TReturn>.() -> Unit) : TReturn =
    first().getFromResult(defaultActionOnFailure, actions)

inline fun <TResult> List<Result<TResult>>.withFirstResult(actions: Result.Actions<TResult, Unit>.() -> Unit) =
    first().withResult(actions)

inline fun <TResult> List<Result<TResult>>.withFirstIfSuccess(action: TResult.() -> Unit) = first().ifSuccess(action)

inline fun <TResult> List<Result<TResult>>.forEachResult(actions: Result.Actions<TResult, Unit>.() -> Unit) {
    forEach {
        Result.Actions<TResult, Unit>().apply(actions).run {
            when (it) {
                is Result.Success -> doIfSuccess?.invoke(it.value)
                is Result.Failure -> doIfFailure?.invoke(it.notificationContext)
                is Result.Exception -> doIfException?.invoke(it.exception)
            }
        }
    }
}