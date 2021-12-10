package dev.cschirmer.ddd.kernel.domain.valueobjects

import dev.cschirmer.ddd.kernel.domain.exception.DomainNotificationContextException
import dev.cschirmer.ddd.kernel.domain.notifications.NotificationContext
import dev.cschirmer.ddd.kernel.domain.notifications.NotificationMessage
import java.util.*

data class Id(
	override var value: String
) : ScalarValueObject<String>() {
	constructor(uuid: UUID) : this(uuid.toString())

	val uuid: UUID by lazy {
		try {
			UUID.fromString(value)
		} catch (e: Throwable) {
			val notificationContext = NotificationContext(this::class.simpleName.toString()).apply {
				addNotification(
					NotificationMessage(
						fieldName = "id",
						fieldValue = value,
						notification = InvalidIDUUIDNotification()
					)
				)
			}
			throw DomainNotificationContextException(listOf(notificationContext))
		}
	}

	override fun isValid(fieldName: String?, notificationContext: NotificationContext?): Boolean = try {
		UUID.fromString(value)
		true
	} catch (e: Exception) {
		notificationContext?.addNotification(
			NotificationMessage(
				fieldName = fieldName,
				fieldValue = value,
				notification = InvalidIDUUIDNotification()
			)
		)
		false
	}
}