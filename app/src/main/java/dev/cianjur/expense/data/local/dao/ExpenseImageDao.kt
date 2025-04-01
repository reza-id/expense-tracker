package dev.cianjur.expense.data.local.dao

import androidx.room.*
import dev.cianjur.expense.data.local.entity.ExpenseImageEntity

@Dao
interface ExpenseImageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpenseImage(expenseImage: ExpenseImageEntity)

    @Update
    suspend fun updateExpenseImage(expenseImage: ExpenseImageEntity)

    @Delete
    suspend fun deleteExpenseImage(expenseImage: ExpenseImageEntity)

    @Query("DELETE FROM expense_images WHERE id = :imageId")
    suspend fun deleteExpenseImageById(imageId: String)

    @Query("SELECT * FROM expense_images WHERE expenseId = :expenseId")
    suspend fun getExpenseImagesByExpenseId(expenseId: String): List<ExpenseImageEntity>

    @Query("SELECT * FROM expense_images WHERE isSync = 0")
    suspend fun getUnsyncedExpenseImages(): List<ExpenseImageEntity>

    @Query("UPDATE expense_images SET isSync = 1, remoteUrl = :remoteUrl WHERE id = :imageId")
    suspend fun markExpenseImageAsSynced(imageId: String, remoteUrl: String)
}
