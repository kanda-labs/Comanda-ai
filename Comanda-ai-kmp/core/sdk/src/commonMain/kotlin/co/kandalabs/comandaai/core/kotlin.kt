package co.kandalabs.comandaai.sdk


public inline fun <reified T> T?.getOrThrow(): T =
    this ?: throw IllegalStateException("${T::class.simpleName} is null")