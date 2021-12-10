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