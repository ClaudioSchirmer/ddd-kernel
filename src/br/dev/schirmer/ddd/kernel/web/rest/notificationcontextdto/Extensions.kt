package br.dev.schirmer.ddd.kernel.web.rest.notificationcontextdto

import br.dev.schirmer.ddd.kernel.application.notifications.NotificationContextDTO
import br.dev.schirmer.ddd.kernel.web.rest.Error
import br.dev.schirmer.ddd.kernel.web.rest.ErrorMessage
import br.dev.schirmer.ddd.kernel.web.rest.Response

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
