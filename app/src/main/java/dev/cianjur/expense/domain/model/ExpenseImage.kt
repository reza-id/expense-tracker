package dev.cianjur.expense.domain.model

data class ExpenseImage(
    val id: String = "",
    val expenseId: String,
    val imageUri: String,
    val thumbnailUri: String = "",
    val isRemote: Boolean = false,
    val remoteUrl: String = "",
    val isSync: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
)
