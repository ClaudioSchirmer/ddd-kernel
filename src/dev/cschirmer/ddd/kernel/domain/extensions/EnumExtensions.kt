package dev.cschirmer.ddd.kernel.domain.extensions

import dev.cschirmer.ddd.kernel.domain.valueobjects.EnumValueObject


inline fun <reified TEnum: Enum<TEnum>> EnumValueObject.Companion.getByValue(value: Int) : TEnum =
    enumValues<TEnum>().firstOrNull { (it as EnumValueObject<*>).value == value } ?:
    enumValues<TEnum>().first { (it as EnumValueObject<*>).value == 0 }

inline fun <reified TEnum: Enum<TEnum>> EnumValueObject.Companion.getByValue(value: String) : TEnum =
    enumValues<TEnum>().firstOrNull { (it as EnumValueObject<*>).value == value || it.name == value } ?:
    enumValues<TEnum>().first { (it as EnumValueObject<*>).value == "" }
