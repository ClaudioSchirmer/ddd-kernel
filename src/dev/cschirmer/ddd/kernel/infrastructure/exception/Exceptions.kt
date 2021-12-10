package dev.cschirmer.ddd.kernel.infrastructure.exception

import dev.cschirmer.ddd.kernel.domain.notifications.NotificationContext

class InfrastructureNotificationException(val notificationContext: List<NotificationContext>) :
	InfrastructureException()