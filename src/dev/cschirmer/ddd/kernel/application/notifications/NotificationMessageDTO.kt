package dev.cschirmer.ddd.kernel.application.notifications

data class NotificationMessageDTO(
    val fieldName: String? = null,
    val fieldValue: String? = null,
    val funName: String? = null,
    val message: String
)