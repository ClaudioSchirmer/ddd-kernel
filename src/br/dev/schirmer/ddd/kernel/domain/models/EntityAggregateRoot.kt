package br.dev.schirmer.ddd.kernel.domain.models

import br.dev.schirmer.ddd.kernel.domain.notifications.NotificationMessage
import br.dev.schirmer.ddd.kernel.domain.valueobjects.AggregateValueObject
import br.dev.schirmer.ddd.kernel.domain.valueobjects.AggregateItemStatus

@Suppress("UNCHECKED_CAST", "TYPE_INFERENCE_ONLY_INPUT_TYPES_WARNING")
abstract class EntityAggregateRoot<TEntity : Entity<TEntity, TService, TInsertable, TUpdatable>, TService : Service<TEntity>, TInsertable : ValidEntity<TEntity>, TUpdatable : ValidEntity<TEntity>>
    : Entity<TEntity, TService, TInsertable, TUpdatable>() {

    /** Aggregate Collections */
    protected val aggregateItems =
        mutableMapOf<String, MutableList<AggregateItem<TEntity, TService, AggregateValueObject<TEntity, TService>>>>()

    protected inline fun <reified TAggregateEntityValueObject> getAggregateItems(): List<AggregateItem<TEntity, TService, TAggregateEntityValueObject>>
            where TAggregateEntityValueObject : AggregateValueObject<TEntity, TService> {
        val itemKey = TAggregateEntityValueObject::class.simpleName.toString()
        return (aggregateItems.putIfAbsent(itemKey, mutableListOf())
            ?: aggregateItems[itemKey]!!) as List<AggregateItem<TEntity, TService, TAggregateEntityValueObject>>
    }

    protected inline fun <reified TAggregateEntityValueObject> aggregateConstructor(items: List<TAggregateEntityValueObject>?)
            where TAggregateEntityValueObject : AggregateValueObject<TEntity, TService> {
        items?.forEach { item ->
            if (isAggregateItemValid(item)) {
                val itemKey = TAggregateEntityValueObject::class.simpleName.toString()
                val list = aggregateItems.putIfAbsent(itemKey, mutableListOf()) ?: aggregateItems[itemKey]!!
                list.add(
                    AggregateItem(
                        item = item,
                        originalStatus = AggregateItemStatus.CONSTRUCTOR,
                        currentStatus = AggregateItemStatus.CONSTRUCTOR
                    )
                )
            }
        }
    }

    protected suspend inline fun <reified TAggregateEntityValueObject> addAggregateItem(item: TAggregateEntityValueObject?)
            where TAggregateEntityValueObject : AggregateValueObject<TEntity, TService> {
        if (isAggregateItemValid(item)) {
            val itemKey = TAggregateEntityValueObject::class.simpleName.toString()
            val list = aggregateItems.putIfAbsent(itemKey, mutableListOf()) ?: aggregateItems[itemKey]!!
            when {
                list.any { it.item == item && (it.currentStatus == AggregateItemStatus.ADDED || it.currentStatus == AggregateItemStatus.CONSTRUCTOR) } -> {
                    addNotificationMessage(
                        NotificationMessage(
                            fieldValue = item.toString(),
                            fieldName = TAggregateEntityValueObject::class.simpleName.toString(),
                            notification = EntityAlreadyAddedNotification()
                        )
                    )
                }

                list.any { it.item == item && (it.currentStatus == AggregateItemStatus.REMOVED || it.currentStatus == AggregateItemStatus.CHANGED) } -> {
                    list.first { it.item == item && (it.currentStatus == AggregateItemStatus.REMOVED || it.currentStatus == AggregateItemStatus.CHANGED) }
                        .apply {
                            currentStatus = AggregateItemStatus.ADDED
                        }
                }

                else -> {
                    list.add(
                        AggregateItem(
                            item = item!!,
                            originalStatus = AggregateItemStatus.ADDED,
                            currentStatus = AggregateItemStatus.ADDED
                        )
                    )
                }
            }
        }
    }

    protected suspend inline fun <reified TAggregateEntityValueObject> changeAggregateItem(
        item: TAggregateEntityValueObject?,
        changes: TAggregateEntityValueObject.() -> Unit
    ) where TAggregateEntityValueObject : AggregateValueObject<TEntity, TService> {
        if (isAggregateItemValid(item)) {
            val itemKey = TAggregateEntityValueObject::class.simpleName.toString()
            val list = aggregateItems.putIfAbsent(itemKey, mutableListOf()) ?: aggregateItems[itemKey]!!
            (list.firstOrNull { it.item == item })?.run {
                list.removeIf { it.item == this.item }
                list.add(
                    AggregateItem(
                        item = (this.item as TAggregateEntityValueObject).apply(changes),
                        originalStatus = originalStatus,
                        currentStatus = AggregateItemStatus.CHANGED
                    )
                )
            } ?: addNotificationMessage(
                NotificationMessage(
                    fieldValue = "null",
                    fieldName = TAggregateEntityValueObject::class.simpleName.toString(),
                    notification = EntityDoesNotExistNotification()
                )
            )
        }
    }

    protected suspend inline fun <reified TAggregateEntityValueObject> removeAggregateItem(item: TAggregateEntityValueObject?)
            where TAggregateEntityValueObject : AggregateValueObject<TEntity, TService> {
        if (item == null) {
            addNotificationMessage(
                NotificationMessage(
                    fieldValue = "null",
                    fieldName = TAggregateEntityValueObject::class.simpleName.toString(),
                    notification = EntityDoesNotExistNotification()
                )
            )
        } else {
            val itemKey = TAggregateEntityValueObject::class.simpleName.toString()
            val list = aggregateItems.putIfAbsent(itemKey, mutableListOf()) ?: aggregateItems[itemKey]!!
            list.firstOrNull { it.item == item && it.currentStatus != AggregateItemStatus.REMOVED }?.apply {
                currentStatus = AggregateItemStatus.REMOVED
            } ?: addNotificationMessage(
                NotificationMessage(
                    fieldValue = item.toString(),
                    fieldName = TAggregateEntityValueObject::class.simpleName.toString(),
                    notification = EntityDoesNotExistNotification()
                )
            )
        }
    }

    protected suspend inline fun <reified TAggregateEntityValueObject> clearAggregateItems()
            where TAggregateEntityValueObject : AggregateValueObject<TEntity, TService> {
        val itemKey = TAggregateEntityValueObject::class.simpleName.toString()
        (aggregateItems.putIfAbsent(itemKey, mutableListOf()) ?: aggregateItems[itemKey]!!).forEach {
            it.currentStatus = AggregateItemStatus.REMOVED
        }
    }

    protected inline fun <reified TAggregateEntityValueObject> isAggregateItemValid(item: TAggregateEntityValueObject?): Boolean
            where TAggregateEntityValueObject : AggregateValueObject<TEntity, TService> = if (item == null) {
        addNotificationMessage(
            NotificationMessage(
                fieldValue = "null",
                fieldName = TAggregateEntityValueObject::class.simpleName.toString(),
                notification = EntityDoesNotExistNotification()
            )
        )
        false
    } else {
        true
    }
}