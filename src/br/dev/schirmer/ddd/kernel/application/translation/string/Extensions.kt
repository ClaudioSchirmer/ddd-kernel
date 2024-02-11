package br.dev.schirmer.ddd.kernel.application.translation.string

import br.dev.schirmer.ddd.kernel.application.translation.Translator


fun String.getTranslatedMessage(): String = Translator.getTranslationByKey(this)
