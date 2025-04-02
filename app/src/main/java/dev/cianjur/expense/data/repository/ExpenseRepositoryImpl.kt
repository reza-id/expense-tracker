package dev.cianjur.expense.data.repository

import android.util.Log
import dev.cianjur.expense.data.local.dao.ExpenseDao
import dev.cianjur.expense.data.local.dao.ExpenseImageDao
import dev.cianjur.expense.data.local.entity.toEntity
import dev.cianjur.expense.data.local.entity.toDomain
import dev.cianjur.expense.data.remote.SupabaseService
import dev.cianjur.expense.data.remote.dto.ExpenseImageDto
import dev.cianjur.expense.data.remote.dto.toDto
import dev.cianjur.expense.data.remote.dto.toDomain
import dev.cianjur.expense.domain.model.Expense
import dev.cianjur.expense.domain.model.ExpenseImage
import dev.cianjur.expense.domain.repository.ExpenseRepository
import dev.cianjur.expense.util.ImageUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import java.io.File
import java.util.UUID

class ExpenseRepositoryImpl(
    private val expenseDao: ExpenseDao,
    private val expenseImageDao: ExpenseImageDao,
    private val supabaseService: SupabaseService,
    private val imageUtils: ImageUtils,
) : ExpenseRepository {

    override suspend fun addExpense(expense: Expense): String {
        val expenseId = expense.id.ifEmpty { UUID.randomUUID().toString() }
        val newExpense = expense.copy(id = expenseId)
        expenseDao.insertExpense(newExpense.toEntity())
        return expenseId
    }

    override suspend fun updateExpense(expense: Expense) {
        expenseDao.updateExpense(expense.toEntity())
    }

    override suspend fun deleteExpense(expenseId: String) {
        expenseDao.deleteExpenseById(expenseId)
        // Remote deletion will be handled during sync
    }

    override suspend fun getExpenseById(expenseId: String): Expense? {
        val expense = expenseDao.getExpenseById(expenseId) ?: return null
        val images = expenseImageDao.getExpenseImagesByExpenseId(expenseId)
        return expense.toDomain(images)
    }

    override fun getExpenses(): Flow<List<Expense>> {
        return expenseDao.getExpenses().map { expenses ->
            expenses.map { expense ->
                val images = expenseImageDao.getExpenseImagesByExpenseId(expense.id)
                expense.toDomain(images)
            }
        }
    }

    override fun getExpensesByCategory(categoryId: String): Flow<List<Expense>> {
        return expenseDao.getExpensesByCategory(categoryId).map { expenses ->
            expenses.map { expense ->
                val images = expenseImageDao.getExpenseImagesByExpenseId(expense.id)
                expense.toDomain(images)
            }
        }
    }

    override fun getExpensesByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<Expense>> {
        return expenseDao.getExpensesByDateRange(startDate.toString(), endDate.toString()).map { expenses ->
            expenses.map { expense ->
                val images = expenseImageDao.getExpenseImagesByExpenseId(expense.id)
                expense.toDomain(images)
            }
        }
    }

    override suspend fun syncExpenses() {
        try {
            // Upload unsynced expenses
            val unsyncedExpenses = expenseDao.getUnsyncedExpenses()
            for (expense in unsyncedExpenses) {
                try {
                    val remoteExpense = supabaseService.createExpense(expense.toDomain().toDto())
                    expenseDao.markExpenseAsSynced(expense.id)
                } catch (e: Exception) {
                    // Log error and continue with next expense
                    Log.e("AGUSSS", "Error syncing data", e)
                    e.printStackTrace()
                }
            }

            // Get all remote expenses and update local database
            val remoteExpenses = supabaseService.getExpenses()
            for (remoteExpense in remoteExpenses) {
                val localExpense = expenseDao.getExpenseById(remoteExpense.id)
                if (localExpense == null) {
                    // New remote expense, save locally
                    expenseDao.insertExpense(remoteExpense.toDomain().toEntity())
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

    override suspend fun addExpenseImage(expenseId: String, imageFile: File): ExpenseImage {
        val imageId = UUID.randomUUID().toString()
        val compressedFile = imageUtils.compressImage(imageFile)
        val thumbnailFile = imageUtils.createThumbnail(imageFile)

        val localUri = imageUtils.saveImageToInternalStorage(compressedFile)
        val thumbnailUri = imageUtils.saveImageToInternalStorage(thumbnailFile)

        val expenseImage = ExpenseImage(
            id = imageId,
            expenseId = expenseId,
            imageUri = localUri,
            thumbnailUri = thumbnailUri,
            isRemote = false,
            remoteUrl = "",
            isSync = false
        )

        expenseImageDao.insertExpenseImage(expenseImage.toEntity())
        return expenseImage
    }

    override suspend fun deleteExpenseImage(imageId: String) {
        expenseImageDao.deleteExpenseImageById(imageId)
    }

    override suspend fun getExpenseImages(expenseId: String): List<ExpenseImage> {
        return expenseImageDao.getExpenseImagesByExpenseId(expenseId).map { it.toDomain() }
    }

    override suspend fun syncExpenseImages() {
        val unsyncedImages = expenseImageDao.getUnsyncedExpenseImages()
        for (image in unsyncedImages) {
            try {
                val imageFile = imageUtils.getFileFromUri(image.imageUri)
                if (imageFile != null) {
                    val remoteUrl = supabaseService.uploadExpenseImage(image.expenseId, imageFile)

                    // Create remote record
                    val imageDto = ExpenseImageDto(
                        id = image.id,
                        expense_id = image.expenseId,
                        image_url = remoteUrl,
                        thumbnail_url = remoteUrl, // Or upload thumbnail separately
                        created_at = image.createdAt.toString()
                    )
                    supabaseService.createExpenseImageRecord(imageDto)

                    // Update local record
                    expenseImageDao.markExpenseImageAsSynced(image.id, remoteUrl)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Handle errors but continue with next image
            }
        }
    }
}
