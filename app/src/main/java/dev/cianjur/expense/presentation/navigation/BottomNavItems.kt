package dev.cianjur.expense.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavItem(
    val screen: Screen,
    val title: String,
    val icon: ImageVector
)

object BottomNavItems {
    val items = listOf(
        BottomNavItem(
            screen = Screen.Dashboard,
            title = "Dashboard",
            icon = Icons.Default.Dashboard
        ),
        BottomNavItem(
            screen = Screen.ExpenseList,
            title = "Expenses",
            icon = Icons.AutoMirrored.Filled.List
        ),
        BottomNavItem(
            screen = Screen.Categories,
            title = "Categories",
            icon = Icons.Default.Category
        )
    )
}
