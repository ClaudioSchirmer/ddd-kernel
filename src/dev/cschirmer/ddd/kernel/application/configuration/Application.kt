package dev.cschirmer.ddd.kernel.application.configuration

object Application {
	var language: (() -> Language) = { Language.PT_BR }
	var translationsFolder: String = "translations"
}