package br.dev.schirmer.ddd.kernel.domain.models

interface WritableRepository<TEntity : Entity<TEntity, *, TInsertable, TUpdatable>, TInsertable : ValidEntity<TEntity>, TUpdatable : ValidEntity<TEntity>> :
    InsertableRepository<TEntity, TInsertable>,
    UpdatableRepository<TEntity, TUpdatable>,
    DeletableRepository<TEntity>,
    Repository<TEntity>