package dev.cschirmer.ddd.kernel.application.exception

import dev.cschirmer.ddd.kernel.domain.notifications.NotificationContext

class ApplicationNotificationContextException(val notificationContext: List<NotificationContext>) :
    ApplicationException()