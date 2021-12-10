package dev.cschirmer.ddd.kernel.web.rest

import dev.cschirmer.ddd.kernel.application.notifications.NotificationContextDTO
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

fun Throwable.toResponse(httpStatus: Int, httpDescription: String): Response {
    LoggerFactory.getLogger(this::class.java).error(this.message)
    return Response(status = httpStatus, description = httpDescription)
}

fun Throwable.toBadRequestResponse() = this.toResponse(400, "Bad Request")

fun Throwable.toNotFoundResponse() = this.toResponse(404, "Not Found")

fun Throwable.toInternalServerErrorResponse() = this.toResponse(500, "Internal Server Error")
