package br.dev.schirmer.ddd.kernel.domain.valueobjects

import br.dev.schirmer.ddd.kernel.domain.exception.DomainNotificationContextException
import br.dev.schirmer.ddd.kernel.domain.notifications.NotificationContext
import br.dev.schirmer.ddd.kernel.domain.notifications.NotificationMessage
import com.fasterxml.jackson.annotation.JsonIgnore
import java.util.*

@JvmInline
value class Id(
	override val value: String
) : ValueObject<String> {
	constructor(uuid: UUID) : this(uuid.toString())

	@get:JsonIgnore
	val uuid: UUID get() {
		return try {
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

	override suspend fun isValid(fieldName: String?, notificationContext: NotificationContext?): Boolean = try {
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