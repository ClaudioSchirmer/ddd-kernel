package dev.cschirmer.ddd.kernel.application.pipeline

import dev.cschirmer.ddd.kernel.application.notifications.NotificationContextDTO

sealed interface Result<out TResult> {
    data class Success<out TResult>(val value: TResult) : Result<TResult>
    data class Failure(val notificationContext: List<NotificationContextDTO>) : Result<Nothing>
    data class Exception(val exception: Throwable) : Result<Nothing>
}