package dev.cianjur.expense

import android.app.Application
import dev.cianjur.expense.di.appModule
import dev.cianjur.expense.di.networkModule
import dev.cianjur.expense.di.repositoryModule
import dev.cianjur.expense.di.viewModelModule
import dev.cianjur.expense.domain.repository.CategoryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class ExpenseTrackerApp : Application() {
    private val categoryRepository: CategoryRepository by inject()

    override fun onCreate() {
        super.onCreate()

        // Initialize Koin
        startKoin {
            androidContext(this@ExpenseTrackerApp)
            modules(
                listOf(
                    appModule,
                    networkModule,
                    repositoryModule,
                    viewModelModule,
                )
            )
        }

        // Create default categories
        CoroutineScope(Dispatchers.IO).launch {
            categoryRepository.createDefaultCategories()
        }
    }
}
