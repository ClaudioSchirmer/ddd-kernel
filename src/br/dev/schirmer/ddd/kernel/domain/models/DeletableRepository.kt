package br.dev.schirmer.ddd.kernel.domain.models

interface DeletableRepository<TEntity: Entity<TEntity,*,*,*>> : Repository<TEntity> {
    suspend fun delete(deletable: ValidEntity.Deletable<TEntity>)
    suspend fun publish(deletable: ValidEntity.Deletable<TEntity>)
}