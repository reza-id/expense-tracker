package dev.cianjur.expense.di

import dev.cianjur.expense.data.repository.CategoryRepositoryImpl
import dev.cianjur.expense.data.repository.ExpenseRepositoryImpl
import dev.cianjur.expense.domain.repository.CategoryRepository
import dev.cianjur.expense.domain.repository.ExpenseRepository
import org.koin.dsl.module

val repositoryModule = module {
    single<ExpenseRepository> {
        ExpenseRepositoryImpl(
            expenseDao = get(),
            expenseImageDao = get(),
            supabaseService = get(),
            imageUtils = get()
        )
    }

    single<CategoryRepository> {
        CategoryRepositoryImpl(
            categoryDao = get(),
            supabaseService = get()
        )
    }
}
