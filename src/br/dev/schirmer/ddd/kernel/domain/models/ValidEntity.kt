package br.dev.schirmer.ddd.kernel.domain.models

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
) : Entity<XYZ, ServiceXYZ, XYZ, XYZ.Fields>() {

    class Fields(
        val y: String
    ) : ValidEntity<XYZ>

    override val updatable: Boolean
        get() = true

    override val updatableValidEntity: ValidEntity.Updatable<XYZ, Fields>
        get() = ValidEntity.Updatable(Fields(y))

    init {
        x.addToValidate(::x.name)
        id = Id(UUID.randomUUID())

        updateRules {
            it?.execute()
        }
    }
}

fun main() {
    runBlocking {
        runCatching {
            val insertable = XYZ(Id(UUID.randomUUID()), "Guedes").getUpdatable()
            println(insertable.entity.y)
        }.onFailure {
            println(it)
            //(it as DomainNotificationContextException).notificationContext.let(::println)
        }
    }
}