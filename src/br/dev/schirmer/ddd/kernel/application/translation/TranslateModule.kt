package br.dev.schirmer.ddd.kernel.application.translation

import br.dev.schirmer.ddd.kernel.application.configuration.Language

interface TranslateModule {
    val language: Language
    val translations: Map<String, String>
}