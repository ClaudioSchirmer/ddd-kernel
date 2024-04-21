package br.dev.schirmer.ddd.kernel.domain.events

abstract class Event(
    val eventType: EventType,
    val className: String,
    val message: String,
    val values: Any? = null,
    val exception: Throwable? = null
)