package dev.cschirmer.ddd.kernel.domain.models

import dev.cschirmer.ddd.kernel.domain.notifications.DomainNotification

/*ENTITY*/
class UnableToInsertWithIDNotification : DomainNotification
class UnableToUpdateWithoutIDNotification : DomainNotification
class UnableToDeleteWithoutIDNotification : DomainNotification

/*AGGREGATE ROOT*/
class EntityAlreadyAddedNotification : DomainNotification
class EntityDoesNotExistNotification : DomainNotification