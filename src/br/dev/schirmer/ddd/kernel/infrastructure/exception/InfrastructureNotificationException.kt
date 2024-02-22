package br.dev.schirmer.ddd.kernel.infrastructure.exception

import br.dev.schirmer.ddd.kernel.domain.notifications.NotificationContext

class InfrastructureNotificationException(val notificationContext: List<NotificationContext>) :
	InfrastructureException()