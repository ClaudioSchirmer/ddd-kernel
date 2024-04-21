package br.dev.schirmer.ddd.kernel.application.configuration

import br.dev.schirmer.ddd.kernel.application.translation.TranslateModule
import br.dev.schirmer.ddd.kernel.application.translation.Translator

interface SharedConfiguration {
    var language: (() -> Language)
    var translationsFolder: String

    fun importTranslateModules(translateModules: List<TranslateModule>) = Translator.importModules(translateModules)
    fun importTranslateModule(translateModule: TranslateModule) = Translator.importModule(translateModule)
}