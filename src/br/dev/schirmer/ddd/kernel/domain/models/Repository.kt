package br.dev.schirmer.ddd.kernel.domain.models

import br.dev.schirmer.ddd.kernel.domain.exception.DomainNotificationContextException
import br.dev.schirmer.ddd.kernel.domain.notifications.NotificationContext
import br.dev.schirmer.ddd.kernel.domain.notifications.NotificationMessage
import br.dev.schirmer.ddd.kernel.domain.valueobjects.Id

abstract class Repository<TEntity : Entity<TEntity, *, TInsertable, TUpdatable>, TInsertable : ValidEntity<TEntity>, TUpdatable : ValidEntity<TEntity>>(val context: Context) {

    private val notificationContext = NotificationContext(this::class.simpleName.toString())
    protected abstract suspend fun <T> workUnitHandler(workUnit: workUnit<T>) : T

    suspend fun insert(
        insertable: ValidEntity.Insertable<TEntity, TInsertable>,
        beforeInsert: (suspend () -> Unit)? = null,
        afterInsert: (suspend (Id) -> Unit)? = null
    ): Id {
        val workUnit = workUnit {
            beforeInsert?.invoke()
            val id = insert(insertable)
            afterInsert?.invoke(id)
            publish(insertable)
            id
        }
        return workUnitHandler(workUnit)
    }

    suspend fun update(
        updatable: ValidEntity.Updatable<TEntity, TUpdatable>,
        beforeUpdate: (suspend () -> Unit)? = null,
        afterUpdate: (suspend () -> Unit)? = null
    ) {
        val workUnit = workUnit {
            beforeUpdate?.invoke()
            update(updatable)
            afterUpdate?.invoke()
            publish(updatable)
        }
        workUnitHandler(workUnit)
    }

    suspend fun delete(
        deletable: ValidEntity.Deletable<TEntity>,
        beforeDelete: (suspend () -> Unit)? = null,
        afterDelete: (suspend () -> Unit)? = null
    ) {
        val workUnit = workUnit {
            beforeDelete?.invoke()
            delete(deletable)
            afterDelete?.invoke()
            publish(deletable)
        }
        workUnitHandler(workUnit)
    }

    suspend fun findById(
        id: Id,
        beforeFindById: (suspend () -> Unit)? = null,
        afterFindById: (suspend () -> Unit)? = null
    ): TEntity? {
        val workUnit = workUnit {
            beforeFindById?.invoke()
            val entity = findById(id)
            afterFindById?.invoke()
            entity
        }
        return workUnitHandler(workUnit)
    }

    protected open suspend fun findById(id: Id): TEntity? {
        notify("findById")
        throw Exception()
    }

    protected open suspend fun insert(insertable: ValidEntity.Insertable<TEntity, TInsertable>): Id {
        notify("insert")
        throw Exception()
    }

    protected open suspend fun update(updatable: ValidEntity.Updatable<TEntity, TUpdatable>) {
        notify("update")
    }

    protected open suspend fun delete(deletable: ValidEntity.Deletable<TEntity>) {
        notify("delete")
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