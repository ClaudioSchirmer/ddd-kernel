package br.dev.schirmer.ddd.kernel.domain.exception

import br.dev.schirmer.ddd.kernel.domain.notifications.NotificationContext

class DomainNotificationContextException(val notificationContext: List<NotificationContext>) : DomainException()