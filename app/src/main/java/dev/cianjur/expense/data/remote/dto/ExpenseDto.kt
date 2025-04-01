package dev.cianjur.expense.data.remote.dto

import dev.cianjur.expense.domain.model.Expense
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toLocalDate
import kotlinx.serialization.Serializable

@Serializable
data class ExpenseDto(
    val id: String,
    val title: String,
    val amount: Double,
    val date: String, // ISO date format
    val category_id: String,
    val notes: String,
    val created_at: String,
    val updated_at: String,
)

fun ExpenseDto.toDomain(): Expense {
    return Expense(
        id = id,
        title = title,
        amount = amount,
        date = date.toLocalDate(),
        categoryId = category_id,
        notes = notes,
        isSync = true,
        createdAt = created_at.toLongTimestamp(),
        updatedAt = updated_at.toLongTimestamp(),
    )
}

fun Expense.toDto(): ExpenseDto {
    return ExpenseDto(
        id = id,
        title = title,
        amount = amount,
        date = date.toString(),
        category_id = categoryId,
        notes = notes,
        created_at = createdAt.toIsoString(),
        updated_at = updatedAt.toIsoString(),
    )
}

// Helper extensions
internal fun String.toLongTimestamp(): Long {
    // Convert ISO timestamp to milliseconds
    return 0L // Implement actual conversion
}

internal fun Long.toIsoString(): String {
    // Convert milliseconds to ISO timestamp
    return "" // Implement actual conversion
}
