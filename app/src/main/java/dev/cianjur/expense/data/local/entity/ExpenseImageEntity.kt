package dev.cianjur.expense.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import dev.cianjur.expense.domain.model.ExpenseImage

@Entity(
    tableName = "expense_images",
    foreignKeys = [
        ForeignKey(
            entity = ExpenseEntity::class,
            parentColumns = ["id"],
            childColumns = ["expenseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("expenseId")]
)
data class ExpenseImageEntity(
    @PrimaryKey
    val id: String,
    val expenseId: String,
    val imageUri: String,
    val thumbnailUri: String,
    val isRemote: Boolean,
    val remoteUrl: String,
    val isSync: Boolean,
    val createdAt: Long,
)

fun ExpenseImageEntity.toDomain(): ExpenseImage {
    return ExpenseImage(
        id = id,
        expenseId = expenseId,
        imageUri = imageUri,
        thumbnailUri = thumbnailUri,
        isRemote = isRemote,
        remoteUrl = remoteUrl,
        isSync = isSync,
        createdAt = createdAt,
    )
}

fun ExpenseImage.toEntity(): ExpenseImageEntity {
    return ExpenseImageEntity(
        id = id,
        expenseId = expenseId,
        imageUri = imageUri,
        thumbnailUri = thumbnailUri,
        isRemote = isRemote,
        remoteUrl = remoteUrl,
        isSync = isSync,
        createdAt = createdAt,
    )
}
