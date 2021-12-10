package dev.cschirmer.ddd.kernel.application.translation

import dev.cschirmer.ddd.kernel.application.exception.ApplicationException

class TranslationNotFoundException(message: String) : ApplicationException(message)