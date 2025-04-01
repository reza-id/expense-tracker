package dev.cianjur.expense.domain.model

data class Category(
    val id: String = "",
    val name: String,
    val icon: String,
    val color: String,
    val isDefault: Boolean = false,
    val isSync: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)
