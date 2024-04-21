package br.dev.schirmer.ddd.kernel.domain.notifications

class NotificationContext(
	val context: String
) {
	private var notificationsInContext: MutableList<NotificationMessage> = mutableListOf()
	val notifications get() = notificationsInContext.toList()

	fun addNotification(notificationMessage: NotificationMessage) {
		notificationsInContext.add(notificationMessage)
	}

	fun changeFieldName(notificationMessage: NotificationMessage, newFieldName: String) {
		notificationsInContext.remove(notificationMessage)
		addNotification(
			NotificationMessage(
				fieldName = newFieldName,
				fieldValue = notificationMessage.fieldValue,
				funName = notificationMessage.funName,
				exception = notificationMessage.exception,
				notification = notificationMessage.notification
			)
		)
	}

	fun changeFieldName(originalFieldName: String, newFieldName: String) {
		notificationsInContext.forEach { notificationMessage ->
			if (notificationMessage.fieldName == originalFieldName) {
				changeFieldName(notificationMessage, newFieldName)
			}
		}
	}

	fun clearNotifications() {
		notificationsInContext.clear()
	}

	fun copy(nContext: String? = null) = if (nContext == null) {
		NotificationContext(context).apply {
			this@NotificationContext.notificationsInContext = notificationsInContext.toMutableList()
		}
	} else {
		NotificationContext(nContext).apply {
			this@NotificationContext.notificationsInContext = notificationsInContext.toMutableList()
		}
	}

	override fun toString(): String {
		return "NotificationContext(context=$context, notifications=${notificationsInContext})"
	}
}