package br.dev.schirmer.ddd.kernel.domain.models

import br.dev.schirmer.ddd.kernel.domain.events.DomainEvent
import br.dev.schirmer.ddd.kernel.domain.valueobjects.Id
import java.time.ZonedDateTime
import java.util.*

sealed interface ValidEntity<TEntity : Entity<TEntity, *, *, *>> {
    class Insertable<TEntity : Entity<TEntity, *, TInsertable, *>, TInsertable : ValidEntity<TEntity>>(
        signature: UUID,
        signatureChecker: Entity<TEntity, *, TInsertable, *>.SignatureChecker,
        val entityName: String,
        val actionName: String,
        val id: Id?,
        val fieldsToInsert: TInsertable,
        val dateTime: ZonedDateTime,
        val events: List<DomainEvent>
    ) : ValidEntity<TEntity> {
        init {
            signatureChecker.invoke(signature)
        }
    }

    class Updatable<TEntity : Entity<TEntity, *, *, TUpdatable>, TUpdatable : ValidEntity<TEntity>>(
        signature: UUID,
        signatureChecker: Entity<TEntity, *, *, TUpdatable>.SignatureChecker,
        val entityName: String,
        val actionName: String,
        val id: Id,
        val fieldsToUpdate: TUpdatable,
        val dateTime: ZonedDateTime,
        val events: List<DomainEvent>
    ) : ValidEntity<TEntity> {
        init {
            signatureChecker.invoke(signature)
        }
    }

    class Deletable<TEntity : Entity<TEntity, *, *, *>>(
        signature: UUID,
        signatureChecker: Entity<TEntity, *, *, *>.SignatureChecker,
        val entityName: String,
        val actionName: String,
        val id: Id,
        val deletedFields: String,
        val dateTime: ZonedDateTime,
        val events: List<DomainEvent>
    ) : ValidEntity<TEntity> {
        init {
            signatureChecker.invoke(signature)
        }
    }
}