package br.dev.schirmer.ddd.kernel.domain.valueobjects

import br.dev.schirmer.ddd.kernel.application.translation.string.getTranslatedMessage
import br.dev.schirmer.ddd.kernel.domain.notifications.Notification
import br.dev.schirmer.ddd.kernel.domain.notifications.NotificationContext
import br.dev.schirmer.ddd.kernel.domain.notifications.NotificationMessage


interface EnumValueObject<TValue : Any> : ValueObject<TValue> {
	val unknownEnumNotification: Notification
	override val value: TValue
	val description get() = ("${this::class.simpleName.toString()}.$this").getTranslatedMessage()

	override suspend fun isValid(fieldName: String?, notificationContext: NotificationContext?): Boolean =
		if (((value is String) && (value == "" || value == "UNKNOWN")) || ((value is Int) && value == 0)) {
			notificationContext?.addNotification(
				NotificationMessage(
					fieldName = fieldName,
					notification = unknownEnumNotification
				)
			)
			true
		} else {
			false
		}

	companion object
}