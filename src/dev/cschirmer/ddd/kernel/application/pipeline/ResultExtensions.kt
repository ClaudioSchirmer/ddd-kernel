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

inline fun <reified TResult> List<Result<TResult>>.runWithFirst(run: Result<TResult>.() -> Unit) = first().run()

inline fun <reified TResult, TReturn> List<Result<TResult>>.whenFirst(
    success: (TResult.() -> TReturn),
    failure: (List<NotificationContextDTO>.() -> TReturn),
    exception: (Throwable.() -> TReturn)
): TReturn {
    return when (val first = first()) {
        is Result.Success -> first.value.success()
        is Result.Failure -> first.notificationContext.failure()
        is Result.Exception -> first.exception.exception()
    }
}

inline fun <reified TResult> List<Result<TResult>>.forEach(
    noinline success: (TResult.() -> Unit)? = null,
    noinline failure: (List<NotificationContextDTO>.() -> Unit)? = null,
    noinline exception: (Throwable.() -> Unit)? = null
) {
    forEach {
        when {
            it is Result.Success && success != null -> it.value.success()
            it is Result.Failure && failure != null -> it.notificationContext.failure()
            it is Result.Exception && exception != null -> it.exception.exception()
        }
    }
}