import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant

internal fun LocalDateTime.toEpochMilliseconds(): Long {
    return this.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
}