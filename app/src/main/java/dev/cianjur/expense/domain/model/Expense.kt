package dev.cianjur.expense.domain.model

import kotlinx.datetime.LocalDate

data class Expense(
    val id: String = "",
    val title: String,
    val amount: Double,
    val date: LocalDate,
    val categoryId: String,
    val notes: String = "",
    val images: List<ExpenseImage> = emptyList(),
    val isSync: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)
