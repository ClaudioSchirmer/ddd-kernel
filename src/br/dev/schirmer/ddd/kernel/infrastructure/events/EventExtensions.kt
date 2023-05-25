package br.dev.schirmer.ddd.kernel.infrastructure.events

import br.dev.schirmer.ddd.kernel.domain.events.Event
import br.dev.schirmer.ddd.kernel.domain.events.EventType
import br.dev.schirmer.ddd.kernel.domain.models.Context
import br.dev.schirmer.ddd.kernel.infrastructure.log.Export
import br.dev.schirmer.ddd.kernel.infrastructure.log.Header
import br.dev.schirmer.ddd.kernel.infrastructure.log.Log
import br.dev.schirmer.utils.kotlin.json.JsonUtils.toJson
import org.slf4j.LoggerFactory
import java.time.ZonedDateTime

fun List<Event>.publish(context: Context) = forEach { it.publish(context) }

fun Event.publish(context: Context) {

    val json = Export(
        header = Header(
            threadId = context.id,
            className = this.className,
            action = null,
            actionName = null,
            eventType = this.eventType,
            dateTime = ZonedDateTime.now()
        ),
        data = Log(
            message = this.message,
            values = this.values,
            exception = this.exception
        )
    ).toJson()

    val eventType = when (this@publish.eventType) {
        EventType.AUDIT -> "Audit"
        EventType.DEBUG -> "Debug"
        EventType.LOG -> "Log"
        EventType.ERROR -> "Error"
        EventType.WARNING -> "Warning"
        else -> "Desconhecido"
    }

    with(LoggerFactory.getLogger("Kernel.$eventType")) {
        when (this@publish.eventType) {
            EventType.AUDIT -> info(json)
            EventType.DEBUG -> debug(json)
            EventType.LOG -> info(json)
            EventType.WARNING -> warn(json)
            EventType.ERROR -> error(json)
            else -> error(json)
        }
    }
}