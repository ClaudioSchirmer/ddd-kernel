package br.dev.schirmer.ddd.kernel.domain.valueobjects

import br.dev.schirmer.ddd.kernel.domain.notifications.NotificationContext

interface ValueObject<TValue : Any> {
    val value: TValue
    suspend fun isValid(fieldName: String? = null, notificationContext: NotificationContext? = null) : Boolean
}