package dev.cschirmer.ddd.kernel.domain.models

interface UpdatableRepository<TEntity: Entity<TEntity>> : Repository<TEntity> {
    fun update(updatable: ValidEntity.Updatable<TEntity>)
}