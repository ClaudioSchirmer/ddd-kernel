package br.dev.schirmer.ddd.kernel.domain.models

import br.dev.schirmer.ddd.kernel.domain.exception.DomainNotificationContextException
import br.dev.schirmer.ddd.kernel.domain.notifications.NotificationMessage
import br.dev.schirmer.ddd.kernel.domain.valueobjects.Id
import kotlinx.coroutines.runBlocking
import java.util.*

sealed interface ValidEntity<TEntity : Entity<TEntity, *, *, *>> {
    class Insertable<TEntity : Entity<TEntity, *, TInsertable, *>, TInsertable : ValidEntity<TEntity>>(val entity: TInsertable) :
        ValidEntity<TEntity>

    class Updatable<TEntity : Entity<TEntity, *, *, TUpdatable>, TUpdatable : ValidEntity<TEntity>>(val entity: TUpdatable) :
        ValidEntity<TEntity>

    class Deletable<TEntity : Entity<TEntity, *, *, *>>(val id: Id) : ValidEntity<TEntity>
}

class ServiceXYZ : Service<XYZ> {
    fun execute() {
        println(" oi ")
    }
}

data class XYZ(
    val x: Id,
    val y: String
) : Entity<XYZ, ServiceXYZ, XYZ.Insertable, XYZ>() {

    class Insertable(
        val x: Id
    ) : ValidEntity<XYZ>

    override val insertableValidEntity: ValidEntity.Insertable<XYZ, Insertable>
        get() = ValidEntity.Insertable(Insertable(x))

    override val insertable: Boolean
        get() = true

    override val updatable: Boolean
        get() = true

    init {
        updateRules {
            id = Id(UUID.randomUUID())
        }
        businessRules {
            x.addToValidate(::x.name)
        }
    }
}

fun main() {
    runBlocking {
        runCatching {
            val insertable = XYZ(Id("asdf"), "Guedes").getUpdatable()
            println(insertable.entity.x.value)
        }.onFailure {
            (it as DomainNotificationContextException).notificationContext.let(::println)
        }
    }
}