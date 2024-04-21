package br.dev.schirmer.ddd.kernel.application.exception

import br.dev.schirmer.ddd.kernel.domain.notifications.NotificationContext

class ApplicationNotificationContextException(val notificationContext: List<NotificationContext>) :
    ApplicationException()