package dev.cianjur.expense.presentation.dashboard

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.cianjur.expense.domain.model.Category
import dev.cianjur.expense.domain.model.Expense
import dev.cianjur.expense.domain.repository.CategoryRepository
import dev.cianjur.expense.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn

class DashboardViewModel(
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository,
) : ViewModel() {

    private val _selectedPeriod = MutableStateFlow(Period.MONTH)
    private val _isLoading = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)

    private val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

    private val categories = categoryRepository.getCategories()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val expenses = expenseRepository.getExpenses()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val uiState: StateFlow<DashboardUiState> = combine(
        expenses,
        categories,
        _selectedPeriod,
        _isLoading,
        _error
    ) { expenses, categories, selectedPeriod, isLoading, error ->
        val (startDate, endDate) = getDateRangeForPeriod(selectedPeriod)

        val filteredExpenses = expenses.filter { expense ->
            expense.date >= startDate && expense.date <= endDate
        }

        val totalAmount = filteredExpenses.sumOf { it.amount }

        val expensesByCategory = filteredExpenses.groupBy { it.categoryId }
            .mapValues { (_, expenses) -> expenses.sumOf { it.amount } }

        val categoryStats = categories.mapNotNull { category ->
            val amount = expensesByCategory[category.id] ?: 0.0
            if (amount > 0) {
                CategoryStat(
                    category = category,
                    amount = amount,
                    percentage = if (totalAmount > 0) (amount / totalAmount) * 100 else 0.0
                )
            } else null
        }.sortedByDescending { it.amount }

        val dailyExpenses = filteredExpenses
            .groupBy { it.date }
            .mapValues { (_, expenses) -> expenses.sumOf { it.amount } }
            .toList()
            .sortedBy { it.first }

        DashboardUiState(
            totalAmount = totalAmount,
            categoryStats = categoryStats,
            dailyExpenses = dailyExpenses,
            selectedPeriod = selectedPeriod,
            startDate = startDate,
            endDate = endDate,
            isLoading = isLoading,
            error = error
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState()
    )

    fun setPeriod(period: Period) {
        _selectedPeriod.value = period
    }

    fun syncData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                expenseRepository.syncExpenses()
                categoryRepository.syncCategories()
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Error syncing data: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun getDateRangeForPeriod(period: Period): Pair<LocalDate, LocalDate> {
        return when (period) {
            Period.WEEK -> {
                val startOfWeek = today.minus(DatePeriod(days = today.dayOfWeek.ordinal))
                Pair(startOfWeek, startOfWeek.plus(DatePeriod(days = 6)))
            }

            Period.MONTH -> {
                val startOfMonth = LocalDate(today.year, today.monthNumber, 1)
                val endOfMonth = when (today.monthNumber) {
                    2 -> {
                        // February - check for leap year
                        val lastDay = if (today.year % 4 == 0 && (today.year % 100 != 0 || today.year % 400 == 0)) 29 else 28
                        LocalDate(today.year, 2, lastDay)
                    }

                    4, 6, 9, 11 -> {
                        // 30 days months
                        LocalDate(today.year, today.monthNumber, 30)
                    }

                    else -> {
                        // 31 days months
                        LocalDate(today.year, today.monthNumber, 31)
                    }
                }
                Pair(startOfMonth, endOfMonth)
            }

            Period.YEAR -> {
                val startOfYear = LocalDate(today.year, 1, 1)
                val endOfYear = LocalDate(today.year, 12, 31)
                Pair(startOfYear, endOfYear)
            }

            Period.ALL -> {
                // Use a very old date for start and future date for end
                Pair(LocalDate(2000, 1, 1), LocalDate(2100, 12, 31))
            }
        }
    }
}

enum class Period {
    WEEK, MONTH, YEAR, ALL,
}

data class CategoryStat(
    val category: Category,
    val amount: Double,
    val percentage: Double,
)

data class DashboardUiState(
    val totalAmount: Double = 0.0,
    val categoryStats: List<CategoryStat> = emptyList(),
    val dailyExpenses: List<Pair<LocalDate, Double>> = emptyList(),
    val selectedPeriod: Period = Period.MONTH,
    val startDate: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
    val endDate: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
    val isLoading: Boolean = false,
    val error: String? = null,
)
