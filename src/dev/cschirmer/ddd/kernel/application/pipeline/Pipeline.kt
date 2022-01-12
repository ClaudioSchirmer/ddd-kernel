package dev.cschirmer.ddd.kernel.application.pipeline

import dev.cschirmer.ddd.kernel.application.exception.ApplicationNotificationContextException
import dev.cschirmer.ddd.kernel.application.translation.toNotificationContextDTO
import dev.cschirmer.ddd.kernel.domain.exception.DomainNotificationContextException
import dev.cschirmer.ddd.kernel.domain.notifications.NotificationContext
import dev.cschirmer.ddd.kernel.domain.notifications.NotificationMessage
import dev.cschirmer.ddd.kernel.infrastructure.exception.InfrastructureNotificationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.slf4j.event.Level
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

    suspend fun <TResult> dispatch(query: Query<TResult>): Result<TResult> = tryExecute {
        Result.Success(
                queryHandlers[query::class.qualifiedName]?.invoke(
                        query
                ) as TResult
        )
    }

    suspend fun <TResult> dispatch(command: Command<TResult>): List<Result<TResult>> =
            commandHandlers[command::class.qualifiedName]?.map {
                tryExecute {
                    Result.Success(it.invoke(command) as TResult)
                }
            } ?: emptyList()

    private suspend fun <TResult> tryExecute(handler: (suspend () -> Result<TResult>)): Result<TResult> = try {
        handler()
    } catch (e: DomainNotificationContextException) {
        e.notificationContext.writeLogs()
        Result.Failure(e.notificationContext.toNotificationContextDTO())
    } catch (e: ApplicationNotificationContextException) {
        e.notificationContext.writeLogs()
        Result.Failure(e.notificationContext.toNotificationContextDTO())
    } catch (e: InfrastructureNotificationException) {
        e.notificationContext.writeLogs()
        Result.Failure(e.notificationContext.toNotificationContextDTO())
    } catch (e: SQLException) {
        val notificationContext = NotificationContext(this::class.simpleName.toString()).apply {
            addNotification(
                    NotificationMessage(
                            notification = SQLExceptionNotification()
                    )
            )
        }
        writeLog(Level.ERROR, e.toString())
        println("posterior")
        Result.Failure(listOf(notificationContext).toNotificationContextDTO())
    } catch (e: Throwable) {
        writeLog(Level.ERROR, e.toString())
        println("posterior")
        Result.Exception(e)
    }

    private suspend fun List<NotificationContext>.writeLogs() {
        if (any { context -> context.notifications.any { message -> message.exception != null } }) {
            writeLog(Level.ERROR, toString())
        } else {
            writeLog(Level.INFO, toString())
        }
    }

    private suspend fun writeLog(level: Level, text: String) = coroutineScope {
        launch(Job()) {
            with(LoggerFactory.getLogger(this@Pipeline::class.java)) {
                when (level) {
                    Level.DEBUG -> debug(text)
                    Level.ERROR -> error(text)
                    Level.INFO -> info(text)
                    Level.TRACE -> trace(text)
                    Level.WARN -> warn(text)
                }
            }
        }
    }
}