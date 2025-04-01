package dev.cianjur.expense.data.remote.dto

import dev.cianjur.expense.domain.model.ExpenseImage
import kotlinx.serialization.Serializable

@Serializable
data class ExpenseImageDto(
    val id: String,
    val expense_id: String,
    val image_url: String,
    val thumbnail_url: String,
    val created_at: String
)

fun ExpenseImageDto.toDomain(): ExpenseImage {
    return ExpenseImage(
        id = id,
        expenseId = expense_id,
        imageUri = "",
        thumbnailUri = "",
        isRemote = true,
        remoteUrl = image_url,
        isSync = true,
        createdAt = created_at.toLongTimestamp()
    )
}
