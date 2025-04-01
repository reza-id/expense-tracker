package dev.cianjur.expense.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun CategoryEmptyState(
    onAddCategory: () -> Unit,
    modifier: Modifier = Modifier
) {
    EmptyStateView(
        title = "No Custom Categories",
        message = "Create custom categories to better organize your expenses.",
        icon = Icons.Default.Category,
        actionLabel = "Add Category",
        onAction = onAddCategory,
        modifier = modifier
    )
}
