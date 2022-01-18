package dev.cschirmer.ddd.kernel.application.configuration

import dev.cschirmer.ddd.kernel.domain.notifications.Notification
import dev.cschirmer.ddd.kernel.domain.valueobjects.EnumValueObject

enum class Language(override val value: Int) : EnumValueObject<Int> {
    UNKNOWN(0),
    PT_BR(1),
    ENG(2),
    ES(3),
    FR(4);

    override val unknownEnumNotification: Notification by lazy { InvalidLanguageDomainNotification() }
}