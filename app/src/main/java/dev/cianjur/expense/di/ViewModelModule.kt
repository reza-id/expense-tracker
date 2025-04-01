package dev.cianjur.expense.di

import dev.cianjur.expense.presentation.expenses.list.ExpenseListViewModel
import dev.cianjur.expense.presentation.expenses.add.AddExpenseViewModel
import dev.cianjur.expense.presentation.expenses.detail.ExpenseDetailViewModel
import dev.cianjur.expense.presentation.dashboard.DashboardViewModel
import dev.cianjur.expense.presentation.categories.CategoryViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val viewModelModule = module {
    viewModelOf(::ExpenseListViewModel)
    viewModelOf(::AddExpenseViewModel)
    viewModelOf(::ExpenseDetailViewModel)
    viewModelOf(::DashboardViewModel)
    viewModelOf(::CategoryViewModel)
}
