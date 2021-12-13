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

    class XYZCommandHandlerA : Handler<String, XYZCommand> {
        override fun invoke(request: XYZCommand): String = request.entrada
    }

    class XYZCommandHandlerB : Handler<String, XYZCommand> {
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
        pipeline.registerCommandHandler(XYZCommandHandlerA())
        pipeline.registerCommandHandler(XYZCommandHandlerB())
        pipeline.registerCommandHandler(ThrowableCommandHandler())

        //command
        pipeline.dispatch(XYZCommand("runWithFirst")).withFirstResult {
            ifSuccess {
                println(this)
                assertEquals("runWithFirst", this, "Valor recebido deveria ser igual ao enviado.")
            }
            ifFailure {
                println(this)
            }
            ifException {
                println(this)
            }
        }
        var s: String = pipeline.dispatch(XYZCommand("getFromFirstResult")).getFromFirstResult({ "Claudio" }) {
            ifSuccess {
                return
            }
            ifFailure {
                ""
            }
        }
        println("------>$s")
        assertEquals("getFromFirstResult", s, "Valor recebido deveria ser igual ao enviado.")
        s = pipeline.dispatch(XYZCommand("getFromFirstResult")).getFromFirstResult({ "Claudio" }) {
            ifFailure {
                ""
            }
        }
        println("------>$s")
        assertEquals("Claudio", s, "Valor recebido deveria ser igual ao enviado.")
        pipeline.dispatch(XYZCommand("forEachResult")).forEachResult {
            ifSuccess {
                println(this)
                assertEquals("forEachResult", this, "Valor recebido deveria ser igual ao enviado.")
            }
            ifFailure {
                println(this)
            }
            ifException {
                println(this)
            }
        }
        //throwable
        pipeline.dispatch(ThrowableCommand()).withFirstResult {
            ifSuccess {
                println(this)
            }
        }
        pipeline.dispatch(ThrowableCommand()).forEachResult {
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
        //query
        pipeline.dispatch(GetPersonByIdQuery(1)).withResult {
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
        val y: MutableList<Person> = pipeline.dispatch(GetPersonByIdQuery(1)).getFromResult {
            ifSuccess {
                this
            }
        }!!
        println(y)
    }
}