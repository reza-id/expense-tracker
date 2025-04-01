package dev.cianjur.expense.data.repository

import dev.cianjur.expense.data.local.dao.CategoryDao
import dev.cianjur.expense.data.local.entity.toEntity
import dev.cianjur.expense.data.local.entity.toDomain
import dev.cianjur.expense.data.remote.SupabaseService
import dev.cianjur.expense.data.remote.dto.toDto
import dev.cianjur.expense.data.remote.dto.toDomain
import dev.cianjur.expense.domain.model.Category
import dev.cianjur.expense.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class CategoryRepositoryImpl(
    private val categoryDao: CategoryDao,
    private val supabaseService: SupabaseService,
) : CategoryRepository {

    override suspend fun addCategory(category: Category): String {
        val categoryId = category.id.ifEmpty { UUID.randomUUID().toString() }
        val newCategory = category.copy(id = categoryId)
        categoryDao.insertCategory(newCategory.toEntity())
        return categoryId
    }

    override suspend fun updateCategory(category: Category) {
        categoryDao.updateCategory(category.toEntity())
    }

    override suspend fun deleteCategory(categoryId: String) {
        categoryDao.deleteCategoryById(categoryId)
    }

    override suspend fun getCategoryById(categoryId: String): Category? {
        return categoryDao.getCategoryById(categoryId)?.toDomain()
    }

    override fun getCategories(): Flow<List<Category>> {
        return categoryDao.getCategories().map { it.map { entity -> entity.toDomain() } }
    }

    override suspend fun syncCategories() {
        try {
            // Upload unsynced categories
            val unsyncedCategories = categoryDao.getUnsyncedCategories()
            for (category in unsyncedCategories) {
                try {
                    supabaseService.createCategory(category.toDomain().toDto())
                    categoryDao.markCategoryAsSynced(category.id)
                } catch (e: Exception) {
                    // Log error and continue with next category
                    e.printStackTrace()
                }
            }

            // Get all remote categories and update local database
            val remoteCategories = supabaseService.getCategories()
            for (remoteCategory in remoteCategories) {
                val localCategory = categoryDao.getCategoryById(remoteCategory.id)
                if (localCategory == null) {
                    // New remote category, save locally
                    categoryDao.insertCategory(remoteCategory.toDomain().toEntity())
                } else {
                    // Update only if remote is newer
                    // Implement comparison logic if needed
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Handle network errors
        }
    }

    override suspend fun createDefaultCategories() {
        if (categoryDao.getCategoryCount() == 0) {
            val defaultCategories = listOf(
                Category(
                    id = UUID.randomUUID().toString(),
                    name = "Food & Drink",
                    icon = "restaurant",
                    color = "#4CAF50",
                    isDefault = true
                ),
                Category(
                    id = UUID.randomUUID().toString(),
                    name = "Shopping",
                    icon = "shopping_cart",
                    color = "#2196F3",
                    isDefault = true
                ),
                Category(
                    id = UUID.randomUUID().toString(),
                    name = "Transportation",
                    icon = "directions_car",
                    color = "#FFC107",
                    isDefault = true
                ),
                Category(
                    id = UUID.randomUUID().toString(),
                    name = "Entertainment",
                    icon = "movie",
                    color = "#9C27B0",
                    isDefault = true
                ),
                Category(
                    id = UUID.randomUUID().toString(),
                    name = "Bills & Utilities",
                    icon = "receipt",
                    color = "#F44336",
                    isDefault = true
                ),
                Category(
                    id = UUID.randomUUID().toString(),
                    name = "Health",
                    icon = "local_hospital",
                    color = "#00BCD4",
                    isDefault = true
                ),
                Category(
                    id = UUID.randomUUID().toString(),
                    name = "Other",
                    icon = "more_horiz",
                    color = "#607D8B",
                    isDefault = true
                )
            )

            defaultCategories.forEach { category ->
                categoryDao.insertCategory(category.toEntity())
            }
        }
    }
}
