package br.dev.schirmer.ddd.kernel.domain.models

import br.dev.schirmer.ddd.kernel.domain.valueobjects.Id

interface InsertableRepository<TEntity : Entity<TEntity, *, *, TInsertable, *>, TInsertable : ValidEntity<TEntity>> :
    Repository<TEntity> {
    suspend fun insert(insertable: ValidEntity.Insertable<TEntity, TInsertable>) : Id
    suspend fun publish(insertable: ValidEntity.Insertable<TEntity, TInsertable>)
}