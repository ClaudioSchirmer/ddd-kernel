package br.dev.schirmer.ddd.kernel.application.translation

import br.dev.schirmer.ddd.kernel.application.configuration.Configuration
import br.dev.schirmer.ddd.kernel.application.configuration.Language
import java.io.File

internal object Translator {
    private val translations: MutableMap<Language, MutableMap<String, String>> = mutableMapOf()
    private val modules: MutableList<TranslateModule> = mutableListOf()

    private val messages: Map<String, String>
        get() {
            if (translations[Configuration.language()] == null) {
                readTranslationsFromFile()
            }
            return translations[Configuration.language()]?.toMap() ?: mapOf()
        }

    fun importModule(translateModule: TranslateModule) {
        modules.add(translateModule)
    }

    fun importModules(translateModules: List<TranslateModule>) {
        modules.addAll(translateModules)
    }

    fun getTranslationByKey(key: String): String = messages[key]
        ?: modules.firstOrNull { it.language == Configuration.language() && it.translations[key] != null }?.translations?.get(
            key
        )
        ?: throw TranslationNotFoundException("Translation: $key key to ${Configuration.language()} language.")

    private fun readTranslationsFromFile() {
        val mapTranslations = translations.putIfAbsent(Configuration.language(), mutableMapOf())
            ?: translations[Configuration.language()]!!
        this::class.java.classLoader.getResource("${Configuration.translationsFolder}/${Configuration.language()}.properties")
            ?.toURI()?.path?.let { file ->
            File(file).useLines { lines ->
                lines.forEach { line ->
                    if (!line.contains("#")) {
                        val item = line.split("=")
                        if (item.count() == 2) {
                            mapTranslations[item[0]] = item[1]
                        }
                    }
                }
            }
        }
    }
}