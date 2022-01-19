package dev.cschirmer.ddd.kernel.domain.valueobjects

import dev.cschirmer.ddd.kernel.domain.models.Entity
import dev.cschirmer.ddd.kernel.domain.models.Service
import dev.cschirmer.ddd.kernel.domain.notifications.NotificationContext

interface AggregateEntityValueObject<TSuperEntity : Entity<TSuperEntity>> {
    suspend fun isValid(
        service: Service<TSuperEntity>? = null,
        transactionMode: TransactionMode,
        fieldName: String,
        notificationContext: NotificationContext
    ): Boolean
}