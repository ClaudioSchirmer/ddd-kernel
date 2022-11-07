package br.dev.schirmer.ddd.kernel.domain.models

import br.dev.schirmer.ddd.kernel.domain.valueobjects.EntityMode

@Target(AnnotationTarget.CLASS)
annotation class EntityModes(vararg val modes: EntityMode)