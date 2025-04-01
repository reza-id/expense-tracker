package dev.cianjur.expense.presentation.expenses.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.cianjur.expense.domain.model.Category
import dev.cianjur.expense.domain.model.Expense
import dev.cianjur.expense.domain.repository.CategoryRepository
import dev.cianjur.expense.domain.repository.ExpenseRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

class ExpenseListViewModel(
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository,
) : ViewModel() {

    private val _selectedCategoryId = MutableStateFlow<String?>(null)
    private val _searchQuery = MutableStateFlow("")
    private val _filterStartDate = MutableStateFlow<LocalDate?>(null)
    private val _filterEndDate = MutableStateFlow<LocalDate?>(null)
    private val _isLoading = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)

    private val categories = categoryRepository.getCategories()
        .catch { e ->
            _error.value = "Error loading categories: ${e.message}"
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    private val expensesFlow = combine(
        _selectedCategoryId,
        _filterStartDate,
        _filterEndDate
    ) { categoryId, startDate, endDate ->
        Triple(categoryId, startDate, endDate)
    }.flatMapLatest { (categoryId, startDate, endDate) ->
        when {
            categoryId != null -> expenseRepository.getExpensesByCategory(categoryId)
            startDate != null && endDate != null -> expenseRepository.getExpensesByDateRange(startDate, endDate)
            else -> expenseRepository.getExpenses()
        }
    }.catch { e ->
        _error.value = "Error loading expenses: ${e.message}"
        emit(emptyList())
    }

    val uiState: StateFlow<ExpenseListUiState> = combine(
        expensesFlow,
        categories,
        _searchQuery,
        _isLoading,
        _error
    ) { expenses, categories, searchQuery, isLoading, error ->
        val filteredExpenses = if (searchQuery.isEmpty()) {
            expenses
        } else {
            expenses.filter { it.title.contains(searchQuery, ignoreCase = true) }
        }

        ExpenseListUiState(
            expenses = filteredExpenses,
            categories = categories,
            selectedCategoryId = _selectedCategoryId.value,
            isLoading = isLoading,
            error = error
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ExpenseListUiState()
    )

    fun loadExpenses() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                expenseRepository.syncExpenses()
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Error synchronizing expenses: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setSelectedCategory(categoryId: String?) {
        _selectedCategoryId.value = categoryId
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setDateRange(startDate: LocalDate, endDate: LocalDate) {
        _filterStartDate.value = startDate
        _filterEndDate.value = endDate
    }

    fun clearFilters() {
        _selectedCategoryId.value = null
        _searchQuery.value = ""
        _filterStartDate.value = null
        _filterEndDate.value = null
    }

    fun deleteExpense(expenseId: String) {
        viewModelScope.launch {
            try {
                expenseRepository.deleteExpense(expenseId)
            } catch (e: Exception) {
                _error.value = "Error deleting expense: ${e.message}"
            }
        }
    }
}

data class ExpenseListUiState(
    val expenses: List<Expense> = emptyList(),
    val categories: List<Category> = emptyList(),
    val selectedCategoryId: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
