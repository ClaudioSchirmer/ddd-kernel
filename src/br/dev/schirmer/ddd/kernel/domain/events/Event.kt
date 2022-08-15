package br.dev.schirmer.ddd.kernel.domain.events

import com.fasterxml.jackson.annotation.JsonIgnore

abstract class Event(
    @JsonIgnore
    val eventType: EventType,
    val className: String,
    val message: String,
    val values: Any? = null,
    val exception: Throwable? = null
)