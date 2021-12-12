package dev.cschirmer.ddd.kernel.application.pipeline

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class PipelineTest {

    data class Person(val id: Int, val name: String)

    data class GetPersonByIdQuery(val id: Int) : Query<MutableList<Person>>
    data class XYZCommand(val entrada: String) : Command<String>
    class ThrowableCommand : Command<Unit>

    class GetPersonByIdQueryHandler :
        Handler<MutableList<Person>, GetPersonByIdQuery> {
        override fun invoke(request: GetPersonByIdQuery): MutableList<Person> = mutableListOf(
            Person(1, "Claudio"),
            Person(2, "Teste")
        )
    }

    class XYZCommandHandler : Handler<String, XYZCommand> {
        override fun invoke(request: XYZCommand): String = request.entrada
    }

    class ThrowableCommandHandler : Handler<Unit, ThrowableCommand> {
        override fun invoke(request: ThrowableCommand) {
            throw Throwable("Teste de mensagem de erro")
        }
    }

    @Test
    fun pipelineTest() {
        val pipeline = Pipeline()
        //register
        pipeline.registerQueryHandler(GetPersonByIdQueryHandler())
        pipeline.registerCommandHandler(XYZCommandHandler())
        pipeline.registerCommandHandler(ThrowableCommandHandler())

        //command
        pipeline.dispatch(XYZCommand("VALORENVIO")).whenFirst(
            success = {
                println(this)
                assertEquals("VALORENVIO", this, "Valor recebido deveria ser igual ao enviado.")
            },
            failure = {
                println(this)
            },
            exception = {
                println(this)
            }
        )
        pipeline.dispatch(XYZCommand("VALORENVIO")).forEach(
            success = {
                println(this)
                assertEquals("VALORENVIO", this, "Valor recebido deveria ser igual ao enviado.")
            },
            failure = {
                println(this)
            },
            exception = {
                println(this)
            }
        )

        pipeline.dispatch(XYZCommand("VALORENVIO")).forEach { response ->
            response.run {
                ifSuccess {
                    println(this)
                    assertEquals("VALORENVIO", this, "Valor recebido deveria ser igual ao enviado.")
                }
                ifFailure {
                    println(this)
                }
                ifException {
                    println(this)
                }
            }
        }

        //throwable
        pipeline.dispatch(ThrowableCommand()).runWithFirst {
            ifSuccess {
                println(this)
            }
            ifFailure {
                println(this)
            }
            ifException {
                assertNotNull(this, "Deveria executar uma exeção")
                println(this)
            }
        }

        pipeline.dispatch(ThrowableCommand()).forEach { response ->
            response.run {
                ifSuccess {
                    println(this)
                }
                ifFailure {
                    println(this)
                }
                ifException {
                    assertNotNull(this, "Deveria executar uma exeção")
                    println(this)
                }
            }
        }

        //query
        pipeline.dispatch(GetPersonByIdQuery(1)).run {
            ifSuccess {
                println(this)
                assertEquals(2, count(), "Deveria possuir duas pessoas dentro do resultado")
            }
            ifFailure {
                println(this)
            }
            ifException {
                println(this)
            }
        }
    }

}