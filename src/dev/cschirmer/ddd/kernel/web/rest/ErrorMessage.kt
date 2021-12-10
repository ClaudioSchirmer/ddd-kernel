package dev.cschirmer.ddd.kernel.web.rest

data class ErrorMessage(
    val field: String?,
    val value: String?,
    val message: String,
)
