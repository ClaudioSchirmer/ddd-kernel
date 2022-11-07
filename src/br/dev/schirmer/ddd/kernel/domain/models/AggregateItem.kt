package br.dev.schirmer.ddd.kernel.domain.models

import br.dev.schirmer.ddd.kernel.domain.valueobjects.EntityAggregateValueObject
import br.dev.schirmer.ddd.kernel.domain.valueobjects.AggregateItemStatus

data class AggregateItem<TSuperEntity: Entity<TSuperEntity, TSuperService,*,*>, TSuperService: Service<TSuperEntity>,
        TAggregateEntityValueObject : EntityAggregateValueObject<TSuperEntity, TSuperService>>(
    var item: TAggregateEntityValueObject,
    val originalStatus: AggregateItemStatus,
    var currentStatus: AggregateItemStatus
)