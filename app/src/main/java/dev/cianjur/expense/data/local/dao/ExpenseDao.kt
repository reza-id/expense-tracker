package dev.cianjur.expense.data.local.dao

import androidx.room.*
import dev.cianjur.expense.data.local.entity.ExpenseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity)

    @Update
    suspend fun updateExpense(expense: ExpenseEntity)

    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity)

    @Query("DELETE FROM expenses WHERE id = :expenseId")
    suspend fun deleteExpenseById(expenseId: String)

    @Query("SELECT * FROM expenses WHERE id = :expenseId")
    suspend fun getExpenseById(expenseId: String): ExpenseEntity?

    @Query("SELECT * FROM expenses ORDER BY date DESC")
    fun getExpenses(): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE categoryId = :categoryId ORDER BY date DESC")
    fun getExpensesByCategory(categoryId: String): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getExpensesByDateRange(startDate: String, endDate: String): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE isSync = 0")
    suspend fun getUnsyncedExpenses(): List<ExpenseEntity>

    @Query("UPDATE expenses SET isSync = 1 WHERE id = :expenseId")
    suspend fun markExpenseAsSynced(expenseId: String)
}
