package br.dev.schirmer.ddd.kernel.domain.models

import br.dev.schirmer.ddd.kernel.domain.exception.DomainNotificationContextException
import br.dev.schirmer.ddd.kernel.domain.notifications.NotificationContext
import br.dev.schirmer.ddd.kernel.domain.notifications.NotificationMessage
import br.dev.schirmer.ddd.kernel.domain.valueobjects.Id
import br.dev.schirmer.ddd.kernel.domain.valueobjects.TransactionMode
import br.dev.schirmer.ddd.kernel.domain.valueobjects.ValueObject

@Suppress("UNCHECKED_CAST")
abstract class Entity<TEntity : Entity<TEntity>> {
    var id: Id? = null
        protected set
    private val notificationContextCollection: MutableList<NotificationContext> = mutableListOf()
    protected val notificationContext = NotificationContext(this::class.simpleName.toString())
    protected var service: Service<TEntity>? = null
    protected var transactionMode: TransactionMode = TransactionMode.DISPLAY
    protected var validateValueObjects: (() -> MutableList<Pair<String, ValueObject>>) = { mutableListOf() }
    protected var businessRules: (() -> Unit) = {}
    protected var insertOrUpdateRules: (() -> Unit) = {}
    protected var updateRules: (() -> Unit) = {}
    protected var insertRules: (() -> Unit) = {}
    protected var deleteRules: (() -> Unit) = {}

    suspend fun isValid(
        transactionMode: TransactionMode,
        service: Service<TEntity>? = null,
        _notificationContext: NotificationContext
    ): Boolean {
        if (service != null) {
            this.service = service
        }
        when (transactionMode) {
            TransactionMode.INSERT -> {
                insertOrUpdateRules()
                insertRules()
            }
            TransactionMode.UPDATE -> {
                insertOrUpdateRules()
                updateRules()
            }
            TransactionMode.DELETE -> {
                deleteRules()
            }
            else -> {}
        }
        businessRules()
        runValidateValueObjects()
        notificationContext.notifications.forEach {
            _notificationContext.addNotification(it)
        }
        return if (notificationContext.notifications.isNotEmpty()) {
            notificationContext.clearNotifications()
            false
        } else {
            true
        }
    }

    protected open suspend fun getInsertable(service: Service<TEntity>? = null): ValidEntity.Insertable<TEntity> {
        this.service = service
        transactionMode = TransactionMode.INSERT
        insertOrUpdateRules()
        insertRules()
        businessRules()
        runValidateValueObjects()
        if (id != null) {
            addNotificationMessage(
                NotificationMessage(
                    fieldName = ::id.name,
                    fieldValue = id?.value,
                    funName = ::getInsertable.name,
                    notification = UnableToInsertWithIDNotification()
                )
            )
        }
        checkNotifications()
        return ValidEntity.Insertable(this as TEntity)
    }

    protected open suspend fun getUpdatable(service: Service<TEntity>? = null): ValidEntity.Updatable<TEntity> {
        this.service = service
        transactionMode = TransactionMode.UPDATE
        insertOrUpdateRules()
        updateRules()
        businessRules()
        runValidateValueObjects()
        id?.isValid(::id.name, notificationContext)
            ?: addNotificationMessage(
                NotificationMessage(
                    funName = ::getUpdatable.name,
                    notification = UnableToUpdateWithoutIDNotification()
                )
            )
        checkNotifications()
        return ValidEntity.Updatable(this as TEntity)
    }

    protected open suspend fun getDeletable(service: Service<TEntity>? = null): ValidEntity.Deletable<TEntity> {
        this.service = service
        transactionMode = TransactionMode.DELETE
        deleteRules()
        businessRules()
        runValidateValueObjects()
        id?.isValid(::id.name, notificationContext)
            ?: addNotificationMessage(
                NotificationMessage(
                    funName = ::getDeletable.name,
                    notification = UnableToDeleteWithoutIDNotification()
                )
            )
        checkNotifications()
        return ValidEntity.Deletable(this as TEntity)
    }

    protected fun addNotificationMessage(notificationMessage: NotificationMessage) {
        notificationContext.addNotification(notificationMessage)
    }

    protected fun addNotificationContext(notificationContext: NotificationContext) {
        notificationContextCollection.add(notificationContext)
    }

    private fun checkNotifications() {
        if (notificationContext.notifications.isNotEmpty()) {
            notificationContextCollection.add(notificationContext)
        }
        if (notificationContextCollection.any { it.notifications.isNotEmpty() }) {
            throw DomainNotificationContextException(notificationContextCollection)
        }
    }

    private suspend fun runValidateValueObjects() {
        validateValueObjects().forEach {
            it.second.isValid(it.first, notificationContext)
        }
    }
}