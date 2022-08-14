package br.dev.schirmer.ddd.kernel.domain.valueobjects

import br.dev.schirmer.ddd.kernel.domain.models.Entity
import br.dev.schirmer.ddd.kernel.domain.models.Service
import br.dev.schirmer.ddd.kernel.domain.notifications.NotificationContext

interface AggregateEntityValueObject<TSuperEntity : Entity<TSuperEntity,TSuperService,*,*>, TSuperService: Service<TSuperEntity>> {
    suspend fun isValid(
        service: TSuperService? = null,
        transactionMode: TransactionMode,
        fieldName: String,
        notificationContext: NotificationContext
    ): Boolean
}