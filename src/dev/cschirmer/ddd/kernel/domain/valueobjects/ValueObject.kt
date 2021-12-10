package dev.cschirmer.ddd.kernel.domain.valueobjects

import dev.cschirmer.ddd.kernel.domain.notifications.NotificationContext

interface ValueObject {
    fun isValid(fieldName: String? = null, notificationContext: NotificationContext? = null) : Boolean
}