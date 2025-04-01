package dev.cianjur.expense.data.local.dao

import androidx.room.*
import dev.cianjur.expense.data.local.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity)

    @Update
    suspend fun updateCategory(category: CategoryEntity)

    @Delete
    suspend fun deleteCategory(category: CategoryEntity)

    @Query("DELETE FROM categories WHERE id = :categoryId")
    suspend fun deleteCategoryById(categoryId: String)

    @Query("SELECT * FROM categories WHERE id = :categoryId")
    suspend fun getCategoryById(categoryId: String): CategoryEntity?

    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE isSync = 0")
    suspend fun getUnsyncedCategories(): List<CategoryEntity>

    @Query("UPDATE categories SET isSync = 1 WHERE id = :categoryId")
    suspend fun markCategoryAsSynced(categoryId: String)

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun getCategoryCount(): Int
}
