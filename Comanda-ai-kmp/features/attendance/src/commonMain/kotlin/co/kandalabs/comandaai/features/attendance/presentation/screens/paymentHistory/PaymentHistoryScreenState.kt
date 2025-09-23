package co.kandalabs.comandaai.features.attendance.presentation.screens.paymentHistory

import co.kandalabs.comandaai.features.attendance.domain.models.model.PartialPayment
import co.kandalabs.comandaai.features.attendance.domain.models.enum.PaymentMethod
import kotlinx.datetime.*

data class PaymentHistoryScreenState(
    val isLoading: Boolean = false,
    val payments: List<PartialPayment> = emptyList(),

    // Applied filters (used for API calls)
    val appliedPaymentMethod: PaymentMethod? = null,
    val appliedConsiderPreviousDay: Boolean = false,
    val appliedStartHour: Int = 0,
    val appliedStartMinute: Int = 0,
    val appliedEndHour: Int = 23,
    val appliedEndMinute: Int = 59,

    // Temporary filters (UI state before applying)
    val tempPaymentMethod: PaymentMethod? = null,
    val tempConsiderPreviousDay: Boolean = false,
    val tempStartHour: Int = 0,
    val tempStartMinute: Int = 0,
    val tempEndHour: Int = 23,
    val tempEndMinute: Int = 59,

    val totalAmount: Long = 0L,
    val totalAmountFormatted: String = "R$ 0,00",
    val isFiltersExpanded: Boolean = false,
    val showStartTimePicker: Boolean = false,
    val showEndTimePicker: Boolean = false,
    val error: Throwable? = null
) {
    val startDate: Long
        get() = if (appliedConsiderPreviousDay) getYesterdayStart() else getTodayStart()

    val endDate: Long
        get() = getTodayEnd()

    val startDateWithHour: Long
        get() = {
            val baseDate = if (appliedConsiderPreviousDay) getYesterday() else getToday()
            baseDate.atTime(appliedStartHour, appliedStartMinute, 0, 0)
                .toInstant(TimeZone.currentSystemDefault())
                .toEpochMilliseconds()
        }()

    val endDateWithHour: Long
        get() = {
            val today = getToday()
            today.atTime(appliedEndHour, appliedEndMinute, 59, 999_000_000)
                .toInstant(TimeZone.currentSystemDefault())
                .toEpochMilliseconds()
        }()

    val hasUnappliedChanges: Boolean
        get() = tempPaymentMethod != appliedPaymentMethod ||
                tempConsiderPreviousDay != appliedConsiderPreviousDay ||
                tempStartHour != appliedStartHour ||
                tempStartMinute != appliedStartMinute ||
                tempEndHour != appliedEndHour ||
                tempEndMinute != appliedEndMinute
}

private fun getToday(): LocalDate {
    val now = Clock.System.now()
    return now.toLocalDateTime(TimeZone.currentSystemDefault()).date
}

private fun getYesterday(): LocalDate {
    val now = Clock.System.now()
    return now.toLocalDateTime(TimeZone.currentSystemDefault()).date.minus(1, DateTimeUnit.DAY)
}

private fun getTodayStart(): Long {
    return getToday().atTime(0, 0, 0, 0).toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
}

private fun getTodayEnd(): Long {
    return getToday().atTime(23, 59, 59, 999_000_000).toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
}

private fun getYesterdayStart(): Long {
    return getYesterday().atTime(0, 0, 0, 0).toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
}