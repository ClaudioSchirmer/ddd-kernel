package br.dev.schirmer.ddd.kernel.infrastructure.validentity

import br.dev.schirmer.ddd.kernel.application.configuration.Context
import br.dev.schirmer.ddd.kernel.domain.models.Entity
import br.dev.schirmer.ddd.kernel.domain.models.ValidEntity
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.slf4j.LoggerFactory
import java.text.SimpleDateFormat

inline fun <reified TEntity : Entity<TEntity, *, TInsertable, *>, TInsertable : ValidEntity<TEntity>> ValidEntity.Insertable<TEntity, TInsertable>.publishAudit(
    context: Context
) {
    jacksonObjectMapper().apply {
        registerKotlinModule()
        registerModule(JavaTimeModule())
        setDateFormat(SimpleDateFormat("yyyy-MM-dd HH:mm a z"))
    }.writeValueAsString(this).let { text ->
        with(LoggerFactory.getLogger(this::class.java)) {
            info("{\"threadId\":\"${context.id}\",\"eventName\":\"Audit\",\"type\":\"Insert\",\"data\":$text}")
        }
    }
}

inline fun <reified TEntity : Entity<TEntity, *, *, TUpdatable>, TUpdatable : ValidEntity<TEntity>> ValidEntity.Updatable<TEntity, TUpdatable>.publishAudit(
    context: Context
) {
    jacksonObjectMapper().apply {
        registerKotlinModule()
        registerModule(JavaTimeModule())
        setDateFormat(SimpleDateFormat("yyyy-MM-dd HH:mm a z"))
    }.writeValueAsString(this).let { text ->
        with(LoggerFactory.getLogger(this::class.java)) {
            info("{\"threadId\":\"${context.id}\",\"eventName\":\"Audit\",\"type\":\"Update\",\"data\":$text}")
        }
    }
}

inline fun <reified TEntity : Entity<TEntity, *, *, *>> ValidEntity.Deletable<TEntity>.publishAudit(
    context: Context
) {
    jacksonObjectMapper().apply {
        registerKotlinModule()
        registerModule(JavaTimeModule())
        setDateFormat(SimpleDateFormat("yyyy-MM-dd HH:mm a z"))
    }.writeValueAsString(this).let { text ->
        with(LoggerFactory.getLogger(this::class.java)) {
            info("{\"threadId\":\"${context.id}\",\"eventName\":\"Audit\",\"type\":\"Delete\",\"data\":$text}")
        }
    }
}