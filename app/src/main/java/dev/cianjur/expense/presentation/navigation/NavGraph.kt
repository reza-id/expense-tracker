package dev.cianjur.expense.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import dev.cianjur.expense.presentation.authentication.signin.SignInScreen
import dev.cianjur.expense.presentation.authentication.signup.SignUpScreen
import dev.cianjur.expense.presentation.categories.CategoryScreen
import dev.cianjur.expense.presentation.dashboard.DashboardScreen
import dev.cianjur.expense.presentation.expenses.add.AddExpenseScreen
import dev.cianjur.expense.presentation.expenses.detail.ExpenseDetailScreen
import dev.cianjur.expense.presentation.expenses.list.ExpenseListScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.SignIn.route,
        modifier = modifier
    ) {
        composable(route = Screen.SignIn.route) {
            SignInScreen(navController)
        }
        composable(route = Screen.SignUp.route) {
            SignUpScreen(navController)
        }
        dashboardScreen(navController)
        expenseListScreen(navController)
        addExpenseScreen(navController)
        expenseDetailScreen(navController)
        categoryScreen(navController)
    }
}

fun NavGraphBuilder.dashboardScreen(navController: NavHostController) {
    composable(route = Screen.Dashboard.route) {
        DashboardScreen(
            onNavigateToAddExpense = {
                navController.navigate(Screen.AddExpense.route)
            },
            onNavigateToExpenseDetail = { expenseId ->
                navController.navigate(Screen.ExpenseDetail.createRoute(expenseId))
            }
        )
    }
}

fun NavGraphBuilder.expenseListScreen(navController: NavHostController) {
    composable(route = Screen.ExpenseList.route) {
        ExpenseListScreen(
            onNavigateToAddExpense = {
                navController.navigate(Screen.AddExpense.route)
            },
            onNavigateToExpenseDetail = { expenseId ->
                navController.navigate(Screen.ExpenseDetail.createRoute(expenseId))
            }
        )
    }
}

fun NavGraphBuilder.addExpenseScreen(navController: NavHostController) {
    composable(route = Screen.AddExpense.route) {
        AddExpenseScreen(
            onExpenseSaved = {
                navController.popBackStack()
            },
            onNavigateBack = {
                navController.popBackStack()
            }
        )
    }
}

fun NavGraphBuilder.expenseDetailScreen(navController: NavHostController) {
    composable(
        route = Screen.ExpenseDetail.route,
        arguments = listOf(
            navArgument(Screen.ExpenseDetail.ARG_EXPENSE_ID) {
                type = NavType.StringType
            }
        )
    ) { backStackEntry ->
        val expenseId = backStackEntry.arguments?.getString(Screen.ExpenseDetail.ARG_EXPENSE_ID) ?: ""
        ExpenseDetailScreen(
            expenseId = expenseId,
            onNavigateBack = {
                navController.popBackStack()
            },
            onExpenseDeleted = {
                navController.popBackStack()
            }
        )
    }
}

fun NavGraphBuilder.categoryScreen(navController: NavHostController) {
    composable(route = Screen.Categories.route) {
        CategoryScreen()
    }
}
