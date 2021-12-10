package dev.cschirmer.ddd.kernel.domain.models

import dev.cschirmer.ddd.kernel.domain.notifications.NotificationMessage
import dev.cschirmer.ddd.kernel.domain.valueobjects.AggregateEntityValueObject
import dev.cschirmer.ddd.kernel.domain.valueobjects.AggregateItemStatus
import dev.cschirmer.ddd.kernel.domain.valueobjects.EntityIsNotActiveNotification

@Suppress("UNCHECKED_CAST", "TYPE_INFERENCE_ONLY_INPUT_TYPES_WARNING")
abstract class AggregateRoot<TEntity : Entity<TEntity>> : Entity<TEntity>() {

    /** Aggregate Collections */

    protected val aggregateItems =
        mutableMapOf<String, MutableList<AggregateItem<AggregateEntityValueObject<TEntity>>>>()

    protected inline fun <reified TAggregateEntityValueObject> getAggregateItems(): List<AggregateItem<TAggregateEntityValueObject>>
            where TAggregateEntityValueObject : AggregateEntityValueObject<TEntity> {
        val itemKey = TAggregateEntityValueObject::class.simpleName.toString()
        println("getting-$itemKey")
        return (aggregateItems.putIfAbsent(itemKey, mutableListOf())
            ?: aggregateItems[itemKey]!!) as List<AggregateItem<TAggregateEntityValueObject>>
    }

    protected inline fun <reified TAggregateEntityValueObject> aggregateConstructor(items: List<TAggregateEntityValueObject>?)
            where TAggregateEntityValueObject : AggregateEntityValueObject<TEntity> {
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

    protected inline fun <reified TAggregateEntityValueObject> addAggregateItem(item: TAggregateEntityValueObject?)
            where TAggregateEntityValueObject : AggregateEntityValueObject<TEntity> {
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

    protected inline fun <reified TAggregateEntityValueObject> changeAggregateItem(
        item: TAggregateEntityValueObject?,
        applyChanges: TAggregateEntityValueObject.() -> TAggregateEntityValueObject
    )
            where TAggregateEntityValueObject : AggregateEntityValueObject<TEntity> {
        if (isAggregateItemValid(item)) {
            val itemKey = TAggregateEntityValueObject::class.simpleName.toString()
            val list = aggregateItems.putIfAbsent(itemKey, mutableListOf()) ?: aggregateItems[itemKey]!!
            if (list.any { it.item == item }) {
                applyChanges(list.first { it.item == item }.item as TAggregateEntityValueObject).run AggregateItemValueObject@{
                    (list.first { it.item == this }).run AggregateItem@{
                        list.removeIf { it.item == this@AggregateItemValueObject }
                        list.add(
                            AggregateItem(
                                item = this@AggregateItemValueObject,
                                originalStatus = originalStatus,
                                currentStatus = AggregateItemStatus.CHANGED
                            )
                        )
                    }
                }
            } else {
                addNotificationMessage(
                    NotificationMessage(
                        fieldValue = "null",
                        fieldName = TAggregateEntityValueObject::class.simpleName.toString(),
                        notification = EntityDoesNotExistNotification()
                    )
                )
            }
        }
    }

    protected inline fun <reified TAggregateEntityValueObject> removeAggregateItem(item: TAggregateEntityValueObject?)
            where TAggregateEntityValueObject : AggregateEntityValueObject<TEntity> {
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
            if (list.any { it.item == item && it.currentStatus != AggregateItemStatus.REMOVED }) {
                list.first { it.item == item && it.currentStatus != AggregateItemStatus.REMOVED }.apply {
                    currentStatus = AggregateItemStatus.REMOVED
                }
            } else {
                addNotificationMessage(
                    NotificationMessage(
                        fieldValue = item.toString(),
                        fieldName = TAggregateEntityValueObject::class.simpleName.toString(),
                        notification = EntityDoesNotExistNotification()
                    )
                )
            }
        }
    }

    protected inline fun <reified TAggregateEntityValueObject> clearAggregateItems() where TAggregateEntityValueObject : AggregateEntityValueObject<TEntity> {
        val itemKey = TAggregateEntityValueObject::class.simpleName.toString()
        (aggregateItems.putIfAbsent(itemKey, mutableListOf()) ?: aggregateItems[itemKey]!!).forEach {
            it.currentStatus = AggregateItemStatus.REMOVED
        }
    }

    protected inline fun <reified TAggregateEntityValueObject> isAggregateItemValid(item: TAggregateEntityValueObject?): Boolean where TAggregateEntityValueObject : AggregateEntityValueObject<TEntity> =
        when {
            item == null -> {
                addNotificationMessage(
                    NotificationMessage(
                        fieldValue = "null",
                        fieldName = TAggregateEntityValueObject::class.simpleName.toString(),
                        notification = EntityDoesNotExistNotification()
                    )
                )
                false
            }
            (item is Activatable) && !item.active -> {
                addNotificationMessage(
                    NotificationMessage(
                        fieldValue = item.active.toString(),
                        fieldName = "${TAggregateEntityValueObject::class.simpleName.toString()}.active",
                        notification = EntityIsNotActiveNotification()
                    )
                )
                false
            }
            item.isValid(
                service,
                transactionMode,
                TAggregateEntityValueObject::class.simpleName.toString(),
                notificationContext
            ) -> {
                true
            }
            else -> false
        }

    /** Aggregate References */

    protected var validateAggregateEntityValueObjects: (() -> MutableList<Pair<String, AggregateEntityValueObject<TEntity>>>) =
        { mutableListOf() }

    override fun getUpdatable(service: Service<TEntity>?): ValidEntity.Updatable<TEntity> {
        this.service = service
        runValidateAggregateEntityValueObjects()
        return super.getUpdatable(service)
    }

    override fun getDeletable(service: Service<TEntity>?): ValidEntity.Deletable<TEntity> {
        this.service = service
        runValidateAggregateEntityValueObjects()
        return super.getDeletable(service)
    }

    override fun getInsertable(service: Service<TEntity>?): ValidEntity.Insertable<TEntity> {
        this.service = service
        runValidateAggregateEntityValueObjects()
        return super.getInsertable(service)
    }

    private fun runValidateAggregateEntityValueObjects() {
        validateAggregateEntityValueObjects().forEach {
            it.second.isValid(service, transactionMode, it.first, notificationContext)
        }
    }
}
