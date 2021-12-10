package dev.cschirmer.ddd.kernel.domain.models

import dev.cschirmer.ddd.kernel.domain.valueobjects.Id

interface ReadableRepository<TEntity : Entity<TEntity>> : Repository<TEntity> {
    fun findById(id: Id): TEntity?
}