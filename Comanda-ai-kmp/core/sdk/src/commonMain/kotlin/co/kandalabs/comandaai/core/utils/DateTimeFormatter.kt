package co.kandalabs.comandaai.core.utils

import kotlinx.datetime.LocalDateTime

object DateTimeFormatter {

    /**
     * Formats a LocalDateTime to Brazilian date/time format: "dd/MM/yyyy às HH:mm"
     */
    fun formatDateTime(localDateTime: LocalDateTime): String {
        val date = localDateTime.date
        val time = localDateTime.time

        val formattedDate = "${date.dayOfMonth.toString().padStart(2, '0')}/${date.monthNumber.toString().padStart(2, '0')}/${date.year}"
        val formattedTime = "${time.hour.toString().padStart(2, '0')}:${time.minute.toString().padStart(2, '0')}"

        return "$formattedDate às $formattedTime"
    }

    /**
     * Parses an ISO datetime string and formats it to Brazilian format
     * Falls back to the original string if parsing fails
     */
    fun formatDateTime(dateTimeString: String): String {
        return try {
            val localDateTime = LocalDateTime.parse(dateTimeString.replace("Z", ""))
            formatDateTime(localDateTime)
        } catch (e: Exception) {
            dateTimeString
        }
    }
}