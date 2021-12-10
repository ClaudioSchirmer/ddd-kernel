package dev.cschirmer.ddd.kernel.application.notifications

data class NotificationContextDTO(
    val context: String,
    val notifications: List<NotificationMessageDTO>
)