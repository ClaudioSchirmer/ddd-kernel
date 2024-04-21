package br.dev.schirmer.ddd.kernel.domain.valueobjects

import br.dev.schirmer.ddd.kernel.domain.notifications.Notification


enum class AggregateItemStatus(override val value: Int) : EnumValueObject<Int> {
    UNKNOWN(0),
    CONSTRUCTOR(1),
    ADDED(2),
    CHANGED(3),
    REMOVED(4);

    override val unknownEnumNotification: Notification by lazy { InvalidAggregateItemStatusNotification() }
}