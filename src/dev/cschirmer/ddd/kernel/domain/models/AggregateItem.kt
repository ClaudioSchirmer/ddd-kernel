package dev.cschirmer.ddd.kernel.domain.models

import dev.cschirmer.ddd.kernel.domain.valueobjects.AggregateEntityValueObject
import dev.cschirmer.ddd.kernel.domain.valueobjects.AggregateItemStatus

data class AggregateItem<TAggregateEntityValueObject : AggregateEntityValueObject<out Entity<*>>>(
    var item: TAggregateEntityValueObject,
    val originalStatus: AggregateItemStatus,
    var currentStatus: AggregateItemStatus
)