package dev.cschirmer.ddd.kernel.application.translation

import dev.cschirmer.ddd.kernel.application.notifications.NotificationContextDTO
import dev.cschirmer.ddd.kernel.application.notifications.NotificationMessageDTO
import dev.cschirmer.ddd.kernel.domain.notifications.NotificationContext


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