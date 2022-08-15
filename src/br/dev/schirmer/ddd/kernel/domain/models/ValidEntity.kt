package br.dev.schirmer.ddd.kernel.domain.models

import br.dev.schirmer.ddd.kernel.application.configuration.Context
import br.dev.schirmer.ddd.kernel.domain.valueobjects.Id
import br.dev.schirmer.ddd.kernel.infrastructure.validentity.publishAudit
import com.fasterxml.jackson.annotation.JsonRawValue
import kotlinx.coroutines.runBlocking
import java.time.ZonedDateTime
import java.util.*

sealed interface ValidEntity<TEntity : Entity<TEntity, *, *, *>> {
    class Insertable<TEntity : Entity<TEntity, *, TInsertable, *>, TInsertable : ValidEntity<TEntity>>(
        val entityName: String,
        val id: Id?,
        val fieldsToInsert: TInsertable,
        val dateTime: ZonedDateTime
    ) : ValidEntity<TEntity>

    class Updatable<TEntity : Entity<TEntity, *, *, TUpdatable>, TUpdatable : ValidEntity<TEntity>>(
        val entityName: String,
        val id: Id,
        val fieldsToUpdate: TUpdatable,
        val dateTime: ZonedDateTime
    ) : ValidEntity<TEntity>

    class Deletable<TEntity : Entity<TEntity, *, *, *>>(
        val entityName: String,
        val id: Id,
        @JsonRawValue
        val deletedFields: String,
        val dateTime: ZonedDateTime
    ) : ValidEntity<TEntity>
}

class ServiceXYZ : Service<XYZ> {
    fun execute() {
        println(" oi ")
    }
}

data class XYZ(
    val x: Id,
    val y: String
) : Entity<XYZ, Nothing, XYZ, XYZ>(updatable = true, deletable = true, insertable = true) {
    class Updatable(
        val x: Id
    ) : ValidEntity<XYZ>

    init {
        id = Id(UUID.randomUUID())
    }
}

fun main() {
    runBlocking {
        //runCatching {
        val insertable = XYZ(Id("Abacaxi"), "Guedes").getDeletable()
        insertable.publishAudit(Context(id = UUID.randomUUID()))
        //}.onFailure {

        //  (it as DomainNotificationContextException).notificationContext.let(::println)
        //}
    }
}