package br.dev.schirmer.ddd.kernel.application.translation

import br.dev.schirmer.ddd.kernel.application.notifications.NotificationContextDTO
import br.dev.schirmer.ddd.kernel.application.notifications.NotificationMessageDTO
import br.dev.schirmer.ddd.kernel.domain.notifications.NotificationContext


fun String.getTranslatedMessage(): String = Translator.getTranslationByKey(this)

fun List<NotificationContext>.toNotificationContextDTO() = map { notificationContext ->
    NotificationContextDTO(
        context = notificationContext.context.getTranslatedMessage(),
        notifications = notificationContext.notifications.map { notificationMessage ->
            NotificationMessageDTO(
                fieldName = notificationMessage.fieldName,
                fieldValue = notificationMessage.fieldValue,
                funName = notificationMessage.funName,
                message = Translator.getTranslationByKey(notificationMessage.notification::class.simpleName.toString())
            )
        }
    )
}