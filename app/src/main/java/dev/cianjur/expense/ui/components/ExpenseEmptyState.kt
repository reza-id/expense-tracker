package dev.cianjur.expense.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ExpenseEmptyState(
    onAddExpense: () -> Unit,
    modifier: Modifier = Modifier,
) {
    EmptyStateView(
        title = "No Expenses Yet",
        message = "Start tracking your expenses by adding your first expense.",
        icon = Icons.Default.ReceiptLong,
        actionLabel = "Add Expense",
        onAction = onAddExpense,
        modifier = modifier,
    )
}
