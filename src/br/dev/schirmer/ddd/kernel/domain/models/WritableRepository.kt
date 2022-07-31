package br.dev.schirmer.ddd.kernel.domain.models

interface WritableRepository<TEntity: Entity<TEntity>> : InsertableRepository<TEntity>, UpdatableRepository<TEntity>,
    DeletableRepository<TEntity>, Repository<TEntity>