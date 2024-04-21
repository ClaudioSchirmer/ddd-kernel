package br.dev.schirmer.ddd.kernel.web.rest

data class Response(
    val status: Int,
    val description: String,
    val errors: List<Error>? = null
)