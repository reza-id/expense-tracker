package dev.cianjur.expense.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.cianjur.expense.domain.model.Expense
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toLocalDate

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val amount: Double,
    val date: String, // ISO date format
    val categoryId: String,
    val notes: String,
    val isSync: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
)

fun ExpenseEntity.toDomain(images: List<ExpenseImageEntity> = emptyList()): Expense {
    return Expense(
        id = id,
        title = title,
        amount = amount,
        date = date.toLocalDate(),
        categoryId = categoryId,
        notes = notes,
        images = images.map { it.toDomain() },
        isSync = isSync,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}

fun Expense.toEntity(): ExpenseEntity {
    return ExpenseEntity(
        id = id,
        title = title,
        amount = amount,
        date = date.toString(), // ISO date format
        categoryId = categoryId,
        notes = notes,
        isSync = isSync,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}
