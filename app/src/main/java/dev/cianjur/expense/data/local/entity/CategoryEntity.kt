package dev.cianjur.expense.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.cianjur.expense.domain.model.Category

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val icon: String,
    val color: String,
    val isDefault: Boolean,
    val isSync: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
)

fun CategoryEntity.toDomain(): Category {
    return Category(
        id = id,
        name = name,
        icon = icon,
        color = color,
        isDefault = isDefault,
        isSync = isSync,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}

fun Category.toEntity(): CategoryEntity {
    return CategoryEntity(
        id = id,
        name = name,
        icon = icon,
        color = color,
        isDefault = isDefault,
        isSync = isSync,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}
