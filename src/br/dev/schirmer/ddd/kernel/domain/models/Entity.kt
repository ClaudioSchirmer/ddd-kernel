package br.dev.schirmer.ddd.kernel.domain.models

import br.dev.schirmer.ddd.kernel.domain.events.DomainEvent
import br.dev.schirmer.ddd.kernel.domain.exception.DomainNotificationContextException
import br.dev.schirmer.ddd.kernel.domain.notifications.NotificationContext
import br.dev.schirmer.ddd.kernel.domain.notifications.NotificationMessage
import br.dev.schirmer.ddd.kernel.domain.valueobjects.AggregateValueObject
import br.dev.schirmer.ddd.kernel.domain.valueobjects.EntityMode
import br.dev.schirmer.ddd.kernel.domain.valueobjects.Id
import br.dev.schirmer.ddd.kernel.domain.valueobjects.ValueObject
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.reflect.full.findAnnotation
import br.dev.schirmer.ddd.kernel.domain.models.ValidEntity as SealedValidEntity

@Suppress("UNCHECKED_CAST")
abstract class Entity<TEntity : Entity<TEntity, TService, TRepository, TInsertable, TUpdatable>,
        TService : Service<TEntity>,
        TRepository : Repository<TEntity>,
        TInsertable : SealedValidEntity<TEntity>,
        TUpdatable : SealedValidEntity<TEntity>>
    : SealedValidEntity<TEntity> {

    private var insertable: Boolean = false
    private var updatable: Boolean = false
    private var deletable: Boolean = false
    private var serviceRequired: Boolean = false
    private var entityMode: EntityMode = EntityMode.DISPLAY
    private var validateValueObjects: MutableList<Pair<String, ValueObject>> = mutableListOf()
    private var validateAggregateValueObjects: MutableList<Pair<String, AggregateValueObject<TEntity, TService>>> =
        mutableListOf()
    private var service: TService? = null
    private val events: MutableList<DomainEvent> = mutableListOf()
    private val notificationContextCollection: MutableList<NotificationContext> = mutableListOf()
    private val fieldNamesToChange: MutableMap<String, String> = mutableMapOf()
    protected var entityState: String? = null
        private set

    @JsonIgnore
    var id: Id? = null
        protected set
    protected val notificationContext = NotificationContext(this::class.simpleName.toString())

    init {
        with(this::class) {
            findAnnotation<EntityModes>()?.modes?.forEach {
                when (it) {
                    EntityMode.INSERT -> insertable = true
                    EntityMode.UPDATE -> updatable = true
                    EntityMode.DELETE -> deletable = true
                    else -> {}
                }
            }
            serviceRequired = findAnnotation<EntityRequiresService>() != null
        }
    }

    fun addFieldNameToChange(originalFieldName: String, newFieldName: String) {
        fieldNamesToChange.put(originalFieldName, newFieldName)
    }

    fun addFieldNamesToChange(fieldNamesToChange: Map<String, String>) =
        this.fieldNamesToChange.putAll(fieldNamesToChange)

    suspend fun isValid(
        entityMode: EntityMode,
        service: TService? = null,
        notificationContext: NotificationContext
    ): Boolean {
        startEntity()
        if (service != null) {
            this.service = service
        }
        when (entityMode) {
            EntityMode.INSERT -> {
                validateBeforeInsert(buildRules("insert"), "insert")
            }

            EntityMode.UPDATE -> {
                validateBeforeUpdate(buildRules("update"), "update")
            }

            EntityMode.DELETE -> {
                validateBeforeDelete(buildRules("delete"), "delete")
            }

            else -> {}
        }
        this.notificationContext.notifications.forEach {
            notificationContext.addNotification(it)
        }
        startEntity()
        return notificationContext.notifications.isNotEmpty()
    }

    suspend fun insert(repository: TRepository, service: TService? = null) =
        insert(repository, "insert", service)

    suspend fun update(repository: TRepository, service: TService? = null) =
        update(repository, "update", service)

    suspend fun delete(repository: TRepository, service: TService? = null) =
        delete(repository, "delete", service)

    protected suspend fun insert(
        repository: TRepository,
        actionName: String,
        service: TService? = null,
        callAfterInsert: Boolean = true
    ) {
        val rules = buildRules(actionName)
        startEntity()
        this.service = service
        validateBeforeInsert(rules, actionName)
        checkNotifications()
        val insertable = SealedValidEntity.Insertable(
            this::class.simpleName!!,
            actionName,
            id,
            getValidEntityInsertable(),
            getDateTime(),
            events
        )
        repository as InsertableRepository<TEntity, TInsertable>
        id = repository.insert(insertable)
        if (callAfterInsert)
            afterInsert(actionName, repository, this.service)
        repository.publish(insertable)
    }

    protected suspend fun getInsertable(
        actionName: String,
        service: TService? = null
    ): SealedValidEntity.Insertable<TEntity, TInsertable> {
        val rules = buildRules(actionName)
        startEntity()
        this.service = service
        validateBeforeInsert(rules, actionName)
        checkNotifications()
        return SealedValidEntity.Insertable(
            this::class.simpleName!!,
            actionName,
            id,
            getValidEntityInsertable(),
            getDateTime(),
            events
        )
    }

    protected suspend fun update(
        repository: TRepository,
        actionName: String,
        service: TService? = null,
        callAfterUpdate: Boolean = true
    ) {
        val rules = buildRules(actionName)
        startEntity()
        this.service = service
        validateBeforeUpdate(rules, actionName)
        checkNotifications()
        val updatable = SealedValidEntity.Updatable(
            this::class.simpleName!!,
            actionName,
            id!!,
            getValidEntityUpdatable(),
            getDateTime(),
            events
        )
        repository as UpdatableRepository<TEntity, TUpdatable>
        repository.update(updatable)
        if (callAfterUpdate)
            afterUpdate(actionName, repository, this.service)
        repository.publish(updatable)
    }

    protected suspend fun getUpdatable(
        actionName: String,
        service: TService? = null
    ): SealedValidEntity.Updatable<TEntity, TUpdatable> {
        val rules = buildRules(actionName)
        startEntity()
        this.service = service
        validateBeforeUpdate(rules, actionName)
        checkNotifications()
        return SealedValidEntity.Updatable(
            this::class.simpleName!!,
            actionName,
            id!!,
            getValidEntityUpdatable(),
            getDateTime(),
            events
        )
    }

    protected suspend fun delete(
        repository: TRepository,
        actionName: String,
        service: TService? = null,
        callAfterDelete: Boolean = true
    ) {
        val rules = buildRules(actionName)
        startEntity()
        this.service = service
        validateBeforeDelete(rules, actionName)
        checkNotifications()
        val deletable = SealedValidEntity.Deletable<TEntity>(
            this::class.simpleName!!,
            actionName,
            id!!,
            this.writeAsString(),
            getDateTime(),
            events
        )
        repository as DeletableRepository<TEntity>
        repository.delete(deletable)
        if (callAfterDelete)
            afterDelete(actionName, repository, this.service)
        repository.publish(deletable)
    }

    protected suspend fun getDeletable(
        actionName: String,
        service: TService? = null
    ): SealedValidEntity.Deletable<TEntity> {
        val rules = buildRules(actionName)
        startEntity()
        this.service = service
        validateBeforeDelete(rules, actionName)
        checkNotifications()
        return SealedValidEntity.Deletable<TEntity>(
            this::class.simpleName!!,
            actionName,
            id!!,
            this.writeAsString(),
            getDateTime(),
            events
        )
    }

    protected open fun getValidEntityInsertable(): TInsertable = this as TInsertable
    protected open fun getValidEntityUpdatable(): TUpdatable = this as TUpdatable

    protected interface ValidEntity<TEntity : Entity<TEntity, *, *, *, *>> : SealedValidEntity<TEntity>

    protected fun ValueObject.addToValidate(name: String) = validateValueObjects.add(Pair(name, this))

    protected fun List<AggregateValueObject<TEntity, TService>>.addToValidate(name: String) =
        forEach { aggregateEntityValueObject ->
            validateAggregateValueObjects.add(Pair(name, aggregateEntityValueObject))
        }

    protected fun AggregateValueObject<TEntity, TService>.addToValidate(name: String) =
        validateAggregateValueObjects.add(Pair(name, this))

    protected fun addNotificationMessage(notificationMessage: NotificationMessage) {
        notificationContext.addNotification(notificationMessage)
    }

    protected fun addNotificationContext(notificationContext: NotificationContext) {
        notificationContextCollection.add(notificationContext)
    }

    protected fun DomainEvent.register() = events.add(this)
    protected fun registerEvent(domainEvent: DomainEvent) = events.add(domainEvent)

    protected abstract fun buildRules(actionName: String, service: TService?): Rules
    protected fun rules(rules: Rules.() -> Unit): Rules = Rules().apply(rules)

    protected fun saveState() {
        entityState = this.writeAsString()
    }

    protected open suspend fun afterUpdate(actionName: String, repository: TRepository, service: TService?) {}
    protected open suspend fun afterDelete(actionName: String, repository: TRepository, service: TService?) {}
    protected open suspend fun afterInsert(actionName: String, repository: TRepository, service: TService?) {}

    protected inline fun <reified Entity : TEntity> getLastStateSaved(addNotificationIfNull: Boolean = true): Entity? {
        val addNotificatonAndReturnNull = { isError: Boolean ->
            if (isError || addNotificationIfNull)
                addNotificationMessage(
                    NotificationMessage(
                        funName = "getLastStateSaved",
                        notification = UnableToRecoverLastState()
                    )
                )
            null
        }
        return if (entityState != null) {
            runCatching {
                jacksonObjectMapper().apply {
                    registerKotlinModule()
                    registerModule(JavaTimeModule())
                    setDateFormat(SimpleDateFormat("yyyy-MM-dd HH:mm a z"))
                }.readValue(entityState, Entity::class.java)
            }.getOrElse {
                addNotificatonAndReturnNull(true)
            }
        } else {
            addNotificatonAndReturnNull(false)
        }
    }

    protected fun checkNotifications() {
        if (notificationContext.notifications.isNotEmpty()) {
            notificationContextCollection.add(notificationContext)
        }
        if (notificationContextCollection.any { it.notifications.isNotEmpty() }) {
            changeFieldNames()
            throw DomainNotificationContextException(notificationContextCollection)
        }
    }

    protected class Rules {
        private var insertRules: suspend () -> Unit = { }
        private var updateRules: suspend () -> Unit = { }
        private var deleteRules: suspend () -> Unit = { }
        private var insertOrUpdateRules: suspend () -> Unit = { }
        private var commonsRules: suspend () -> Unit = { }
        suspend fun executeInsertRulesBeforeInsert() {
            commonsRules()
            insertOrUpdateRules()
            insertRules()
        }

        suspend fun executeUpdateRulesBeforeUpdate() {
            commonsRules()
            insertOrUpdateRules()
            updateRules()
        }

        suspend fun executeDeleteRulesBeforeDelete() {
            commonsRules()
            deleteRules()
        }

        companion object {
            fun Rules.ifInsert(rules: suspend () -> Unit) {
                insertRules = rules
            }

            fun Rules.ifUpdate(rules: suspend () -> Unit) {
                updateRules = rules
            }

            fun Rules.ifDelete(rules: suspend () -> Unit) {
                deleteRules = rules
            }

            fun Rules.ifInsertOrUpdate(rules: suspend () -> Unit) {
                insertOrUpdateRules = rules
            }

            fun Rules.commons(rules: suspend () -> Unit) {
                commonsRules = rules
            }
        }
    }

    private fun buildRules(actionName: String) = buildRules(actionName, getService())

    private fun getService() = if (service == null) null else service as TService
    private fun getDateTime() = ZonedDateTime.now(ZoneId.of("UTC"))
    private suspend fun validateBeforeInsert(rules: Rules, actionName: String) {
        entityMode = EntityMode.INSERT
        checkService()
        rules.executeInsertRulesBeforeInsert()
        runValidateValueObjects()
        runValidateAggregateEntityValueObjects()
        if (!insertable) {
            addNotificationMessage(
                NotificationMessage(
                    fieldName = ::insertable.name,
                    fieldValue = insertable.toString(),
                    funName = "Insert.$actionName()",
                    notification = InsertNotAllowedNotification()
                )
            )
        }
        if (id != null) {
            addNotificationMessage(
                NotificationMessage(
                    fieldName = ::id.name,
                    fieldValue = id?.value,
                    funName = "Insert.$actionName()",
                    notification = UnableToInsertWithIDNotification()
                )
            )
        }
    }

    private suspend fun validateBeforeUpdate(rules: Rules, actionName: String) {
        entityMode = EntityMode.UPDATE
        checkService()
        rules.executeUpdateRulesBeforeUpdate()
        runValidateValueObjects()
        runValidateAggregateEntityValueObjects()
        if (!updatable) {
            addNotificationMessage(
                NotificationMessage(
                    fieldName = ::updatable.name,
                    fieldValue = updatable.toString(),
                    funName = "Update.$actionName()",
                    notification = UpdateNotAllowedNotification()
                )
            )
        }
        id?.isValid(::id.name, notificationContext)
            ?: addNotificationMessage(
                NotificationMessage(
                    fieldName = ::id.name,
                    funName = "Update.$actionName()",
                    notification = UnableToUpdateWithoutIDNotification()
                )
            )
    }

    private suspend fun validateBeforeDelete(rules: Rules, actionName: String) {
        entityMode = EntityMode.DELETE
        checkService()
        rules.executeDeleteRulesBeforeDelete()
        runValidateValueObjects()
        runValidateAggregateEntityValueObjects()
        if (!deletable) {
            addNotificationMessage(
                NotificationMessage(
                    fieldName = ::deletable.name,
                    fieldValue = deletable.toString(),
                    funName = "Delete.$actionName()",
                    notification = DeleteNotAllowedNotification()
                )
            )
        }
        id?.isValid(::id.name, notificationContext)
            ?: addNotificationMessage(
                NotificationMessage(
                    fieldName = ::id.name,
                    funName = "Delete.$actionName()",
                    notification = UnableToDeleteWithoutIDNotification()
                )
            )
    }

    private fun changeFieldNames() {
        notificationContext.notifications.forEach { notificationMessage ->
            if (!fieldNamesToChange[notificationMessage.fieldName].isNullOrBlank()) {
                notificationContext.changeFieldName(
                    notificationMessage,
                    fieldNamesToChange[notificationMessage.fieldName]!!
                )
            }
        }
    }

    private fun checkService() {
        if (getService() == null && serviceRequired) {
            addNotificationMessage(
                NotificationMessage(
                    fieldName = ::service.name,
                    fieldValue = null,
                    funName = ::checkService.name,
                    notification = ServiceIsRequiredNotification()
                )
            )
            checkNotifications()
        }
    }

    private suspend fun runValidateValueObjects() {
        validateValueObjects.forEach {
            it.second.isValid(it.first, notificationContext)
        }
    }

    private suspend fun runValidateAggregateEntityValueObjects() {
        validateAggregateValueObjects.forEach {
            it.second.isValid(getService(), entityMode, it.first, notificationContext)
        }
    }

    private fun Any.writeAsString(): String = jacksonObjectMapper().apply {
        registerKotlinModule()
        registerModule(JavaTimeModule())
        setDateFormat(SimpleDateFormat("yyyy-MM-dd HH:mm a z"))
    }.writeValueAsString(this)

    private fun startEntity() {
        events.clear()
        validateValueObjects.clear()
        validateAggregateValueObjects.clear()
        notificationContext.clearNotifications()
        entityMode = EntityMode.DISPLAY
        service = null
    }
}