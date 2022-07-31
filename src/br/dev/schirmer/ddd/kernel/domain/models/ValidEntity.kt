package br.dev.schirmer.ddd.kernel.domain.models

sealed interface ValidEntity<TEntity : Entity<TEntity>> {
	data class Insertable<TEntity: Entity<TEntity>>(val entity: TEntity) : ValidEntity<TEntity>
	data class Updatable<TEntity: Entity<TEntity>>(val entity: TEntity) : ValidEntity<TEntity>
	data class Deletable<TEntity: Entity<TEntity>>(val entity: TEntity) : ValidEntity<TEntity>
}