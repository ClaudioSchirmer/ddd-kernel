package br.dev.schirmer.ddd.kernel.application.pipeline

import br.dev.schirmer.ddd.kernel.application.configuration.Context
import br.dev.schirmer.ddd.kernel.application.exception.ApplicationNotificationContextException
import br.dev.schirmer.ddd.kernel.application.translation.toNotificationContextDTO
import br.dev.schirmer.ddd.kernel.domain.exception.DomainNotificationContextException
import br.dev.schirmer.ddd.kernel.domain.notifications.NotificationContext
import br.dev.schirmer.ddd.kernel.domain.notifications.NotificationMessage
import br.dev.schirmer.ddd.kernel.infrastructure.exception.InfrastructureNotificationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.kodein.di.DIAware
import org.kodein.di.instance
import org.slf4j.LoggerFactory
import org.slf4j.event.Level
import java.sql.SQLException

class Pipeline(val diAware: DIAware, var context: Context? = null) {
    suspend inline fun <TResult, TQuery : Query<TResult>, reified TQueryHandler : Handler<TResult, TQuery>> dispatch(
        query: TQuery,
        context: Context? = null
    ): Result<TResult> {
        checkContext(query::class.simpleName!!, context)?.let {
            return it
        }
        val queryHandler: TQueryHandler by diAware.di.instance(arg = context ?: this.context!!)
        return tryExecute {
            Result.Success(
                queryHandler.apply {
                    setContext(context ?: this@Pipeline.context!!)
                }.invoke(query)
            )
        }
    }

    suspend inline fun <TResult, TCommand : Command<TResult>, reified TCommandHandler : List<Handler<TResult, TCommand>>> dispatch(
        command: TCommand,
        context: Context? = null
    ): List<Result<TResult>> {
        checkContext(command::class.simpleName!!, context)?.let {
            return listOf(it)
        }
        val commandHandlers: TCommandHandler by diAware.di.instance(arg = context ?: this.context!!)
        return commandHandlers.map { commandHandler ->
            tryExecute {
                Result.Success(
                    commandHandler.apply {
                        setContext(context ?: this@Pipeline.context!!)
                    }.invoke(command)
                )
            }
        }
    }

    suspend fun checkContext(handlerName: String, context: Context? = null): Result.Failure? {
        if (this.context == null && context == null) {
            NotificationContext(this::class.simpleName.toString()).apply {
                addNotification(
                    NotificationMessage(
                        fieldName = "context",
                        funName = handlerName,
                        notification = ContextNotIniatializedNotification()
                    )
                )
            }.let {
                with(listOf(it)) {
                    writeLogs()
                    return Result.Failure(toNotificationContextDTO())
                }
            }
        }
        return null
    }

    suspend fun <TResult> tryExecute(handler: (suspend () -> Result<TResult>)): Result<TResult> = try {
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
        val notificationContext = NotificationContext(this::class.simpleName!!).apply {
            addNotification(
                NotificationMessage(
                    notification = SQLExceptionNotification()
                )
            )
        }
        writeLog(Level.ERROR, e.toString())
        Result.Failure(listOf(notificationContext).toNotificationContextDTO())
    } catch (e: Throwable) {
        writeLog(Level.ERROR, e.toString())
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
                    Level.DEBUG -> debug("{\"threadId\":\"${context?.id}\",\"data\":\"$text\"}")
                    Level.ERROR -> error("{\"threadId\":\"${context?.id}\",\"data\":\"$text\"}")
                    Level.INFO -> info("{\"threadId\":\"${context?.id}\",\"data\":\"$text\"}")
                    Level.TRACE -> trace("{\"threadId\":\"${context?.id}\",\"data\":\"$text\"}")
                    Level.WARN -> warn("{\"threadId\":\"${context?.id}\",\"data\":\"$text\"}")
                }
            }
        }
    }
}