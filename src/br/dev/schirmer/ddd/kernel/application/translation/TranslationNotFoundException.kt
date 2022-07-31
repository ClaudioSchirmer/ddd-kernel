package br.dev.schirmer.ddd.kernel.application.translation

import br.dev.schirmer.ddd.kernel.application.exception.ApplicationException

class TranslationNotFoundException(message: String) : ApplicationException(message)