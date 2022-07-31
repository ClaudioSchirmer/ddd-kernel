package br.dev.schirmer.ddd.kernel.domain.models

interface UpdatableRepository<TEntity: Entity<TEntity>> : Repository<TEntity> {
    suspend fun update(updatable: ValidEntity.Updatable<TEntity>)
}