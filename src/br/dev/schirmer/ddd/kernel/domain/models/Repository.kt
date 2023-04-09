package br.dev.schirmer.ddd.kernel.domain.models

import br.dev.schirmer.ddd.kernel.domain.exception.DomainNotificationContextException
import br.dev.schirmer.ddd.kernel.domain.notifications.NotificationContext
import br.dev.schirmer.ddd.kernel.domain.notifications.NotificationMessage
import br.dev.schirmer.ddd.kernel.domain.valueobjects.Id

abstract class Repository<TEntity : Entity<TEntity, *, TInsertable, TUpdatable>, TInsertable : ValidEntity<TEntity>, TUpdatable : ValidEntity<TEntity>> {

    private val notificationContext = NotificationContext(this::class.simpleName.toString())
    protected abstract suspend fun <T> unitOfWorkHandler(unitOfWork: UnitOfWork<T>) : T

    suspend fun insert(
        insertable: ValidEntity.Insertable<TEntity, TInsertable>,
        beforeInsert: (suspend () -> Unit)? = null,
        afterInsert: (suspend (Id) -> Unit)? = null
    ): Id {
        val unitOfWork = UnitOfWork {
            beforeInsert?.invoke()
            val id = insertData(insertable)
            afterInsert?.invoke(id)
            publish(insertable)
            id
        }
        return unitOfWorkHandler(unitOfWork)
    }

    suspend fun update(
        updatable: ValidEntity.Updatable<TEntity, TUpdatable>,
        beforeUpdate: (suspend () -> Unit)? = null,
        afterUpdate: (suspend () -> Unit)? = null
    ) {
        val unitOfWork = UnitOfWork {
            beforeUpdate?.invoke()
            updateData(updatable)
            afterUpdate?.invoke()
            publish(updatable)
        }
        unitOfWorkHandler(unitOfWork)
    }

    suspend fun delete(
        deletable: ValidEntity.Deletable<TEntity>,
        beforeDelete: (suspend () -> Unit)? = null,
        afterDelete: (suspend () -> Unit)? = null
    ) {
        val unitOfWork = UnitOfWork {
            beforeDelete?.invoke()
            deleteData(deletable)
            afterDelete?.invoke()
            publish(deletable)
        }
        unitOfWorkHandler(unitOfWork)
    }

    suspend fun findById(id: Id): TEntity? {
        val unitOfWork = UnitOfWork {
            findDataById(id)
        }
        return unitOfWorkHandler(unitOfWork)
    }

    protected open suspend fun findDataById(id: Id): TEntity? {
        notify(::findDataById.name)
        throw Exception()
    }

    protected open suspend fun insertData(insertable: ValidEntity.Insertable<TEntity, TInsertable>): Id {
        notify(::insertData.name)
        throw Exception()
    }

    protected open suspend fun updateData(updatable: ValidEntity.Updatable<TEntity, TUpdatable>) {
        notify(::updateData.name)
    }

    protected open suspend fun deleteData(deletable: ValidEntity.Deletable<TEntity>) {
        notify(::deleteData.name)
    }

    protected open suspend fun publish(insertable: ValidEntity.Insertable<TEntity, TInsertable>) {
        notify(funName = "insert.publish")
    }

    protected open suspend fun publish(updatable: ValidEntity.Updatable<TEntity, TUpdatable>) {
        notify(funName = "update.publish")
    }

    protected open suspend fun publish(deletable: ValidEntity.Deletable<TEntity>) {
        notify(funName = "delete.publish")
    }

    private fun notify(funName: String) {
        notificationContext.addNotification(
            NotificationMessage(
                funName = "${this::class.simpleName.toString().trim()}.$funName()",
                notification = RepositoryFunctionNotImplementedNotification()
            )
        )
        throw DomainNotificationContextException(listOf(notificationContext))
    }

    private fun notify(funName: String, actionName: String) {
        notificationContext.addNotification(
            NotificationMessage(
                funName = "${this::class.simpleName.toString().trim()}.$funName.$actionName()",
                notification = RepositoryFunctionNotImplementedNotification()
            )
        )
        throw DomainNotificationContextException(listOf(notificationContext))
    }
}