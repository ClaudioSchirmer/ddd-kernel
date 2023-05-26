package br.dev.schirmer.ddd.kernel.infrastructure.log

import br.dev.schirmer.ddd.kernel.domain.events.EventType
import java.time.ZonedDateTime
import java.util.*

data class Header(
    val threadId: UUID,
    val className: String,
    val action: String?,
    val actionName: String?,
    val eventType: EventType?,
    val dateTime: ZonedDateTime
)
