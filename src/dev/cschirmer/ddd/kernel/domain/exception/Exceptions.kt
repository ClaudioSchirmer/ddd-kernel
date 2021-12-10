package dev.cschirmer.ddd.kernel.domain.exception

import dev.cschirmer.ddd.kernel.domain.notifications.NotificationContext

class DomainNotificationContextException(val notificationContext: List<NotificationContext>) : DomainException()