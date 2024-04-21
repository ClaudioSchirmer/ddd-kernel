package br.dev.schirmer.ddd.kernel.domain.valueobjects

import br.dev.schirmer.ddd.kernel.domain.notifications.DomainNotification

enum class EntityMode(override val value: Int) : EnumValueObject<Int> {
	UNKNOWN(0),
	DISPLAY(1),
	INSERT(2),
	UPDATE(3),
	DELETE(4);

	override val unknownEnumNotification: DomainNotification by lazy { InvalidTransactionModeNotification() }
}