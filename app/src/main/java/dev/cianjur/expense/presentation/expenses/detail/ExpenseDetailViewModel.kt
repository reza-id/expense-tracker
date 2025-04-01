package dev.cianjur.expense.presentation.expenses.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.cianjur.expense.domain.model.Category
import dev.cianjur.expense.domain.model.Expense
import dev.cianjur.expense.domain.model.ExpenseImage
import dev.cianjur.expense.domain.repository.CategoryRepository
import dev.cianjur.expense.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

class ExpenseDetailViewModel(
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _expenseId = MutableStateFlow<String?>(null)
    private val _expense = MutableStateFlow<Expense?>(null)
    private val _category = MutableStateFlow<Category?>(null)
    private val _isLoading = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)
    private val _expenseDeleted = MutableSharedFlow<Unit>()

    val uiState: StateFlow<ExpenseDetailUiState> = MutableStateFlow(
        ExpenseDetailUiState(
            expense = _expense.value,
            category = _category.value,
            isLoading = _isLoading.value,
            error = _error.value
        )
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ExpenseDetailUiState()
    )

    val expenseDeleted: SharedFlow<Unit> = _expenseDeleted.asSharedFlow()

    fun loadExpense(expenseId: String) {
        _expenseId.value = expenseId
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val expense = expenseRepository.getExpenseById(expenseId)
                _expense.value = expense

                expense?.let {
                    val category = categoryRepository.getCategoryById(it.categoryId)
                    _category.value = category
                }

                _error.value = null
            } catch (e: Exception) {
                _error.value = "Error loading expense: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addImage(imageFile: File) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _expenseId.value?.let { expenseId ->
                    val expenseImage = expenseRepository.addExpenseImage(expenseId, imageFile)

                    // Update expense with new image
                    _expense.value = _expense.value?.let { currentExpense ->
                        currentExpense.copy(
                            images = currentExpense.images + expenseImage
                        )
                    }

                    // Sync changes
                    expenseRepository.syncExpenseImages()
                }
            } catch (e: Exception) {
                _error.value = "Error adding image: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun removeImage(imageId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                expenseRepository.deleteExpenseImage(imageId)

                // Update expense by removing image
                _expense.value = _expense.value?.let { currentExpense ->
                    currentExpense.copy(
                        images = currentExpense.images.filter { it.id != imageId }
                    )
                }

                // Sync changes
                expenseRepository.syncExpenseImages()
            } catch (e: Exception) {
                _error.value = "Error removing image: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteExpense() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _expenseId.value?.let { expenseId ->
                    expenseRepository.deleteExpense(expenseId)
                    _expenseDeleted.emit(Unit)
                }
            } catch (e: Exception) {
                _error.value = "Error deleting expense: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}

data class ExpenseDetailUiState(
    val expense: Expense? = null,
    val category: Category? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
)
