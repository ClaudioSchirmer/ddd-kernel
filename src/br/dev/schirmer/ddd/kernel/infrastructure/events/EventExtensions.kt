package br.dev.schirmer.ddd.kernel.infrastructure.events

import br.dev.schirmer.ddd.kernel.application.configuration.Context
import br.dev.schirmer.ddd.kernel.domain.events.Event
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.slf4j.LoggerFactory
import java.text.SimpleDateFormat

fun List<Event>.publish(context: Context) = forEach { it.publish(context) }

fun Event.publish(context: Context) {
    jacksonObjectMapper().apply {
        registerKotlinModule()
        registerModule(JavaTimeModule())
        setDateFormat(SimpleDateFormat("yyyy-MM-dd HH:mm a z"))
    }.writeValueAsString(this).let { text ->
        with(LoggerFactory.getLogger(this::class.java)) {
            debug("{\"threadId\":\"${context.id}\",\"EventType\":\"${this@publish.eventType.value}\",\"data\":$text}")
        }
    }
}