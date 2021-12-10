package dev.cschirmer.ddd.kernel.domain.models

import dev.cschirmer.ddd.kernel.domain.valueobjects.AggregateEntityValueObject
import dev.cschirmer.ddd.kernel.domain.valueobjects.AggregateItemStatus

inline fun <reified TEntity : Entity<TEntity>, TAggregateEntityValueObject : AggregateEntityValueObject<TEntity>> List<AggregateItem<TAggregateEntityValueObject>>.getCurrentItems(): List<TAggregateEntityValueObject> =
    this.filter { it.currentStatus != AggregateItemStatus.REMOVED }.map { it.item }

inline fun <reified TEntity : Entity<TEntity>, TAggregateEntityValueObject : AggregateEntityValueObject<TEntity>> List<AggregateItem<TAggregateEntityValueObject>>.forEachCurrentItems(
    execute: (TAggregateEntityValueObject) -> Unit
) {
    getCurrentItems().forEach(execute)
}

inline fun <reified TEntity : Entity<TEntity>, TAggregateEntityValueObject : AggregateEntityValueObject<TEntity>> List<AggregateItem<TAggregateEntityValueObject>>.getAddedItems() =
    this.filter { it.originalStatus != AggregateItemStatus.CONSTRUCTOR && it.currentStatus != AggregateItemStatus.REMOVED }
        .map { it.item }

inline fun <reified TEntity : Entity<TEntity>, TAggregateEntityValueObject : AggregateEntityValueObject<TEntity>> List<AggregateItem<TAggregateEntityValueObject>>.forEachAddedItems(
    execute: (TAggregateEntityValueObject) -> Unit
) {
    getAddedItems().forEach(execute)
}

inline fun <reified TEntity : Entity<TEntity>, TAggregateEntityValueObject : AggregateEntityValueObject<TEntity>> List<AggregateItem<TAggregateEntityValueObject>>.getChangedItems() =
    this.filter { it.originalStatus == AggregateItemStatus.CONSTRUCTOR && (it.currentStatus == AggregateItemStatus.ADDED || it.currentStatus == AggregateItemStatus.CHANGED) }
        .map { it.item }

inline fun <reified TEntity : Entity<TEntity>, TAggregateEntityValueObject : AggregateEntityValueObject<TEntity>> List<AggregateItem<TAggregateEntityValueObject>>.forEachChangedItems(
    execute: (TAggregateEntityValueObject) -> Unit
) {
    getChangedItems().forEach(execute)
}

inline fun <reified TEntity : Entity<TEntity>, TAggregateEntityValueObject : AggregateEntityValueObject<TEntity>> List<AggregateItem<TAggregateEntityValueObject>>.getRemovedItems() =
    this.filter { it.originalStatus == AggregateItemStatus.CONSTRUCTOR && it.currentStatus == AggregateItemStatus.REMOVED }
        .map { it.item }

inline fun <reified TEntity : Entity<TEntity>, TAggregateEntityValueObject : AggregateEntityValueObject<TEntity>> List<AggregateItem<TAggregateEntityValueObject>>.forEachRemovedItems(
    execute: (TAggregateEntityValueObject) -> Unit
) {
    getRemovedItems().forEach(execute)
}