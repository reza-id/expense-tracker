package dev.cianjur.expense.presentation.navigation

sealed class Screen(val route: String) {
    data object Dashboard : Screen("dashboard")
    data object ExpenseList : Screen("expense_list")
    data object AddExpense : Screen("add_expense")
    data object ExpenseDetail : Screen("expense_detail/{expenseId}") {
        const val ARG_EXPENSE_ID = "expenseId"

        fun createRoute(expenseId: String): String {
            return "expense_detail/$expenseId"
        }
    }
    data object Categories : Screen("categories")

    // For bottom nav
    companion object {
        val bottomNavItems = listOf(Dashboard, ExpenseList, Categories)
    }
}
