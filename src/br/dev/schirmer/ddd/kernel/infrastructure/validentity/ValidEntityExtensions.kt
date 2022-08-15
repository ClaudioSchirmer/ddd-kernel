package br.dev.schirmer.ddd.kernel.infrastructure.validentity

import br.dev.schirmer.ddd.kernel.application.configuration.Context
import br.dev.schirmer.ddd.kernel.domain.models.Entity
import br.dev.schirmer.ddd.kernel.domain.models.ValidEntity
import br.dev.schirmer.ddd.kernel.infrastructure.events.publish
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.slf4j.LoggerFactory
import java.text.SimpleDateFormat

inline fun <reified TEntity : Entity<TEntity, *, TInsertable, *>, TInsertable : ValidEntity<TEntity>> ValidEntity.Insertable<TEntity, TInsertable>.publish(
    context: Context
) {
    jacksonObjectMapper().apply {
        registerKotlinModule()
        registerModule(JavaTimeModule())
        setDateFormat(SimpleDateFormat("yyyy-MM-dd HH:mm a z"))
    }.writeValueAsString(this).let { text ->
        with(LoggerFactory.getLogger(this::class.java)) {
            debug("{\"threadId\":\"${context.id}\",\"EventType\":\"AUDIT\",\"Action\":\"INSERT\",\"data\":$text}")
        }
    }
    this.events.publish(context)
}

inline fun <reified TEntity : Entity<TEntity, *, *, TUpdatable>, TUpdatable : ValidEntity<TEntity>> ValidEntity.Updatable<TEntity, TUpdatable>.publish(
    context: Context
) {
    jacksonObjectMapper().apply {
        registerKotlinModule()
        registerModule(JavaTimeModule())
        setDateFormat(SimpleDateFormat("yyyy-MM-dd HH:mm a z"))
    }.writeValueAsString(this).let { text ->
        with(LoggerFactory.getLogger(this::class.java)) {
            debug("{\"threadId\":\"${context.id}\",\"EventType\":\"AUDIT\",\"action\":\"UPDATE\",\"data\":$text}")
        }
    }
    this.events.publish(context)
}

inline fun <reified TEntity : Entity<TEntity, *, *, *>> ValidEntity.Deletable<TEntity>.publish(
    context: Context
) {
    jacksonObjectMapper().apply {
        registerKotlinModule()
        registerModule(JavaTimeModule())
        setDateFormat(SimpleDateFormat("yyyy-MM-dd HH:mm a z"))
    }.writeValueAsString(this).let { text ->
        with(LoggerFactory.getLogger(this::class.java)) {
            debug("{\"threadId\":\"${context.id}\",\"EventType\":\"AUDIT\",\"action\":\"DELETE\",\"data\":$text}")
        }
    }
    this.events.publish(context)
}