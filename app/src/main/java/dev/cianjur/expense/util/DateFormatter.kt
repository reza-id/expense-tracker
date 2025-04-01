package dev.cianjur.expense.util

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn
import java.time.format.DateTimeFormatter
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toJavaLocalDateTime

class DateFormatter {
    private val fullDateFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")
    private val shortDateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")
    private val monthYearFormatter = DateTimeFormatter.ofPattern("MMMM yyyy")

    fun formatFullDate(date: LocalDate): String {
        return fullDateFormatter.format(date.toJavaLocalDate())
    }

    fun formatShortDate(date: LocalDate): String {
        return shortDateFormatter.format(date.toJavaLocalDate())
    }

    fun formatMonthYear(date: LocalDate): String {
        return monthYearFormatter.format(date.toJavaLocalDate())
    }

    fun getToday(): LocalDate {
        return Clock.System.todayIn(TimeZone.currentSystemDefault())
    }

    fun getRelativeDateDescription(date: LocalDate): String {
        val today = getToday()
        return when {
            date == today -> "Today"
            date == today.minus(kotlinx.datetime.DatePeriod(days = 1)) -> "Yesterday"
            date == today.plus(kotlinx.datetime.DatePeriod(days = 1)) -> "Tomorrow"
            else -> formatShortDate(date)
        }
    }
}
