package kandalabs.commander.core.extensions

public fun <T> T?.getOrThrow(): T =
    this ?: throw IllegalStateException("${this?.javaClass} is null")