package dev.cschirmer.ddd.kernel.domain.models

import dev.cschirmer.ddd.kernel.domain.valueobjects.Id

interface InsertableRepository<TEntity : Entity<TEntity>> : Repository<TEntity> {
    fun insert(insertable: ValidEntity.Insertable<TEntity>): Id
}