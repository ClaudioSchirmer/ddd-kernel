package br.dev.schirmer.ddd.kernel.domain.models

import br.dev.schirmer.ddd.kernel.domain.exception.DomainNotificationContextException
import br.dev.schirmer.ddd.kernel.domain.valueobjects.Id
import br.dev.schirmer.ddd.kernel.domain.valueobjects.TransactionMode
import br.dev.schirmer.ddd.kernel.infrastructure.validentity.publish
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonRawValue
import com.fasterxml.jackson.databind.JsonMappingException
import kotlinx.coroutines.runBlocking
import java.time.ZonedDateTime
import java.util.*

sealed interface ValidEntity<TEntity : Entity<TEntity, *, *, *>> {
    class Insertable<TEntity : Entity<TEntity, *, TInsertable, *>, TInsertable : ValidEntity<TEntity>>(
        val id: Id?,
        val transactionMode: TransactionMode,
        val fieldsToInsert: TInsertable,
        val dateTime: ZonedDateTime
    ) : ValidEntity<TEntity>

    class Updatable<TEntity : Entity<TEntity, *, *, TUpdatable>, TUpdatable : ValidEntity<TEntity>>(
        val id: Id,
        val transactionMode: TransactionMode,
        val fieldsToUpdate: TUpdatable,
        val dateTime: ZonedDateTime
    ) : ValidEntity<TEntity>

    class Deletable<TEntity : Entity<TEntity, *, *, *>>(
        val id: Id,
        val transactionMode: TransactionMode,
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
) : Entity<XYZ, Nothing, XYZ, XYZ.Updatable>(updatable = true, deletable = true, insertable = true) {
    class Updatable(
        val x: Id
    ) : ValidEntity<XYZ>

    override val updatableValidEntity: Updatable
        get() = Updatable(x)

    init {
        id = Id(UUID.randomUUID())
    }
}

fun main() {
    runBlocking {
        //runCatching {
            val insertable = XYZ(Id("Abacaxi"), "Guedes").getUpdatable()
            insertable.publish()
        //}.onFailure {

          //  (it as DomainNotificationContextException).notificationContext.let(::println)
        //}
    }
}