package br.dev.schirmer.ddd.kernel.application.configuration

import br.dev.schirmer.ddd.kernel.application.translation.KernelPTBRTranslationModule

internal object Configuration : SharedConfiguration {
    override var language: (() -> Language) = { Language.PT_BR }
    override var translationsFolder: String = "translations"
    init {
        importTranslateModule(KernelPTBRTranslationModule)
    }
}