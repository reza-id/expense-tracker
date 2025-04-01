package dev.cianjur.expense.domain.repository

import dev.cianjur.expense.domain.model.Category
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    suspend fun addCategory(category: Category): String
    suspend fun updateCategory(category: Category)
    suspend fun deleteCategory(categoryId: String)
    suspend fun getCategoryById(categoryId: String): Category?
    fun getCategories(): Flow<List<Category>>
    suspend fun syncCategories()
    suspend fun createDefaultCategories()
}
