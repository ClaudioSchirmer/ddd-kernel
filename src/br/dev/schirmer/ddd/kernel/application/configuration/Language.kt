package br.dev.schirmer.ddd.kernel.application.configuration

import br.dev.schirmer.ddd.kernel.domain.notifications.Notification
import br.dev.schirmer.ddd.kernel.domain.valueobjects.EnumValueObject

enum class Language(override val value: Int) : EnumValueObject<Int> {
    UNKNOWN(0),
    PT_BR(1),
    ENG(2),
    ES(3),
    FR(4);

    override val unknownEnumNotification: Notification by lazy { InvalidLanguageDomainNotification() }
}