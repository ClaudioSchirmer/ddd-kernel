package br.dev.schirmer.ddd.kernel.domain.models

import br.dev.schirmer.ddd.kernel.domain.events.DomainEvent
import br.dev.schirmer.ddd.kernel.domain.valueobjects.Id
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonRawValue
import java.time.ZonedDateTime

sealed interface ValidEntity<TEntity : Entity<TEntity, *, *, *>> {
    class Insertable<TEntity : Entity<TEntity, *, TInsertable, *>, TInsertable : ValidEntity<TEntity>>(
        val entityName: String,
        @JsonIgnore
        val actionName: String,
        val id: Id?,
        val fieldsToInsert: TInsertable,
        val dateTime: ZonedDateTime,
        @JsonIgnore
        val events: List<DomainEvent>
    ) : ValidEntity<TEntity>

    class Updatable<TEntity : Entity<TEntity, *, *, TUpdatable>, TUpdatable : ValidEntity<TEntity>>(
        val entityName: String,
        @JsonIgnore
        val actionName: String,
        val id: Id,
        val fieldsToUpdate: TUpdatable,
        val dateTime: ZonedDateTime,
        @JsonIgnore
        val events: List<DomainEvent>
    ) : ValidEntity<TEntity>

    class Deletable<TEntity : Entity<TEntity, *, *, *>>(
        val entityName: String,
        @JsonIgnore
        val actionName: String,
        val id: Id,
        @JsonRawValue
        val deletedFields: String,
        val dateTime: ZonedDateTime,
        @JsonIgnore
        val events: List<DomainEvent>
    ) : ValidEntity<TEntity>
}