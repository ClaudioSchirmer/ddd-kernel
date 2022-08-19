package br.dev.schirmer.ddd.kernel.domain.models

import br.dev.schirmer.ddd.kernel.domain.notifications.DomainNotification

/*ENTITY*/
class UnableToInsertWithIDNotification : DomainNotification
class UnableToUpdateWithoutIDNotification : DomainNotification
class UnableToDeleteWithoutIDNotification : DomainNotification

class InsertNotAllowedNotification : DomainNotification
class UpdateNotAllowedNotification : DomainNotification
class DeleteNotAllowedNotification : DomainNotification

/*AGGREGATE ROOT*/
class EntityAlreadyAddedNotification : DomainNotification
class EntityDoesNotExistNotification : DomainNotification