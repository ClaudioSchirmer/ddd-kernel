package br.dev.schirmer.ddd.kernel.domain.notifications

data class NotificationMessage(
    val fieldName: String? = null,
    val fieldValue: String? = null,
    val funName: String? = null,
    val exception: Throwable? = null,
    val notification: Notification
)