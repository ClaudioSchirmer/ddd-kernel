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
abstract class Entity<TEntity : Entity<TEntity, TService, TInsertable, TUpdatable>,
        TService : Service<TEntity>,
        TInsertable : SealedValidEntity<TEntity>,
        TUpdatable : SealedValidEntity<TEntity>>
    : SealedValidEntity<TEntity> {

    private var signature: UUID? = null
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

    fun checkSignature(signature: UUID) {
        if (signature != this.signature) {
            addNotificationMessage(
                NotificationMessage(
                    fieldName = "signature",
                    fieldValue = signature.toString(),
                    funName = ::checkSignature.name,
                    notification = InvalidDomainSignatureNotification()
                )
            )
            checkNotifications()
        }
    }

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
                validateToInsert("insert")
            }

            EntityMode.UPDATE -> {
                validateToUpdate("update")
            }

            EntityMode.DELETE -> {
                validateToDelete("delete")
            }

            else -> {}
        }
        this.notificationContext.notifications.forEach {
            notificationContext.addNotification(it)
        }
        startEntity()
        return notificationContext.notifications.isNotEmpty()
    }

    suspend fun getInsertable(service: TService? = null) = getInsertable("getInsertable", service)
    suspend fun getUpdatable(service: TService? = null) = getUpdatable("getUpdatable", service)
    suspend fun getDeletable(service: TService? = null) = getDeletable("getDeletable", service)

    protected suspend fun getInsertable(
        actionName: String,
        service: TService? = null
    ): SealedValidEntity.Insertable<TEntity, TInsertable> {
        startEntity()
        this.service = service
        validateToInsert(actionName)
        checkNotifications()
        return SealedValidEntity.Insertable(
            signature!!,
            this as TEntity,
            this::class.simpleName!!,
            actionName,
            id,
            getValidEntityInsertable(),
            getDateTime(),
            events
        )
    }

    protected suspend fun getUpdatable(
        actionName: String,
        service: TService? = null
    ): SealedValidEntity.Updatable<TEntity, TUpdatable> {
        startEntity()
        this.service = service
        validateToUpdate(actionName)
        checkNotifications()
        return SealedValidEntity.Updatable(
            signature!!,
            this as TEntity,
            this::class.simpleName!!,
            actionName,
            id!!,
            getValidEntityUpdatable(),
            getDateTime(),
            events
        )
    }

    protected suspend fun getDeletable(
        actionName: String,
        service: TService? = null
    ): SealedValidEntity.Deletable<TEntity> {
        startEntity()
        this.service = service
        validateToDelete(actionName)
        checkNotifications()
        return SealedValidEntity.Deletable<TEntity>(
            signature!!,
            this as TEntity,
            this::class.simpleName!!,
            actionName,
            id!!,
            this.writeAsString(),
            getDateTime(),
            events
        )
    }

    protected open suspend fun getValidEntityInsertable(): TInsertable = this as TInsertable
    protected open suspend fun getValidEntityUpdatable(): TUpdatable = this as TUpdatable
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

    protected fun saveState() {
        entityState = this.writeAsString()
    }

    protected open suspend fun beforeValidations(actionName: String, service: TService?) {}
    protected open suspend fun ifInsertOrUpdate(actionName: String, service: TService?) {}
    protected open suspend fun ifInsert(actionName: String, service: TService?) {}
    protected open suspend fun ifUpdate(actionName: String, service: TService?) {}
    protected open suspend fun ifDelete(actionName: String, service: TService?) {}
    protected open suspend fun afterValidations(actionName: String, service: TService?) {}

    protected inline fun <reified Entity : TEntity> getLastSavedState(addNotificationIfNull: Boolean = true): Entity? {
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

    private fun getService() = if (service == null) null else service as TService
    private fun getDateTime() = ZonedDateTime.now(ZoneId.of("UTC"))
    private suspend fun validateToInsert(actionName: String) {
        entityMode = EntityMode.INSERT
        beforeValidations(actionName, this.service)
        checkService()
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
        ifInsertOrUpdate(actionName, this.service)
        ifInsert(actionName, this.service)
        runValidateValueObjects()
        runValidateAggregateEntityValueObjects()
        afterValidations(actionName, this.service)
    }

    private suspend fun validateToUpdate(actionName: String) {
        entityMode = EntityMode.UPDATE
        beforeValidations(actionName, this.service)
        checkService()
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
        ifInsertOrUpdate(actionName, this.service)
        ifUpdate(actionName, this.service)
        runValidateValueObjects()
        runValidateAggregateEntityValueObjects()
        afterValidations(actionName, this.service)
    }

    private suspend fun validateToDelete(actionName: String) {
        entityMode = EntityMode.DELETE
        beforeValidations(actionName, this.service)
        checkService()
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
        ifDelete(actionName, this.service)
        runValidateValueObjects()
        runValidateAggregateEntityValueObjects()
        afterValidations(actionName, this.service)
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
        signature = UUID.randomUUID()
    }
}