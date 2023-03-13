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
import java.util.*
import kotlin.reflect.full.findAnnotation
import br.dev.schirmer.ddd.kernel.domain.models.ValidEntity as SealedValidEntity

@Suppress("UNCHECKED_CAST")
abstract class Entity<TEntity : Entity<TEntity, TService, TInsertable, TUpdatable>, TService : Service<TEntity>, TInsertable : SealedValidEntity<TEntity>, TUpdatable : SealedValidEntity<TEntity>>
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
                validateToInsert()
            }

            EntityMode.UPDATE -> {
                validateToUpdate()
            }

            EntityMode.DELETE -> {
                validateToDelete()
            }

            else -> {}
        }
        this.notificationContext.notifications.forEach {
            notificationContext.addNotification(it)
        }
        startEntity()
        return notificationContext.notifications.isNotEmpty()
    }

    suspend fun getInsertable(service: TService? = null): SealedValidEntity.Insertable<TEntity, TInsertable> {
        startEntity()
        this.service = service
        validateToInsert()
        checkNotifications()
        return SealedValidEntity.Insertable(this::class.simpleName!!, id, getInsertable(), getDateTime(), events)
    }

    suspend fun getUpdatable(service: TService? = null): SealedValidEntity.Updatable<TEntity, TUpdatable> {
        startEntity()
        this.service = service
        validateToUpdate()
        checkNotifications()
        return SealedValidEntity.Updatable(this::class.simpleName!!, id!!, getUpdatable(), getDateTime(), events)
    }

    suspend fun getDeletable(service: TService? = null): SealedValidEntity.Deletable<TEntity> {
        startEntity()
        this.service = service
        validateToDelete()
        checkNotifications()
        return SealedValidEntity.Deletable(this::class.simpleName!!, id!!, this.writeAsString(), getDateTime(), events)
    }

    protected open fun getInsertable(): TInsertable = this as TInsertable
    protected open fun getUpdatable(): TUpdatable = this as TUpdatable

    protected interface ValidEntity<TEntity : Entity<TEntity, *, *, *>> : SealedValidEntity<TEntity>

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

    protected abstract fun buildRules(service: TService?): Rules
    protected fun rules(rules: Rules.() -> Unit): Rules = Rules().apply(rules)

    protected class Rules {
        private var insert: suspend () -> Unit = { }
        private var update: suspend () -> Unit = { }
        private var delete: suspend () -> Unit = { }
        private var insertOrUpdate: suspend () -> Unit = { }
        private var commons: suspend () -> Unit = { }
        suspend fun executeInsertRules() {
            commons()
            insertOrUpdate()
            insert()
        }

        suspend fun executeUpdateRules() {
            commons()
            insertOrUpdate()
            update()
        }

        suspend fun executeDeleteRules() {
            commons()
            delete()
        }

        companion object {
            fun Rules.ifInsert(rules: suspend () -> Unit) {
                insert = rules
            }

            fun Rules.ifUpdate(rules: suspend () -> Unit) {
                update = rules
            }

            fun Rules.ifDelete(rules: suspend () -> Unit) {
                delete = rules
            }

            fun Rules.ifInsertOrUpdate(rules: suspend () -> Unit) {
                insertOrUpdate = rules
            }

            fun Rules.commons(rules: suspend () -> Unit) {
                commons = rules
            }
        }
    }

    private fun buildRules() = buildRules(getService())

    private fun getService() = if (service == null) null else service as TService
    private fun getDateTime() = ZonedDateTime.now(ZoneId.of("UTC"))
    private suspend fun validateToInsert() {
        entityMode = EntityMode.INSERT
        checkService()
        buildRules().executeInsertRules()
        runValidateValueObjects()
        runValidateAggregateEntityValueObjects()
        if (!insertable) {
            addNotificationMessage(
                NotificationMessage(
                    fieldName = ::insertable.name,
                    fieldValue = insertable.toString(),
                    funName = "getInsertable",
                    notification = InsertNotAllowedNotification()
                )
            )
        }
        if (id != null) {
            addNotificationMessage(
                NotificationMessage(
                    fieldName = ::id.name,
                    fieldValue = id?.value,
                    funName = "getInsertable",
                    notification = UnableToInsertWithIDNotification()
                )
            )
        }
    }

    private suspend fun validateToUpdate() {
        entityMode = EntityMode.UPDATE
        checkService()
        buildRules().executeUpdateRules()
        runValidateValueObjects()
        runValidateAggregateEntityValueObjects()
        if (!updatable) {
            addNotificationMessage(
                NotificationMessage(
                    fieldName = ::updatable.name,
                    fieldValue = updatable.toString(),
                    funName = "getUpdatable",
                    notification = UpdateNotAllowedNotification()
                )
            )
        }
        id?.isValid(::id.name, notificationContext)
            ?: addNotificationMessage(
                NotificationMessage(
                    fieldName = ::id.name,
                    funName = "getUpdatable",
                    notification = UnableToUpdateWithoutIDNotification()
                )
            )
    }

    private suspend fun validateToDelete() {
        entityMode = EntityMode.DELETE
        checkService()
        buildRules().executeDeleteRules()
        runValidateValueObjects()
        runValidateAggregateEntityValueObjects()
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
        id?.isValid(::id.name, notificationContext)
            ?: addNotificationMessage(
                NotificationMessage(
                    fieldName = ::id.name,
                    funName = ::getDeletable.name,
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

    private fun checkNotifications() {
        if (notificationContext.notifications.isNotEmpty()) {
            notificationContextCollection.add(notificationContext)
        }
        if (notificationContextCollection.any { it.notifications.isNotEmpty() }) {
            changeFieldNames()
            throw DomainNotificationContextException(notificationContextCollection)
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