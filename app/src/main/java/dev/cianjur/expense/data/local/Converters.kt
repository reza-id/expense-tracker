package dev.cianjur.expense.data.local

import androidx.room.TypeConverter
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toLocalDate

class Converters {
    @TypeConverter
    fun fromLocalDate(value: LocalDate): String {
        return value.toString()
    }

    @TypeConverter
    fun toLocalDate(value: String): LocalDate {
        return value.toLocalDate()
    }
}
