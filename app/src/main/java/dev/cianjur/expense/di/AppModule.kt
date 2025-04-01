package dev.cianjur.expense.di

import dev.cianjur.expense.data.local.ExpenseDatabase
import dev.cianjur.expense.util.ImageUtils
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModule = module {
    // Database
    single { ExpenseDatabase.getInstance(androidContext()) }
    single { get<ExpenseDatabase>().expenseDao() }
    single { get<ExpenseDatabase>().categoryDao() }
    single { get<ExpenseDatabase>().expenseImageDao() }

    // Utils
    single { ImageUtils(androidContext()) }
}
