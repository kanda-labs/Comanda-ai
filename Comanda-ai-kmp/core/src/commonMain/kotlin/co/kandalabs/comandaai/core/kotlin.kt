package co.kandalabs.comandaai.core


public inline fun <reified T> T?.getOrThrow(): T =
    this ?: throw IllegalStateException("${T::class.simpleName} is null")