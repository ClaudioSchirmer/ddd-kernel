# DDD-Kernel
ddd-kernel is a Kotlin library for fast Domain-Driven Design development

>Package: **br.dev.schirmer.ddd.kernel**<br/>
>Developer: **Claudio Schirmer Guedes**<br/>
>Version: **14.0.0**</br>
>**[Website](https://www.schirmer.dev.br/about-me/ddd-with-kotlin)**

## Configuration

### Properties
```kotlin
language: (() -> Language)

translationsFolder: String
```
### Methods
```kotlin
importTranslateModules(translateModules: List<TranslateModule>)

importTranslateModule(translateModule: TranslateModule)
```

### Example
```kotlin    
dddKernel {
    
    language = {
        if (condition) {
            Language.PT_BR
        } else {
            Language.ENG
        }
    }
    
    translationsFolder = "myFolder"
    
    importTranslateModule(MyModule)
}
```

# *Domain*
## Interfaces
 
#### Activatable
#### Context
#### DomainNotification
#### ValueObject < TValue :  Any >
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
) : Event
```
### DomainNotificationContextException
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

### Id
```kotlin
data class Id(
    var value: String
)
```

#### Properties
```kotlin
uuid
```
#### Methods
```kotlin
isValid(fieldName: String?, notificationContext: NotificationContext?): Boolean
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

### EventType
```kotlin
enum class EventType(override val value: String){
    UNKNOWN("UNKNOWN"),
    LOG("LOG"),
    AUDIT("AUDIT"),
    DEBUG("DEBUG"),
    ERROR("ERROR"),
    WARNING("WARNING");
}
```
## Abstract Classes

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

#### Rules builder
```kotlin
fun interface Rules {
    suspend fun Rules.build()
}
```

#### Entensions for Rules
```kotlin
Rules.ifInsert(rules: suspend () -> Unit)

Rules.ifUpdate(rules: suspend () -> Unit)

Rules.ifDelete(rules: suspend () -> Unit)

Rules.ifInsertOrUpdate(rules: suspend () -> Unit)
```

#### Protected methods
```kotlin
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

### Repository<TEntity : Entity<TEntity, *, TInsertable, TUpdatable>, TInsertable : ValidEntity<TEntity>, TUpdatable : ValidEntity< TEntity >>

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

## Interfaces

#### ApplicationNotification
#### TranslateModule
#### Command < TResult >
#### Query < TResult >

## Open Classes
### AppContext
```kotlin    
open class AppContext(
    override val id: UUID
) : Context
```

## Classes
### Language
```kotlin    
enum class Language(override val value: Int) {
    UNKNOWN(0),
    PT_BR(1),
    ENG(2),
    ES(3),
    FR(4);
}
```

### ApplicationEvent
```kotlin    
class ApplicationEvent(
    eventType: EventType,
    className: String,
    message: String,
    values: Any? = null,
    exception: Throwable? = null
) : Event
```

### ApplicationNotificationContextException
```kotlin    
class ApplicationNotificationContextException(val notificationContext: List<NotificationContext>)
```
### NotificationMessageDTO
```kotlin    
data class NotificationMessageDTO(
val fieldName: String? = null,
val fieldValue: String? = null,
val funName: String? = null,
val message: String
)
```

### NotificationContextDTO
```kotlin    
data class NotificationContextDTO(
    val context: String,
    val notifications: List<NotificationMessageDTO>
)
```

### Pipeline
#### Constructor
```kotlin
Pipeline(val diAware: DIAware, var appContext: AppContext? = null)
```

#### Methods
```kotlin
dispatch(
    query: TQuery,
    appContext: AppContext? = null
): Result<TResult>

dispatch(
    command: TCommand,
    appContext: AppContext? = null
): List<Result<TResult>>
```

## Abstract Classes
### Handler<TResult, TRequest : Request< TResult >>
#### Properties
```kotlin    
appContext: AppContext
```

#### Abstract methods
```kotlin
invoke(request: TRequest): TResult
```
## Extensions
### from Result

#### Success
```kotlin    
ifSuccess<TResult>(action: TResult.() -> Unit)
```
#### Failure
```kotlin    
ifFailure<TResult>(action: List<NotificationContextDTO>.() -> Unit)
```
#### Exception
```kotlin    
ifException<TResult>(action: Throwable.() -> Unit)
```
#### Get
```kotlin    
getFromResult<TResult, TReturn>(
    noinline defaultActionOnFailure: (() -> TReturn)? = null,
    actions: Result<TResult>.() -> TReturn
) : TReturn
```

### from List< Result >
#### with First
```kotlin    
withFirstResult<TResult>(
    action: Result<TResult>.() -> Unit
)

getFromFirstResult(
    noinline defaultActionOnFailure: (() -> TReturn)? = null,
    action: Result<TResult>.() -> TReturn
): TReturn

withFirstIfSuccess<TResult>(
    defaultActionOtherwise: (() -> Unit) = {},
    action: TResult.() -> Unit
)
```

#### with All
```kotlin
forEachResult<TResult>(action: Result<TResult>.() -> Unit)
```

### from List< NotificationContext >
```kotlin
toNotificationContextDTO() : List<NotificationContextDTO>
```
### from String
```kotlin
getTranslatedMessage(): String
```
# *Infrastructure*

## Interfaces

#### InfrastructureNotification

## Classes
### InfrastructureEvent
```kotlin    
class InfrastructureEvent(
    eventType: EventType,
    className: String,
    message: String,
    values: Any? = null,
    exception: Throwable? = null
) : Event
```
### InfrastructureNotificationException
```kotlin    
class InfrastructureNotificationException(val notificationContext: List<NotificationContext>)
```
## Extensions
### from Event Or List < Event >

#### publish
```kotlin    
publish(context: Context)
```
### from ValidEntity

#### publish
```kotlin    
publish(context: Context)
```

# *Web*

## Classes
### ErrorMessage
```kotlin    
data class ErrorMessage(
    val field: String?,
    val value: String?,
    val message: String,
)
```

### Error
```kotlin    
data class Error(
    val context: String,
    val messages: List<ErrorMessage>
)
```


### Response
```kotlin    
data class Response(
    val status: Int,
    val description: String,
    val errors: List<Error>? = null
)
```

## Extensions
### from List< NotificationContextDTO >
```kotlin
toResponse(
    httpStatus: Int = 400,
    httpDescription: String = "Bad Request"
) : Response
```
### from Throwable
```kotlin
toResponse(httpStatus: Int, httpDescription: String): Response

toBadRequestResponse() : Response

toNotFoundResponse() : Response

toInternalServerErrorResponse() : Response
```
