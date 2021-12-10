package dev.cschirmer.ddd.kernel.application.config

object ApplicationConfig {
	var language: (() -> Language) = { Language.PT_BR }
	var translationsFolder: String = "translations"
}