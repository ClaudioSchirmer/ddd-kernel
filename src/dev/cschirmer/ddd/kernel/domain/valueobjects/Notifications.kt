package dev.cschirmer.ddd.kernel.domain.valueobjects

import dev.cschirmer.ddd.kernel.domain.notifications.DomainNotification

/** TransactionMode*/
class InvalidTransactionModeNotification: DomainNotification

/** ID */
class InvalidIDUUIDNotification: DomainNotification

/** AggregateItemStatus */
class InvalidAggregateItemStatusNotification: DomainNotification

/** ActivatableEntityValueObject */
class EntityIsNotActiveNotification : DomainNotification