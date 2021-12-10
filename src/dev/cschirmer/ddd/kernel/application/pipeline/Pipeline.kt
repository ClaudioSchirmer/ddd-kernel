package dev.cschirmer.ddd.kernel.application.pipeline

import dev.cschirmer.ddd.kernel.application.exception.ApplicationNotificationContextException
import dev.cschirmer.ddd.kernel.application.translation.toNotificationContextDTO
import dev.cschirmer.ddd.kernel.domain.exception.DomainNotificationContextException
import dev.cschirmer.ddd.kernel.domain.notifications.NotificationContext
import dev.cschirmer.ddd.kernel.domain.notifications.NotificationMessage
import dev.cschirmer.ddd.kernel.infrastructure.exception.InfrastructureNotificationException
import org.slf4j.LoggerFactory
import java.sql.SQLException

@Suppress("UNCHECKED_CAST", "TYPE_INFERENCE_ONLY_INPUT_TYPES_WARNING")
class Pipeline {

    val queryHandlers = mutableMapOf<String, Handler<*, Query<*>>>()
    val commandHandlers = mutableMapOf<String, MutableList<Handler<*, Command<*>>>>()

    inline fun <reified TResult, reified TQuery : Query<TResult>> registerQueryHandler(handler: Handler<TResult, TQuery>) {
        queryHandlers[TQuery::class.qualifiedName!!] = handler as Handler<*, Query<*>>
    }

    inline fun <reified TResult, reified TCommand : Command<TResult>> registerCommandHandler(handler: Handler<TResult, TCommand>) {
        val commandClass = TCommand::class.qualifiedName!!
        val commandList = commandHandlers.putIfAbsent(commandClass, mutableListOf()) ?: commandHandlers[commandClass]!!
        commandList.add(handler as Handler<*, Command<*>>)
    }

    fun <TResult> dispatch(query: Query<TResult>): Result<TResult> = tryExecute {
        Result.Success(
            queryHandlers[query::class.qualifiedName]?.invoke(
                query
            ) as TResult
        )
    }

    fun <TResult> dispatch(command: Command<TResult>): List<Result<TResult>> =
        commandHandlers[command::class.qualifiedName]?.map {
            tryExecute {
                Result.Success(it.invoke(command) as TResult)
            }
        } ?: emptyList()

    private fun <TResult> tryExecute(handler: (() -> Result<TResult>)): Result<TResult> = try {
        handler()
    } catch (e: DomainNotificationContextException) {
        Result.Failure(e.notificationContext.toNotificationContextDTO())
    } catch (e: ApplicationNotificationContextException) {
        Result.Failure(e.notificationContext.toNotificationContextDTO())
    } catch (e: InfrastructureNotificationException) {
        Result.Failure(e.notificationContext.toNotificationContextDTO())
    } catch (e: SQLException) {
        val notificationContext = NotificationContext(this::class.simpleName.toString()).apply {
            addNotification(
                NotificationMessage(
                    notification = SQLExceptionNotification()
                )
            )
        }
        LoggerFactory.getLogger(this::class.java).error(e.toString())
        Result.Failure(listOf(notificationContext).toNotificationContextDTO())
    } catch (e: Throwable) {
        LoggerFactory.getLogger(this::class.java).error(e.toString())
        Result.Exception(e)
    }
}