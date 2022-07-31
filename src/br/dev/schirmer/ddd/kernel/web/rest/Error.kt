package br.dev.schirmer.ddd.kernel.web.rest

data class Error(
    val context: String,
    val messages: List<ErrorMessage>
)
