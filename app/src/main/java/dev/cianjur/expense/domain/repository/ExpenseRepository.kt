package dev.cianjur.expense.domain.repository

import dev.cianjur.expense.domain.model.Expense
import dev.cianjur.expense.domain.model.ExpenseImage
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import java.io.File

interface ExpenseRepository {
    suspend fun addExpense(expense: Expense): String
    suspend fun updateExpense(expense: Expense)
    suspend fun deleteExpense(expenseId: String)
    suspend fun getExpenseById(expenseId: String): Expense?
    fun getExpenses(): Flow<List<Expense>>
    fun getExpensesByCategory(categoryId: String): Flow<List<Expense>>
    fun getExpensesByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<Expense>>
    suspend fun syncExpenses()

    // Image related operations
    suspend fun addExpenseImage(expenseId: String, imageFile: File): ExpenseImage
    suspend fun deleteExpenseImage(imageId: String)
    suspend fun getExpenseImages(expenseId: String): List<ExpenseImage>
    suspend fun syncExpenseImages()
}
