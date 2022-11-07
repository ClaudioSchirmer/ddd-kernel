package br.dev.schirmer.ddd.kernel.domain.valueobjects

import br.dev.schirmer.ddd.kernel.domain.models.Entity
import br.dev.schirmer.ddd.kernel.domain.models.Service
import br.dev.schirmer.ddd.kernel.domain.notifications.NotificationContext

interface EntityAggregateValueObject<TSuperEntity : Entity<TSuperEntity,TSuperService,*,*>, TSuperService: Service<TSuperEntity>> {
    suspend fun isValid(
        service: TSuperService? = null,
        entityMode: EntityMode,
        fieldName: String,
        notificationContext: NotificationContext
    ): Boolean
}