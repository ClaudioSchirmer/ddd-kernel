package br.dev.schirmer.ddd.kernel.application.pipeline

import br.dev.schirmer.ddd.kernel.application.configuration.Context
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class PipelineTest {

    class MyContext(id: UUID, val abacaxi: String) : Context(id)
    data class Person(val id: Int, val name: String)
    data class GetPersonByIdQuery(val id: Int) : Query<MutableList<Person>>
    data class XYZCommand(val entrada: String) : Command<String>
    class ThrowableCommand : Command<Unit>

    class GetPersonByIdQueryHandler : Handler<MutableList<Person>, GetPersonByIdQuery>() {
        override suspend fun invoke(request: GetPersonByIdQuery): MutableList<Person> = mutableListOf(
                Person(1, "Claudio"),
                Person(2, "Teste")
        )
    }

    class XYZCommandHandlerA : Handler<String, XYZCommand>() {
        override suspend fun invoke(request: XYZCommand): String {
            if (context is MyContext) {
                println((context as MyContext).abacaxi)
            } else {
                println(context.id)
            }
            //throw Throwable("Teste de mensagem de erro")
            return request.entrada
        }
    }

    class XYZCommandHandlerB : Handler<String, XYZCommand>() {
        override suspend fun invoke(request: XYZCommand): String = request.entrada
    }

    class ThrowableCommandHandler : Handler<Unit, ThrowableCommand>() {
        override suspend fun invoke(request: ThrowableCommand) {
            throw Throwable("Teste de mensagem de erro")
        }
    }

    @Test
    fun pipelineTest() {

        val pipeline = Pipeline(MyContext(id = UUID.randomUUID(), abacaxi = "foi"))
        //register
        pipeline.registerQueryHandler(GetPersonByIdQueryHandler())
        pipeline.registerCommandHandler(XYZCommandHandlerA())
        pipeline.registerCommandHandler(XYZCommandHandlerB())
        pipeline.registerCommandHandler(ThrowableCommandHandler())

        runBlocking {
            //command
            pipeline.dispatch(XYZCommand("runWithFirst"))
                    .withFirstIfSuccess({ assertTrue(false, "Deveria ter dado sucesso") }) {
                        println(this)
                        assertEquals("runWithFirst", this, "Valor recebido deveria ser igual ao enviado.")
                    }
            var s: String = pipeline.dispatch(XYZCommand("getFromFirstResult")).getFromFirstResult({ "Claudio" }) {
                ifSuccess {
                    this
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
            pipeline.dispatch(XYZCommand("withFirstIfSuccess")).withFirstIfSuccess {
                s = this
            }
            println("------>$s")
            assertEquals("withFirstIfSuccess", s, "Valor recebido deveria ser igual ao enviado.")
            pipeline.dispatch(XYZCommand("first().ifSuccess")).first().ifSuccess {
                s = this
            }
            println("------>$s")
            assertEquals("first().ifSuccess", s, "Valor recebido deveria ser igual ao enviado.")
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
            }
            println(y)
        }
    }
}