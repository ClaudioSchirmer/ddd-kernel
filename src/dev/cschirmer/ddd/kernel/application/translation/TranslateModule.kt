package dev.cschirmer.ddd.kernel.application.translation

import dev.cschirmer.ddd.kernel.application.config.Language

interface TranslateModule {
    val language: Language
    val translations: Map<String, String>
}