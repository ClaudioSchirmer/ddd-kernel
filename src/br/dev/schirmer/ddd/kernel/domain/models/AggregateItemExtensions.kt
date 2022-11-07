package br.dev.schirmer.ddd.kernel.domain.models

import br.dev.schirmer.ddd.kernel.domain.valueobjects.AggregateValueObject
import br.dev.schirmer.ddd.kernel.domain.valueobjects.AggregateItemStatus

inline fun <reified TEntity : Entity<TEntity, *, *, *>, TAggregateEntityValueObject : AggregateValueObject<TEntity, *>> List<AggregateItem<TEntity, *, TAggregateEntityValueObject>>.getCurrentItems(): List<TAggregateEntityValueObject> =
    this.filter { it.currentStatus != AggregateItemStatus.REMOVED }.map { it.item }

inline fun <reified TEntity : Entity<TEntity, *, *, *>, TAggregateEntityValueObject : AggregateValueObject<TEntity, *>> List<AggregateItem<TEntity, *, TAggregateEntityValueObject>>.forEachCurrentItems(
    execute: (TAggregateEntityValueObject) -> Unit
) {
    getCurrentItems().forEach(execute)
}

inline fun <reified TEntity : Entity<TEntity, *, *, *>, TAggregateEntityValueObject : AggregateValueObject<TEntity, *>> List<AggregateItem<TEntity, *, TAggregateEntityValueObject>>.getAddedItems() =
    this.filter { it.originalStatus != AggregateItemStatus.CONSTRUCTOR && it.currentStatus != AggregateItemStatus.REMOVED }
        .map { it.item }

inline fun <reified TEntity : Entity<TEntity, *, *, *>, TAggregateEntityValueObject : AggregateValueObject<TEntity, *>> List<AggregateItem<TEntity, *, TAggregateEntityValueObject>>.forEachAddedItems(
    execute: (TAggregateEntityValueObject) -> Unit
) {
    getAddedItems().forEach(execute)
}

inline fun <reified TEntity : Entity<TEntity, *, *, *>, TAggregateEntityValueObject : AggregateValueObject<TEntity, *>> List<AggregateItem<TEntity, *, TAggregateEntityValueObject>>.getChangedItems() =
    this.filter { it.originalStatus == AggregateItemStatus.CONSTRUCTOR && (it.currentStatus == AggregateItemStatus.ADDED || it.currentStatus == AggregateItemStatus.CHANGED) }
        .map { it.item }

inline fun <reified TEntity : Entity<TEntity, *, *, *>, TAggregateEntityValueObject : AggregateValueObject<TEntity, *>> List<AggregateItem<TEntity, *, TAggregateEntityValueObject>>.forEachChangedItems(
    execute: (TAggregateEntityValueObject) -> Unit
) {
    getChangedItems().forEach(execute)
}

inline fun <reified TEntity : Entity<TEntity, *, *, *>, TAggregateEntityValueObject : AggregateValueObject<TEntity, *>> List<AggregateItem<TEntity, *, TAggregateEntityValueObject>>.getRemovedItems() =
    this.filter { it.originalStatus == AggregateItemStatus.CONSTRUCTOR && it.currentStatus == AggregateItemStatus.REMOVED }
        .map { it.item }

inline fun <reified TEntity : Entity<TEntity, *, *, *>, TAggregateEntityValueObject : AggregateValueObject<TEntity, *>> List<AggregateItem<TEntity, *, TAggregateEntityValueObject>>.forEachRemovedItems(
    execute: (TAggregateEntityValueObject) -> Unit
) {
    getRemovedItems().forEach(execute)
}