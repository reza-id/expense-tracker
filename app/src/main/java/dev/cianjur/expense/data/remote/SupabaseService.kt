package dev.cianjur.expense.data.remote

import dev.cianjur.expense.data.remote.dto.CategoryDto
import dev.cianjur.expense.data.remote.dto.ExpenseDto
import dev.cianjur.expense.data.remote.dto.ExpenseImageDto
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import java.io.File

class SupabaseService(
    private val supabaseUrl: String,
    private val supabaseKey: String,
) {
    private val client = createSupabaseClient(
        supabaseUrl = supabaseUrl,
        supabaseKey = supabaseKey,
    ) {
        install(Postgrest)
        install(Auth)
        install(Storage)
    }

    // Expense operations
    suspend fun getExpenses(): List<ExpenseDto> {
        return client.postgrest.from("expenses")
            .select()
            .decodeList()
    }

    suspend fun getExpenseById(id: String): ExpenseDto? {
        return client.postgrest.from("expenses")
            .select {
                filter {
                    eq("id", id)
                }
                limit(1)
            }
            .decodeSingleOrNull()
    }

    suspend fun createExpense(expense: ExpenseDto): ExpenseDto {
        return client.postgrest.from("expenses")
            .insert(expense)
            .decodeSingle()
    }

    suspend fun updateExpense(id: String, expense: ExpenseDto): ExpenseDto {
        return client.postgrest.from("expenses")
            .update(expense) {
                filter {
                    eq("id", id)
                }
            }
            .decodeSingle()
    }

    suspend fun deleteExpense(id: String) {
        client.postgrest.from("expenses")
            .delete {
                filter {
                    eq("id", id)
                }
            }
    }

    // Category operations
    suspend fun getCategories(): List<CategoryDto> {
        return client.postgrest.from("categories")
            .select()
            .decodeList()
    }

    suspend fun getCategoryById(id: String): CategoryDto? {
        return client.postgrest.from("categories")
            .select {
                filter {
                    eq("id", id)
                }
                limit(1)
            }
            .decodeSingleOrNull()
    }

    suspend fun createCategory(category: CategoryDto): CategoryDto {
        return client.postgrest.from("categories")
            .insert(category)
            .decodeSingle()
    }

    suspend fun updateCategory(id: String, category: CategoryDto): CategoryDto {
        return client.postgrest.from("categories")
            .update(category) {
                filter {
                    eq("id", id)
                }
            }
            .decodeSingle()
    }

    suspend fun deleteCategory(id: String) {
        client.postgrest.from("categories")
            .delete {
                filter {
                    eq("id", id)
                }
            }
    }

    // Expense Image operations
    suspend fun getExpenseImages(expenseId: String): List<ExpenseImageDto> {
        return client.postgrest.from("expense_images")
            .select {
                filter {
                    eq("expense_id", expenseId)
                }
            }
            .decodeList()
    }

    suspend fun uploadExpenseImage(expenseId: String, file: File): String {
        val bucketName = "expense_images"
        val path = "$expenseId/${file.name}"

        client.storage.from(bucketName)
            .upload(path = path, data = file.readBytes()) {
                upsert = true
            }

        return "${supabaseUrl}/storage/v1/object/public/$bucketName/$path"
    }

    suspend fun createExpenseImageRecord(imageDto: ExpenseImageDto): ExpenseImageDto {
        return client.postgrest.from("expense_images")
            .insert(imageDto)
            .decodeSingle()
    }

    suspend fun deleteExpenseImage(id: String) {
        // First, get the image info to know the path
        val image = client.postgrest.from("expense_images")
            .select {
                filter {
                    eq("id", id)
                }
                limit(1)
            }
            .decodeSingleOrNull<ExpenseImageDto>()

        image?.let {
            // Extract path from URL
            val urlPath = it.image_url.substringAfterLast("/storage/v1/object/public/")
            val parts = urlPath.split("/", limit = 2)
            if (parts.size == 2) {
                val bucketName = parts[0]
                val objectPath = parts[1]

                // Delete from storage
                client.storage.from(bucketName)
                    .delete(objectPath)
            }
        }

        // Delete record
        client.postgrest.from("expense_images")
            .delete {
                filter {
                    eq("id", id)
                }
            }
    }
}
