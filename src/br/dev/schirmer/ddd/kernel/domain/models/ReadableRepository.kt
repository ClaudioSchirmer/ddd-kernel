package br.dev.schirmer.ddd.kernel.domain.models

import br.dev.schirmer.ddd.kernel.domain.valueobjects.Id

interface ReadableRepository<TEntity : Entity<TEntity, *, *, *, *>> : Repository<TEntity> {
    suspend fun findById(id: Id): TEntity?
}