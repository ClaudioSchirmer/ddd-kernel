package br.dev.schirmer.ddd.kernel.application.translation

import br.dev.schirmer.ddd.kernel.application.configuration.Application
import br.dev.schirmer.ddd.kernel.application.configuration.Language
import java.io.File

object Translator {
    private val translations: MutableMap<Language, MutableMap<String, String>> = mutableMapOf()
    private val modules: MutableList<TranslateModule> = mutableListOf()

    private val messages: Map<String, String>
        get() {
            if (translations[Application.language()] == null) {
                readTranslationsFromFile()
            }
            return translations[Application.language()]?.toMap() ?: mapOf()
        }

    fun importModule(translateModule: TranslateModule) {
        modules.add(translateModule)
    }

    fun importModules(translateModules: List<TranslateModule>) {
        modules.addAll(translateModules)
    }

    fun getTranslationByKey(key: String): String = messages[key]
        ?: modules.firstOrNull { it.language == Application.language() && it.translations[key] != null }?.translations?.get(
            key
        )
        ?: throw TranslationNotFoundException("Translation: $key key to ${Application.language()} language.")

    private fun readTranslationsFromFile() {
        val mapTranslations = translations.putIfAbsent(Application.language(), mutableMapOf())
            ?: translations[Application.language()]!!
        this::class.java.classLoader.getResource("${Application.translationsFolder}/${Application.language()}.properties")
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