package dev.cianjur.expense.presentation.expenses.add.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.cianjur.expense.domain.model.Category
import dev.cianjur.expense.ui.components.CategoryItem
import dev.cianjur.expense.ui.components.EmptyStateView

@Composable
fun CategorySelector(
    categories: List<Category>,
    selectedCategoryId: String?,
    onCategorySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (categories.isEmpty()) {
        EmptyStateView(
            title = "No Categories",
            message = "Categories need to be created first.",
            icon = Icons.Default.Category,
            modifier = modifier.padding(vertical = 16.dp)
        )
    } else {
        LazyRow(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { category ->
                CategoryItem(
                    category = category,
                    isSelected = category.id == selectedCategoryId,
                    onClick = { onCategorySelected(category.id) }
                )
            }
        }
    }
}
