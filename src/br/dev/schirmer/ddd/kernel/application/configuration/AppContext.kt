package br.dev.schirmer.ddd.kernel.application.configuration

import br.dev.schirmer.ddd.kernel.domain.models.Context
import java.util.*

open class AppContext(
    override val id: UUID
) : Context