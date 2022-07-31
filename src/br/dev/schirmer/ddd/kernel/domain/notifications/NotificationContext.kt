package br.dev.schirmer.ddd.kernel.domain.notifications

class NotificationContext(
	val context: String
) {
	private var notificationsInContext: MutableList<NotificationMessage> = mutableListOf()
	val notifications get() = notificationsInContext.toList()

	fun addNotification(notificationMessage: NotificationMessage) {
		notificationsInContext.add(notificationMessage)
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