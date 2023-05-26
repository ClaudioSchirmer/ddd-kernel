package br.dev.schirmer.ddd.kernel.infrastructure.validentity


import br.dev.schirmer.ddd.kernel.domain.events.EventType
import br.dev.schirmer.ddd.kernel.domain.models.Context
import br.dev.schirmer.ddd.kernel.domain.models.Entity
import br.dev.schirmer.ddd.kernel.domain.models.ValidEntity
import br.dev.schirmer.ddd.kernel.infrastructure.events.publish
import br.dev.schirmer.ddd.kernel.infrastructure.log.Data
import br.dev.schirmer.ddd.kernel.infrastructure.log.Export
import br.dev.schirmer.ddd.kernel.infrastructure.log.Header
import br.dev.schirmer.utils.kotlin.json.JsonUtils.toClass
import br.dev.schirmer.utils.kotlin.json.JsonUtils.toJson
import org.slf4j.LoggerFactory


inline fun <reified TEntity : Entity<TEntity, *, TInsertable, *>, TInsertable : ValidEntity<TEntity>> ValidEntity.Insertable<TEntity, TInsertable>.publish(
    context: Context
) {
    with(LoggerFactory.getLogger("Kernel.Audit")) {
        info(
            Export(
                header = Header(
                    threadId = context.id,
                    action = "Insert",
                    className = this@publish.entityName,
                    actionName = this@publish.actionName,
                    eventType = EventType.AUDIT,
                    dateTime = this@publish.dateTime
                ),
                data = Data(
                    id = this@publish.id?.uuid,
                    fields = this@publish.fieldsToInsert
                )
            ).toJson()
        )
    }
    this.events.publish(context)
}

inline fun <reified TEntity : Entity<TEntity, *, *, TUpdatable>, TUpdatable : ValidEntity<TEntity>> ValidEntity.Updatable<TEntity, TUpdatable>.publish(
    context: Context
) {
    with(LoggerFactory.getLogger("Kernel.Audit")) {
        info(
            Export(
                header = Header(
                    threadId = context.id,
                    action = "Update",
                    className = this@publish.entityName,
                    actionName = this@publish.actionName,
                    eventType = EventType.AUDIT,
                    dateTime = this@publish.dateTime
                ),
                data = Data(
                    id = this@publish.id.uuid,
                    fields = this@publish.fieldsToUpdate
                )
            ).toJson()
        )
    }
    this.events.publish(context)
}

inline fun <reified TEntity : Entity<TEntity, *, *, *>> ValidEntity.Deletable<TEntity>.publish(
    context: Context
) {
    with(LoggerFactory.getLogger("Kernel.Audit")) {
        info(
            Export(
                header = Header(
                    threadId = context.id,
                    action = "Delete",
                    className = this@publish.entityName,
                    actionName = this@publish.actionName,
                    eventType = EventType.AUDIT,
                    dateTime = this@publish.dateTime
                ),
                data = Data(
                    id = this@publish.id.uuid,
                    fields = this@publish.deletedFields.toClass()
                )
            ).toJson()
        )
    }
    this.events.publish(context)
}