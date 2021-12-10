package dev.cschirmer.ddd.kernel.application.translation

import dev.cschirmer.ddd.kernel.application.config.ApplicationConfig
import dev.cschirmer.ddd.kernel.application.config.Language
import java.io.File

object Translator {
    private val translations: MutableMap<Language, MutableMap<String, String>> = mutableMapOf()
    private val modules: MutableList<TranslateModule> = mutableListOf()

    private val messages: Map<String, String>
        get() {
            if (translations[ApplicationConfig.language()] == null) {
                readTranslationsFromFile()
            }
            return translations[ApplicationConfig.language()]?.toMap() ?: mapOf()
        }

    fun importModule(translateModule: TranslateModule) {
        modules.add(translateModule)
    }

    fun importModules(translateModules: List<TranslateModule>) {
        modules.addAll(translateModules)
    }

    fun getTranslationByKey(key: String): String = messages[key]
        ?: modules.firstOrNull { it.language == ApplicationConfig.language() && it.translations[key] != null }?.translations?.get(
            key
        )
        ?: throw TranslationNotFoundException("Translation: $key key to ${ApplicationConfig.language()} language.")

    private fun readTranslationsFromFile() {
        val mapTranslations = translations.putIfAbsent(ApplicationConfig.language(), mutableMapOf())
            ?: translations[ApplicationConfig.language()]!!
        this::class.java.classLoader.getResource("${ApplicationConfig.translationsFolder}/${ApplicationConfig.language()}.properties")?.file?.let { file ->
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