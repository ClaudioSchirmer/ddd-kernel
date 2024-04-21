package br.dev.schirmer.ddd.kernel.domain.models

import br.dev.schirmer.ddd.kernel.application.configuration.AppContext
import br.dev.schirmer.ddd.kernel.domain.events.DomainEvent
import br.dev.schirmer.ddd.kernel.domain.events.EventType
import br.dev.schirmer.ddd.kernel.domain.valueobjects.EntityMode
import br.dev.schirmer.ddd.kernel.domain.valueobjects.Id
import br.dev.schirmer.ddd.kernel.infrastructure.validentity.publish
import kotlinx.coroutines.runBlocking
import java.util.UUID
import kotlin.test.Test

class EntityTest {

    @EntityModes(EntityMode.DELETE, EntityMode.UPDATE, EntityMode.INSERT)
    data class EntityForTest(
        val a: String = "A"
    ) : Entity<EntityForTest, Nothing, EntityForTest, EntityForTest>() {
        override suspend fun buildRules(actionName: String, service: Nothing?): Rules = Rules {
            ifInsert {
                DomainEvent(
                    eventType = EventType.WARNING,
                    className = this@EntityForTest::class.simpleName!!,
                    message = "DomainEvent on Insert",
                    values = this@EntityForTest
                ).register()
            }
            ifUpdate {
                id = Id(UUID.randomUUID())
                DomainEvent(
                    eventType = EventType.WARNING,
                    className = this@EntityForTest::class.simpleName!!,
                    message = "DomainEvent on Update",
                    values = this@EntityForTest
                ).register()
            }
            ifDelete {
                id = Id(UUID.randomUUID())
                DomainEvent(
                    eventType = EventType.WARNING,
                    className = this@EntityForTest::class.simpleName!!,
                    message = "DomainEvent on Delete",
                    values = this@EntityForTest
                ).register()
            }
        }
    }

    @Test
    fun `Validar Entidade`() = runBlocking {
        val a = EntityForTest()
        println("----")
        val i = a.getInsertable()
        i.publish(AppContext(UUID.randomUUID()))
        println("----")
        val u = a.getUpdatable()
        u.publish(AppContext(UUID.randomUUID()))
        println("----")
        val d= a.getDeletable()
        d.publish(AppContext(UUID.randomUUID()))
        println("----")
        println(a)
    }
}