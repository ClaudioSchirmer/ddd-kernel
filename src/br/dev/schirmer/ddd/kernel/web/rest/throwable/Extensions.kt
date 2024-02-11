package br.dev.schirmer.ddd.kernel.web.rest.throwable

import br.dev.schirmer.ddd.kernel.web.rest.Response
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory

suspend fun Throwable.toResponse(httpStatus: Int, httpDescription: String): Response = coroutineScope {
    launch(Job()) {
        LoggerFactory.getLogger(this::class.java).error(this@toResponse.message)
    }
    return@coroutineScope Response(status = httpStatus, description = httpDescription)
}

suspend fun Throwable.toBadRequestResponse() = this.toResponse(400, "Bad Request")

suspend fun Throwable.toNotFoundResponse() = this.toResponse(404, "Not Found")

suspend fun Throwable.toInternalServerErrorResponse() = this.toResponse(500, "Internal Server Error")
