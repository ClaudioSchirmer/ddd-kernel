package dev.cschirmer.ddd.kernel.web.rest

import dev.cschirmer.ddd.kernel.application.notifications.NotificationContextDTO
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory

fun List<NotificationContextDTO>.toResponse(
    httpStatus: Int = 400,
    httpDescription: String = "Bad Request"
) = Response(status = httpStatus, description = httpDescription, errors = this.map { notificationContext ->
        Error(
            context = notificationContext.context,
            messages = notificationContext.notifications.map { notificationMessage ->
                ErrorMessage(
                    field = notificationMessage.fieldName,
                    value = notificationMessage.fieldValue,
                    message = notificationMessage.message
                )
            }
        )
    })

suspend fun Throwable.toResponse(httpStatus: Int, httpDescription: String): Response = coroutineScope {
    launch(Job()) {
        LoggerFactory.getLogger(this::class.java).error(this@toResponse.message)
    }
    return@coroutineScope Response(status = httpStatus, description = httpDescription)
}

suspend fun Throwable.toBadRequestResponse() = this.toResponse(400, "Bad Request")

suspend fun Throwable.toNotFoundResponse() = this.toResponse(404, "Not Found")

suspend fun Throwable.toInternalServerErrorResponse() = this.toResponse(500, "Internal Server Error")
