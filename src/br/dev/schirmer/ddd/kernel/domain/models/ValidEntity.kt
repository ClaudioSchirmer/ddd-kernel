package br.dev.schirmer.ddd.kernel.domain.models

import br.dev.schirmer.ddd.kernel.domain.valueobjects.Id
import kotlinx.coroutines.runBlocking
import java.util.ServiceConfigurationError

sealed interface ValidEntity<TEntity> {
    class Insertable<TEntity>(val entity: TEntity) : ValidEntity<TEntity>
	class Updatable<TEntity>(val entity: TEntity) : ValidEntity<TEntity>
	class Deletable<TEntity> (val id: Id) : ValidEntity<TEntity>
}

class ServiceXYZ: Service<XYZ> {
    fun x() {

    }
}
data class XYZ(
    val x: Id,
    val y: String
) : Entity<XYZ, ServiceXYZ, XYZ, XYZ.Updatable>() {

    class Updatable(
        val y: String
    ) : ValidEntity<XYZ>

    override val updatable: Boolean
        get() = true

    override val updatableValidEntity: ValidEntity.Updatable<Updatable>
        get() = ValidEntity.Updatable(Updatable(y))

    init {
        x.addToValidate(::x.name)
    }
}

fun main() {
    runBlocking {
        val insertable = XYZ("Claudio", "Guedes").getInsertable(null)
        println(insertable.entity.x)
    }
}