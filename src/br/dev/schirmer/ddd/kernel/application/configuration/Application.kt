package br.dev.schirmer.ddd.kernel.application.configuration

object Application {
	var language: (() -> br.dev.schirmer.ddd.kernel.application.configuration.Language) = { br.dev.schirmer.ddd.kernel.application.configuration.Language.PT_BR }
	var translationsFolder: String = "translations"
}