package dev.cschirmer.ddd.kernel.domain.valueobjects

import dev.cschirmer.ddd.kernel.application.translation.getTranslatedMessage
import dev.cschirmer.ddd.kernel.domain.notifications.Notification
import dev.cschirmer.ddd.kernel.domain.notifications.NotificationContext
import dev.cschirmer.ddd.kernel.domain.notifications.NotificationMessage


interface EnumValueObject<TValue : Any> : ValueObject {
	val unknownEnumNotification: Notification
	val value: TValue
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