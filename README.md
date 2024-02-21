# DDD-Kernel
ddd-kernel is a Kotlin library for fast Domain-Driven Design development

>Package: **br.dev.schirmer.ddd.kernel**<br/>
>Developer: **Claudio Schirmer Guedes**

# *Domain*
## Interfaces
 
#### Activatable
#### Context
#### DomainNotification
#### EnumValueObject < TValue :  Any >
#### AggregateValueObject < TSuperEntity : Entity >
#### Service < TEntity : Entity >

## Classes

### DomainEvent
```kotlin    
class DomainEvent(
    eventType: EventType,
    className: String,
    message: String,
    values: Any? = null,
    exception: Throwable? = null
) : Event(eventType, className, message, values, exception)
```
### DomainException
```kotlin    
class DomainNotificationContextException(val notificationContext: List<NotificationContext>)
```

### NotificationMessage
```kotlin
data class NotificationMessage(
    val fieldName: String? = null,
    val fieldValue: String? = null,
    val funName: String? = null,
    val exception: Throwable? = null,
    val notification: Notification
)
```

### NotificationContext
```kotlin
class NotificationContext(
    val context: String
)
```
#### Properties
```kotlin
notifications: List<NotificationMessage>
```

#### Methods
```kotlin
addNotification(notificationMessage: NotificationMessage)

changeFieldName(notificationMessage: NotificationMessage, newFieldName: String)

changeFieldName(originalFieldName: String, newFieldName: String)

clearNotifications()

copy(nContext: String? = null) : NotificationContext
```

### EntityMode
```kotlin
enum class EntityMode(override val value: Int) {
    UNKNOWN(0),
    DISPLAY(1),
    INSERT(2),
    UPDATE(3),
    DELETE(4);
}
```

## Abstract Classes

### ScalarValueObject<TObject: Any> : ValueObject
#### Properties
```kotlin
value: TObject
```
#### Abstract method
```kotlin
isValid(fieldName: String? = null, notificationContext: NotificationContext? = null) : Boolean
```

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

#### Abstract methods
```kotlin
buildRules(actionName: String, service: TService?): Rules
```

#### Protected methods
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

#### Protected open methods
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
### EntityAggregateRoot<TEntity : Entity<TEntity, TService, TInsertable, TUpdatable> : Entity

#### Protected properties
```kotlin
aggregateItems :  MutableMap<String, MutableList<AggregateItem<TEntity, TService, AggregateValueObject<TEntity, TService>>>>
```

#### Protected methods
```kotlin
getAggregateItems<TAggregateEntityValueObject>(): List<AggregateItem<TEntity, TService, TAggregateEntityValueObject>>

aggregateConstructor(items: List<TAggregateEntityValueObject>?)

addAggregateItem(item: TAggregateEntityValueObject?)

changeAggregateItem(
    item: TAggregateEntityValueObject?,
    changes: TAggregateEntityValueObject.() -> Unit
)

removeAggregateItem(item: TAggregateEntityValueObject?)

clearAggregateItems<TAggregateEntityValueObject>()

isAggregateItemValid(item: TAggregateEntityValueObject?): Boolean
```

### Repository<TEntity : Entity<TEntity, *, TInsertable, TUpdatable>, TInsertable : ValidEntity<TEntity>, TUpdatable : ValidEntity<TEntity>>

#### Methods
```kotlin
findById(
    id: Id,
    beforeFindById: (suspend () -> Any)? = null,
    afterFindById: (suspend () -> Unit)? = null
)

insert(
    insertable: ValidEntity.Insertable<TEntity, TInsertable>,
    beforeInsert: (suspend () -> Any)? = null,
    afterInsert: (suspend (Id) -> Unit)? = null
): Id

update(
    updatable: ValidEntity.Updatable<TEntity, TUpdatable>,
    beforeUpdate: (suspend () -> Any)? = null,
    afterUpdate: (suspend () -> Unit)? = null
)

delete(
    deletable: ValidEntity.Deletable<TEntity>,
    beforeDelete: (suspend () -> Any)? = null,
    afterDelete: (suspend () -> Unit)? = null
)
```

#### Protected open methods
```kotlin
insert(
    insertable: ValidEntity.Insertable<TEntity, TInsertable>,
    beforeInsertResult: Any?
): Id

update(updatable: ValidEntity.Updatable<TEntity, TUpdatable>, beforeUpdateResult: Any?)

delete(deletable: ValidEntity.Deletable<TEntity>, beforeDeleteResult: Any?)

publish(insertable: ValidEntity.Insertable<TEntity, TInsertable>)

publish(updatable: ValidEntity.Updatable<TEntity, TUpdatable>)

publish(deletable: ValidEntity.Deletable<TEntity>)    
```

## Extensions

### from AggregateItem

#### Items
```kotlin
getCurrentItems(): List<TAggregateEntityValueObject>

forEachCurrentItems(
    execute: (TAggregateEntityValueObject) -> Unit
)
```

#### Added
```kotlin
getAddedItems(): List<TAggregateEntityValueObject>

forEachAddedItems(
    execute: (TAggregateEntityValueObject) -> Unit
)
```
#### Changed
```kotlin
getChangedItems(): List<TAggregateEntityValueObject>

forEachChangedItems(
    execute: (TAggregateEntityValueObject) -> Unit
)
```
#### Removed
```kotlin
getRemovedItems(): List<TAggregateEntityValueObject>

forEachRemovedItems(
    execute: (TAggregateEntityValueObject) -> Unit
)
```
### from EnumValueObject

#### get
```kotlin
getByValue<TEnum>(value: Int) : TEnum

getByValue<TEnum>(value: String) : TEnum
```

# *Application*
## Open Classes

### AppContext
```kotlin    
open class AppContext(
    override val id: UUID
) : Context
```

## Objects

### Application
```kotlin    
open class AppContext(
    override val id: UUID
) : Context
```

