package br.dev.schirmer.ddd.kernel.domain.models

import br.dev.schirmer.ddd.kernel.domain.valueobjects.AggregateItemStatus
import br.dev.schirmer.ddd.kernel.domain.valueobjects.AggregateValueObject

data class AggregateItem<TSuperEntity: Entity<TSuperEntity, TSuperService, *, *>, TSuperService: Service<TSuperEntity>,
        TAggregateEntityValueObject : AggregateValueObject<TSuperEntity, TSuperService>>(
    var item: TAggregateEntityValueObject,
    val originalStatus: AggregateItemStatus,
    var currentStatus: AggregateItemStatus
)