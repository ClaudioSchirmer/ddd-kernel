package br.dev.schirmer.ddd.kernel.domain.models

import br.dev.schirmer.ddd.kernel.domain.exception.DomainNotificationContextException
import br.dev.schirmer.ddd.kernel.domain.notifications.NotificationContext
import br.dev.schirmer.ddd.kernel.domain.notifications.NotificationMessage
import br.dev.schirmer.ddd.kernel.domain.valueobjects.AggregateEntityValueObject
import br.dev.schirmer.ddd.kernel.domain.valueobjects.Id
import br.dev.schirmer.ddd.kernel.domain.valueobjects.TransactionMode
import br.dev.schirmer.ddd.kernel.domain.valueobjects.ValueObject

@Suppress("UNCHECKED_CAST")
abstract class Entity<TEntity: Entity<TEntity, TService, TInsertable, TUpdatable>, TService : Service<TEntity>, TInsertable : ValidEntity<TEntity>, TUpdatable : ValidEntity<TEntity>> :
    ValidEntity<TEntity> {
    var id: Id? = null
        protected set
    private val notificationContextCollection: MutableList<NotificationContext> = mutableListOf()
    protected val notificationContext = NotificationContext(this::class.simpleName.toString())
    protected var transactionMode: TransactionMode = TransactionMode.DISPLAY
    private var validateValueObjects: MutableList<Pair<String, ValueObject>> =  mutableListOf()
    private var validateAggregateEntityValueObjects: MutableList<Pair<String, AggregateEntityValueObject<TEntity, TService>>> =
        mutableListOf()
    private var businessRules: ((service: TService?) -> Unit) = {}
    private var insertOrUpdateRules: ((service: TService?) -> Unit) = {}
    private var updateRules: ((service: TService?) -> Unit) = {}
    private var insertRules: ((service: TService?) -> Unit) = {}
    private var deleteRules: ((service: TService?) -> Unit) = {}
    private var service: Service<TEntity>? = null
    open val insertable: Boolean = false
    open val updatable: Boolean = false
    open val deletable: Boolean = false
    open val insertableValidEntity: ValidEntity.Insertable<TEntity, TInsertable> = ValidEntity.Insertable(this as TInsertable)
    open val updatableValidEntity: ValidEntity.Updatable<TEntity, TUpdatable> = ValidEntity.Updatable(this as TUpdatable)

    suspend fun isValid(
        transactionMode: TransactionMode,
        service: Service<TEntity>? = null,
        _notificationContext: NotificationContext
    ): Boolean {
        startEntity()
        if (service != null) {
            this.service = service
        }
        when (transactionMode) {
            TransactionMode.INSERT -> {
                validateToInsert()
            }
            TransactionMode.UPDATE -> {
                validateToUpdate()
            }
            TransactionMode.DELETE -> {
                validateToDelete()
            }
            else -> {}
        }
        notificationContext.notifications.forEach {
            _notificationContext.addNotification(it)
        }
        startEntity()
        return _notificationContext.notifications.isNotEmpty()
    }

    suspend fun getInsertable(service: Service<TEntity>? = null): ValidEntity.Insertable<TEntity, TInsertable> {
        startEntity()
        this.service = service
        validateToInsert()
        checkNotifications()
        return insertableValidEntity
    }

    suspend fun getUpdatable(service: Service<TEntity>? = null): ValidEntity.Updatable<TEntity, TUpdatable> {
        startEntity()
        this.service = service
        validateToUpdate()
        checkNotifications()
        return updatableValidEntity
    }

    suspend fun getDeletable(service: Service<TEntity>? = null): ValidEntity.Deletable<TEntity> {
        startEntity()
        this.service = service
        validateToDelete()
        checkNotifications()
        return ValidEntity.Deletable(id!!)
    }

    protected fun ValueObject.addToValidate(name: String) = validateValueObjects.add(Pair(name, this))
    protected fun AggregateEntityValueObject<TEntity, TService>.addToValidate(name: String) =
        validateAggregateEntityValueObjects.add(Pair(name, this))

    protected fun insertRules(function: (service: TService?) -> Unit) {
        insertRules = function
    }

    protected fun updateRules(function: (service: TService?) -> Unit) {
        updateRules = function
    }

    protected fun insertOrUpdateRules(function: (service: TService?) -> Unit) {
        insertOrUpdateRules = function
    }

    protected fun deleteRules(function: (service: TService?) -> Unit) {
        deleteRules = function
    }

    protected fun businessRules(function: (service: TService?) -> Unit) {
        businessRules = function
    }

    protected fun addNotificationMessage(notificationMessage: NotificationMessage) {
        notificationContext.addNotification(notificationMessage)
    }

    protected fun addNotificationContext(notificationContext: NotificationContext) {
        notificationContextCollection.add(notificationContext)
    }

    protected fun getService() = if( service == null ) null else service as TService

    private suspend fun validateToInsert() {
        if (!insertable) {
            addNotificationMessage(
                NotificationMessage(
                    fieldName = ::insertable.name,
                    fieldValue = insertable.toString(),
                    funName = ::getInsertable.name,
                    notification = InsertNotAllowedNotification()
                )
            )
        }
        transactionMode = TransactionMode.INSERT
        insertRules.runWithService()
        insertOrUpdateRules.runWithService()
        businessRules.runWithService()
        runValidateValueObjects()
        runValidateAggregateEntityValueObjects()
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
    }

    private suspend fun validateToUpdate() {
        if (!updatable) {
            addNotificationMessage(
                NotificationMessage(
                    fieldName = ::updatable.name,
                    fieldValue = updatable.toString(),
                    funName = ::getUpdatable.name,
                    notification = UpdateNotAllowedNotification()
                )
            )
        }
        transactionMode = TransactionMode.UPDATE
        updateRules.runWithService()
        insertOrUpdateRules.runWithService()
        businessRules.runWithService()
        runValidateValueObjects()
        runValidateAggregateEntityValueObjects()
        id?.isValid(::id.name, notificationContext)
            ?: addNotificationMessage(
                NotificationMessage(
                    fieldName = ::id.name,
                    funName = ::getUpdatable.name,
                    notification = UnableToUpdateWithoutIDNotification()
                )
            )
    }

    private suspend fun validateToDelete() {
        if (!deletable) {
            addNotificationMessage(
                NotificationMessage(
                    fieldName = ::deletable.name,
                    fieldValue = deletable.toString(),
                    funName = ::getDeletable.name,
                    notification = DeleteNotAllowedNotification()
                )
            )
        }

        transactionMode = TransactionMode.DELETE
        deleteRules.runWithService()
        businessRules.runWithService()
        runValidateValueObjects()
        runValidateAggregateEntityValueObjects()
        id?.isValid(::id.name, notificationContext)
            ?: addNotificationMessage(
                NotificationMessage(
                    fieldName = ::id.name,
                    funName = ::getDeletable.name,
                    notification = UnableToDeleteWithoutIDNotification()
                )
            )
    }

    private fun checkNotifications() {
        if (notificationContext.notifications.isNotEmpty()) {
            notificationContextCollection.add(notificationContext)
        }
        if (notificationContextCollection.any { it.notifications.isNotEmpty() }) {
            throw DomainNotificationContextException(notificationContextCollection)
        }
    }

    private fun (TService?.() -> Unit).runWithService() = this(getService())

    private suspend fun runValidateValueObjects() {
        validateValueObjects.forEach {
            it.second.isValid(it.first, notificationContext)
        }
    }

    private suspend fun runValidateAggregateEntityValueObjects() {
        validateAggregateEntityValueObjects.forEach {
            it.second.isValid(getService(), transactionMode, it.first, notificationContext)
        }
    }

    private fun startEntity() {
        validateValueObjects.clear()
        validateAggregateEntityValueObjects.clear()
        notificationContext.clearNotifications()
        transactionMode = TransactionMode.DISPLAY
        service = null
    }
}