package dev.cschirmer.ddd.kernel.domain.valueobjects

import dev.cschirmer.ddd.kernel.domain.notifications.DomainNotification

enum class TransactionMode(override val value: Int) : EnumValueObject<Int> {
	UNKNOWN(0),
	DISPLAY(1),
	INSERT(2),
	UPDATE(3),
	DELETE(4);

	override val unknownEnumNotification: DomainNotification by lazy { InvalidTransactionModeNotification() }
}