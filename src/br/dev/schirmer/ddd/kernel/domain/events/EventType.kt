package br.dev.schirmer.ddd.kernel.domain.events

import br.dev.schirmer.ddd.kernel.domain.notifications.Notification
import br.dev.schirmer.ddd.kernel.domain.valueobjects.EnumValueObject

enum class EventType(override val value: String) : EnumValueObject<String> {
    UNKNOWN("UNKNOWN"),
    LOG("LOG"),
    AUDIT("AUDIT"),
    DEBUG("DEBUG"),
    ERROR("ERROR"),
    WARNING("WARNING");

    override val unknownEnumNotification: Notification by lazy { InvalidEventTypeNotification() }
}