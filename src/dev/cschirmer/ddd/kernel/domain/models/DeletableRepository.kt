package dev.cschirmer.ddd.kernel.domain.models

interface DeletableRepository<TEntity: Entity<TEntity>> : Repository<TEntity> {
    suspend fun delete(deletable: ValidEntity.Deletable<TEntity>)
}