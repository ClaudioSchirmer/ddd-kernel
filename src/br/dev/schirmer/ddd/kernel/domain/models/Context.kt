package br.dev.schirmer.ddd.kernel.domain.models

import java.util.UUID

interface Context {
    val id: UUID
}