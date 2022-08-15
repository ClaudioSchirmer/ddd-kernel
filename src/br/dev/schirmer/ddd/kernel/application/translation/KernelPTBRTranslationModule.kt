package br.dev.schirmer.ddd.kernel.application.translation

import br.dev.schirmer.ddd.kernel.application.configuration.Language

object KernelPTBRTranslationModule : TranslateModule {
    override val language: Language = Language.PT_BR
    override val translations: Map<String, String> = mapOf(
        /* Kernel.domain */
        "UnableToUpdateWithoutIDNotification" to "Impossível efetuar a atualização do registro sem a chave primária.",
        "UnableToDeleteWithoutIDNotification" to "Impossível efetuar a exclusão do registro sem a chave primária.",
        "UnableToInsertWithIDNotification" to "Impossível efetuar a inclusão de um registro com a chave primária informada.",
        "EntityAlreadyAddedNotification" to "Entidade já foi adicionada.",
        "EntityDoesNotExistNotification" to "Entidade não existe.",
        "InvalidTransactionModeNotification" to "Modo da transação é inválido.",
        "InvalidIDUUIDNotification" to "Chave primária do registro é inválida.",
        "InvalidAggregateItemStatusNotification" to "Situação do aggregado é inválida.",
        "EntityIsNotActiveNotification" to "Entidade não está ativa.",
        "InsertNotAllowedNotification" to "Inclusão não permtida.",
        "UpdateNotAllowedNotification" to "Atualização não permitida.",
        "DeleteNotAllowedNotification" to "Exclusão não permitida",

        /* Kernel.domain.vo */
        "TransactionMode.UNKNOWN" to "Desconhecido",
        "TransactionMode.DISPLAY" to "Consulta",
        "TransactionMode.INSERT" to "Inserir",
        "TransactionMode.UPDATE" to "Atualizar",
        "TransactionMode.DELETE" to "Excluir",
        "AggregateItemStatus.UNKNOWN" to "Desconhecido",
        "AggregateItemStatus.CONSTRUCTOR" to "Construtor",
        "AggregateItemStatus.ADDED" to "Adicionado",
        "AggregateItemStatus.CHANGED" to "Alterado",
        "AggregateItemStatus.REMOVED" to "Removido",
        "Id" to "Chave primária",

        /* Kernel.domain.events */
        "EventType.UNKNOWN" to "Desconhecido",
        "EventType.LOG" to "Log",
        "EventType.AUDIT" to "Auditoria",
        "EventType.DEBUG" to "Debug",

        /* Kernel.application */
        "InvalidLanguageDomainNotification" to "Idioma não é válido.",
        "SQLExceptionNotification" to "Registro não pode ser incluído, alterado ou excluído.",
        "Pipeline" to "Fluxo de dados",

        /* Kernel.application.enum */
        "Language.UNKNOWN" to "Desconhecido",
        "Language.PT_BR" to "Português",
        "Language.ENG" to "Inglês",
        "Language.ES" to "Espanhol",
        "Language.FR" to "Francês",
        )
}