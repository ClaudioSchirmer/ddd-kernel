package dev.cschirmer.ddd.kernel.domain.valueobjects

import dev.cschirmer.ddd.kernel.domain.notifications.NotificationContext

interface ValueObject {
    suspend fun isValid(fieldName: String? = null, notificationContext: NotificationContext? = null) : Boolean
}