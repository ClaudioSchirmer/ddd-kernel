package br.dev.schirmer.ddd.kernel.domain.events

class DomainEvent(
    eventType: EventType,
    className: String,
    message: String,
    values: Any? = null,
    exception: Throwable? = null
) : Event(eventType, className, message, values, exception)