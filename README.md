# DDD-Kernel
ddd-kernel is a Kotlin library for fast Domain-Driven Design development

>Package: **br.dev.schirmer.ddd.kernel**<br/>
>Developer: **Claudio Schirmer Guedes**

## *Domain*
### Interfaces

---

#### Activatable
#### Context
#### DomainNotification
#### ValueObject
#### EnumValueObject
#### AggregateValueObject
#### Service
#### ValidEntity

### Classes

---

#### DomainEvent
```kotlin    
class DomainEvent(
    eventType: EventType,
    className: String,
    message: String,
    values: Any? = null,
    exception: Throwable? = null
) : Event(eventType, className, message, values, exception)
```
#### DomainException
```kotlin    
class DomainNotificationContextException(val notificationContext: List<NotificationContext>)
```

### Abstract Classes

---

### Entity<TEntity : Entity<TEntity, TService, TInsertable, TUpdatable>

#### Annotations
```kotlin
    EntityModes(vararg val modes: EntityMode)

    EntityRequiresService
```

#### Protected properties
```kotlin
    entityState: String?

    notificationContext: NotificationContext
```

#### Properties
```kotlin    
    id: Id?    
```

#### Abstract Methods
```kotlin
    buildRules(actionName: String, service: TService?): Rules
```

#### Protected Methods
```kotlin
    rulesBuilder(rules: suspend Rules.() -> Unit): Rules

    getInsertable(
        actionName: String,
        service: TService? = null
    ) : ValidEntity.Insertable<TEntity, TInsertable>

    getUpdatable(
        actionName: String,
        service: TService? = null
    ): ValidEntity.Updatable<TEntity, TUpdatable>

    getDeletable(
        actionName: String,
        service: TService? = null
    ): ValidEntity.Deletable<TEntity>

    ValueObject.addToValidate(name: String) : Boolean

    AggregateValueObject<TEntity, TService>.addToValidate(name: String) : Boolean

    addNotificationMessage(notificationMessage: NotificationMessage)

    addNotificationContext(notificationContext: NotificationContext)

    DomainEvent.register()

    registerEvent(domainEvent: DomainEvent)

    saveState()

    getLastSavedState<TEntity>(addNotificationIfNull: Boolean = true): TEntity?

    checkNotifications()
```

#### Protected open Methods
```kotlin
    getValidEntityInsertable(): TInsertable

    getValidEntityUpdatable(): TUpdatable
```

#### Protected interface
```kotlin
    ValidEntity<TEntity : Entity<TEntity, *, *, *>>
```

#### Methods
```kotlin
    addFieldNameToChange(originalFieldName: String, newFieldName: String)

    addFieldNamesToChange(fieldNamesToChange: Map<String, String>)

    isValid(
        entityMode: EntityMode,
        service: TService? = null,
        notificationContext: NotificationContext
    ): Boolean

    getInsertable(service: TService? = null) : ValidEntity.Insertable<TEntity, TInsertable>

    getUpdatable(service: TService? = null) :  ValidEntity.Updatable<TEntity, TUpdatable>

    getDeletable(service: TService? = null) :  ValidEntity.Deletable<TEntity>
```
