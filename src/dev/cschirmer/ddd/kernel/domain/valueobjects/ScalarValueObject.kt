package dev.cschirmer.ddd.kernel.domain.valueobjects

abstract class ScalarValueObject<TObject: Any> : ValueObject {
    abstract var value: TObject
    protected set

    override fun toString(): String = value.toString()
    override fun equals(other: Any?): Boolean = (other is ScalarValueObject<*>) && (value == other.value)
    override fun hashCode(): Int = value.hashCode()
}