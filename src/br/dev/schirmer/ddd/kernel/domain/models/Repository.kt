package br.dev.schirmer.ddd.kernel.domain.models

import br.dev.schirmer.ddd.kernel.domain.exception.DomainNotificationContextException
import br.dev.schirmer.ddd.kernel.domain.notifications.NotificationContext
import br.dev.schirmer.ddd.kernel.domain.notifications.NotificationMessage
import br.dev.schirmer.ddd.kernel.domain.valueobjects.Id

abstract class Repository<TEntity : Entity<TEntity, *, TInsertable, TUpdatable>, TInsertable : ValidEntity<TEntity>, TUpdatable : ValidEntity<TEntity>>(
    val context: Context
) {

    private val notificationContext = NotificationContext(this::class.simpleName.toString())
    protected abstract suspend fun <T> workUnitHandler(workUnit: WorkUnit<T>): T

    suspend fun insert(
        insertable: ValidEntity.Insertable<TEntity, TInsertable>,
        beforeInsert: (suspend () -> Any)? = null,
        afterInsert: (suspend (Id) -> Unit)? = null
    ): Id {
        val workUnit = WorkUnit {
            val resultBefore = beforeInsert?.invoke()
            val id = insert(insertable, resultBefore)
            afterInsert?.invoke(id)
            publish(insertable)
            id
        }
        return workUnitHandler(workUnit)
    }

    suspend fun update(
        updatable: ValidEntity.Updatable<TEntity, TUpdatable>,
        beforeUpdate: (suspend () -> Any)? = null,
        afterUpdate: (suspend () -> Unit)? = null
    ) {
        val workUnit = WorkUnit {
            val resultBefore = beforeUpdate?.invoke()
            update(updatable, resultBefore)
            afterUpdate?.invoke()
            publish(updatable)
        }
        workUnitHandler(workUnit)
    }

    suspend fun delete(
        deletable: ValidEntity.Deletable<TEntity>,
        beforeDelete: (suspend () -> Any)? = null,
        afterDelete: (suspend () -> Unit)? = null
    ) {
        val workUnit = WorkUnit {
            val resultBefore = beforeDelete?.invoke()
            delete(deletable, resultBefore)
            afterDelete?.invoke()
            publish(deletable)
        }
        workUnitHandler(workUnit)
    }

    suspend fun findById(
        id: Id,
        beforeFindById: (suspend () -> Any)? = null,
        afterFindById: (suspend () -> Unit)? = null
    ): TEntity? {
        val workUnit = WorkUnit {
            val resultBefore = beforeFindById?.invoke()
            val entity = findById(id, resultBefore)
            afterFindById?.invoke()
            entity
        }
        return workUnitHandler(workUnit)
    }

    protected open suspend fun findById(id: Id, beforeFindByIdResult: Any?): TEntity? {
        notify("findById")
        throw Exception()
    }

    protected open suspend fun insert(
        insertable: ValidEntity.Insertable<TEntity, TInsertable>,
        beforeInsertResult: Any?
    ): Id {
        notify("insert")
        throw Exception()
    }

    protected open suspend fun update(updatable: ValidEntity.Updatable<TEntity, TUpdatable>, beforeUpdateResult: Any?) {
        notify("update")
    }

    protected open suspend fun delete(deletable: ValidEntity.Deletable<TEntity>, beforeDeleteResult: Any?) {
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