package dev.cschirmer.ddd.kernel.application.pipeline

import dev.cschirmer.ddd.kernel.application.notifications.NotificationContextDTO

inline fun <reified TResult> Result<TResult>.ifSuccess(callback: TResult.() -> Unit) {
    if (this is Result.Success) {
        value.callback()
    }
}

inline fun <reified TResult> Result<TResult>.ifFailure(callback: List<NotificationContextDTO>.() -> Unit) {
    if (this is Result.Failure) {
        notificationContext.callback()
    }
}

inline fun <reified TResult> Result<TResult>.ifException(callback: Throwable.() -> Unit) {
    if (this is Result.Exception) {
        exception.callback()
    }
}

inline fun <reified TResult> List<Result<TResult>>.runWithFirst(run: Result<TResult>.() -> Unit) {
    first().run()
}

inline fun <reified TResult> List<Result<TResult>>.runWithFirst(noinline ifSuccess: (TResult.() -> Unit)?, noinline ifFailure: (List<NotificationContextDTO>.() -> Unit)?, noinline ifException: (Throwable.() -> Unit)?) {
    val first = first()
    when {
        first is Result.Success && ifSuccess != null -> first.value.ifSuccess()
        first is Result.Failure && ifFailure != null -> first.notificationContext.ifFailure()
        first is Result.Exception && ifException != null -> first.exception.ifException()
    }
}

inline fun <reified TResult> List<Result<TResult>>.forEach(noinline ifSuccess: (TResult.() -> Unit)?, noinline ifFailure: (List<NotificationContextDTO>.() -> Unit)?, noinline ifException: (Throwable.() -> Unit)?) {
    forEach {
        when {
            it is Result.Success && ifSuccess != null -> it.value.ifSuccess()
            it is Result.Failure && ifFailure != null -> it.notificationContext.ifFailure()
            it is Result.Exception && ifException != null -> it.exception.ifException()
        }
    }
}