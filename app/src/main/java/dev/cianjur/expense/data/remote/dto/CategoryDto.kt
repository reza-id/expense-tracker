package dev.cianjur.expense.data.remote.dto

import dev.cianjur.expense.domain.model.Category
import kotlinx.serialization.Serializable

@Serializable
data class CategoryDto(
    val id: String,
    val name: String,
    val icon: String,
    val color: String,
    val is_default: Boolean,
    val created_at: String,
    val updated_at: String,
    val user_id: String? = null,
)

fun CategoryDto.toDomain(): Category {
    return Category(
        id = id,
        name = name,
        icon = icon,
        color = color,
        isDefault = is_default,
        isSync = true,
        createdAt = created_at.toLongTimestamp(),
        updatedAt = updated_at.toLongTimestamp(),
    )
}

fun Category.toDto(): CategoryDto {
    return CategoryDto(
        id = id,
        name = name,
        icon = icon,
        color = color,
        is_default = isDefault,
        created_at = createdAt.toIsoString(),
        updated_at = updatedAt.toIsoString(),
    )
}
