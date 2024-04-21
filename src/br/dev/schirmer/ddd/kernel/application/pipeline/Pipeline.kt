package br.dev.schirmer.ddd.kernel.application.pipeline

import br.dev.schirmer.ddd.kernel.application.configuration.AppContext
import br.dev.schirmer.ddd.kernel.application.exception.ApplicationNotificationContextException
import br.dev.schirmer.ddd.kernel.application.translation.notificationcontext.toNotificationContextDTO
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

class Pipeline(val diAware: DIAware, var appContext: AppContext? = null) {
    suspend inline fun <TResult, TQuery : Query<TResult>, reified TQueryHandler : Handler<TResult, TQuery>> dispatch(
        query: TQuery,
        appContext: AppContext? = null
    ): Result<TResult> {
        checkContext(query::class.simpleName!!, appContext)?.let {
            return it
        }
        val queryHandler: TQueryHandler by diAware.di.instance(arg = appContext ?: this.appContext!!)
        return tryExecute {
            Result.Success(
                queryHandler.apply {
                    setContext(appContext ?: this@Pipeline.appContext!!)
                }.invoke(query)
            )
        }
    }

    suspend inline fun <TResult, TCommand : Command<TResult>, reified TCommandHandler : List<Handler<TResult, TCommand>>> dispatch(
        command: TCommand,
        appContext: AppContext? = null
    ): List<Result<TResult>> {
        checkContext(command::class.simpleName!!, appContext)?.let {
            return listOf(it)
        }
        val commandHandlers: TCommandHandler by diAware.di.instance(arg = appContext ?: this.appContext!!)
        return commandHandlers.map { commandHandler ->
            tryExecute {
                Result.Success(
                    commandHandler.apply {
                        setContext(appContext ?: this@Pipeline.appContext!!)
                    }.invoke(command)
                )
            }
        }
    }

    suspend fun checkContext(handlerName: String, appContext: AppContext? = null): Result.Failure? {
        if (this.appContext == null && appContext == null) {
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
                    Level.DEBUG -> debug("{\"threadId\":\"${appContext?.id}\",\"data\":\"$text\"}")
                    Level.ERROR -> error("{\"threadId\":\"${appContext?.id}\",\"data\":\"$text\"}")
                    Level.INFO -> info("{\"threadId\":\"${appContext?.id}\",\"data\":\"$text\"}")
                    Level.TRACE -> trace("{\"threadId\":\"${appContext?.id}\",\"data\":\"$text\"}")
                    Level.WARN -> warn("{\"threadId\":\"${appContext?.id}\",\"data\":\"$text\"}")
                }
            }
        }
    }
}