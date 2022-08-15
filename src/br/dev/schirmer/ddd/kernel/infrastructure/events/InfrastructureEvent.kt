package br.dev.schirmer.ddd.kernel.infrastructure.events

import br.dev.schirmer.ddd.kernel.domain.events.Event
import br.dev.schirmer.ddd.kernel.domain.events.EventType

class InfrastructureEvent(
    eventType: EventType,
    className: String,
    message: String,
    values: Any? = null,
    exception: Throwable? = null
): Event(eventType, className, message, values, exception)