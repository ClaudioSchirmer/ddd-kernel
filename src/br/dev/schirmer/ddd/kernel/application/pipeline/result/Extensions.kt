package br.dev.schirmer.ddd.kernel.application.pipeline.result

import br.dev.schirmer.ddd.kernel.application.notifications.NotificationContextDTO
import br.dev.schirmer.ddd.kernel.application.pipeline.Result

/* ResultOptions Configuration */
inline fun <TResult> Result<TResult>.ifSuccess(action: TResult.() -> Unit) {
    if (this is Result.Success) {
        action(value)
    }
}

inline fun <TResult> Result<TResult>.ifFailure(action: List<NotificationContextDTO>.() -> Unit)  {
    if (this is Result.Failure) {
        action(notificationContext)
    }
}

inline fun <TResult> Result<TResult>.ifException(action: Throwable.() -> Unit) {
    if (this is Result.Exception) {
        action(exception)
    }
}

inline fun <TResult, TReturn> Result<TResult>.getFromResult(
    noinline defaultActionOnFailure: (() -> TReturn)? = null,
    actions: Result<TResult>.() -> TReturn
): TReturn =
    runCatching {
        actions()
    }.getOrElse {
        defaultActionOnFailure!!.invoke()
    }


/* RESULT LIST */
inline fun <TResult> List<Result<TResult>>.withFirstResult(
    action: Result<TResult>.() -> Unit
) = first().run(action)


inline fun <TResult, TReturn> List<Result<TResult>>.getFromFirstResult(
    noinline defaultActionOnFailure: (() -> TReturn)? = null,
    action: Result<TResult>.() -> TReturn
): TReturn = first().getFromResult(defaultActionOnFailure, action)


inline fun <TResult> List<Result<TResult>>.withFirstIfSuccess(
    defaultActionOtherwise: (() -> Unit) = {},
    action: TResult.() -> Unit
) = first().run {
    if (this is Result.Success) {
        value.action()
    } else {
        defaultActionOtherwise.invoke()
    }
}

inline fun <TResult> List<Result<TResult>>.forEachResult(action: Result<TResult>.() -> Unit) {
    forEach {
        it.action()
    }
}