package br.dev.schirmer.ddd.kernel.infrastructure.validentity

import br.dev.schirmer.ddd.kernel.domain.models.Entity
import br.dev.schirmer.ddd.kernel.domain.models.ValidEntity
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.text.SimpleDateFormat

inline fun <reified TEntity : Entity<TEntity, *, TInsertable, *>, TInsertable : ValidEntity<TEntity>> ValidEntity.Insertable<TEntity, TInsertable>.publish() {
    jacksonObjectMapper().apply {
        registerKotlinModule()
        registerModule(JavaTimeModule())
        setDateFormat(SimpleDateFormat("yyyy-MM-dd HH:mm a z"))
    }.writeValueAsString(this).let(::println)
}

inline fun <reified TEntity : Entity<TEntity, *, *, TUpdatable>, TUpdatable : ValidEntity<TEntity>> ValidEntity.Updatable<TEntity, TUpdatable>.publish() {
    jacksonObjectMapper().apply {
        registerKotlinModule()
        registerModule(JavaTimeModule())
        setDateFormat(SimpleDateFormat("yyyy-MM-dd HH:mm a z"))
    }.writeValueAsString(this).let(::println)
}

inline fun <reified TEntity : Entity<TEntity, *, *, *>> ValidEntity.Deletable<TEntity>.publish() {
    jacksonObjectMapper().apply {
        registerKotlinModule()
        registerModule(JavaTimeModule())
        setDateFormat(SimpleDateFormat("yyyy-MM-dd HH:mm a z"))
    }.writeValueAsString(this).let(::println)
}