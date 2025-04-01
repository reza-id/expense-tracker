package dev.cianjur.expense.presentation.expenses.add

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
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import java.io.File

class AddExpenseViewModel(
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository,
) : ViewModel() {

    private val _title = MutableStateFlow("")
    private val _amount = MutableStateFlow("")
    private val _date = MutableStateFlow(Clock.System.todayIn(TimeZone.currentSystemDefault()))
    private val _selectedCategoryId = MutableStateFlow<String?>(null)
    private val _notes = MutableStateFlow("")
    private val _images = MutableStateFlow<List<ExpenseImage>>(emptyList())
    private val _isLoading = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)
    private val _expenseSaved = MutableSharedFlow<String>()

    val categories = categoryRepository.getCategories()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val uiState: StateFlow<AddExpenseUiState> = MutableStateFlow(
        AddExpenseUiState(
            title = _title.value,
            amount = _amount.value,
            date = _date.value,
            selectedCategoryId = _selectedCategoryId.value,
            notes = _notes.value,
            images = _images.value,
            isLoading = _isLoading.value,
            error = _error.value
        )
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AddExpenseUiState()
    )

    val expenseSaved: SharedFlow<String> = _expenseSaved.asSharedFlow()

    fun setTitle(title: String) {
        _title.value = title
    }

    fun setAmount(amount: String) {
        _amount.value = amount
    }

    fun setDate(date: LocalDate) {
        _date.value = date
    }

    fun setCategory(categoryId: String) {
        _selectedCategoryId.value = categoryId
    }

    fun setNotes(notes: String) {
        _notes.value = notes
    }

    fun addImage(imageFile: File) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val tempExpenseId = "temp_${System.currentTimeMillis()}"
                val expenseImage = expenseRepository.addExpenseImage(tempExpenseId, imageFile)
                _images.value += expenseImage
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
                _images.value = _images.value.filter { it.id != imageId }
            } catch (e: Exception) {
                _error.value = "Error removing image: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun saveExpense() {
        viewModelScope.launch {
            if (!validateInput()) {
                return@launch
            }

            _isLoading.value = true
            try {
                val amountValue = _amount.value.toDoubleOrNull() ?: 0.0

                val expense = Expense(
                    title = _title.value,
                    amount = amountValue,
                    date = _date.value,
                    categoryId = _selectedCategoryId.value ?: "",
                    notes = _notes.value,
                    images = emptyList() // Images will be associated after saving expense
                )

                val expenseId = expenseRepository.addExpense(expense)

                // Associate images with the new expense
                val updatedImages = mutableListOf<ExpenseImage>()
                for (image in _images.value) {
                    // Update expense ID for each image
                    val updatedImage = image.copy(expenseId = expenseId)
                    expenseRepository.addExpenseImage(expenseId, File(updatedImage.imageUri))
                    updatedImages.add(updatedImage)
                }

                // Sync with remote
                try {
                    expenseRepository.syncExpenses()
                    expenseRepository.syncExpenseImages()
                } catch (e: Exception) {
                    // Log but don't fail on sync error
                    e.printStackTrace()
                }

                _expenseSaved.emit(expenseId)
                resetForm()
            } catch (e: Exception) {
                _error.value = "Error saving expense: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun validateInput(): Boolean {
        return when {
            _title.value.isBlank() -> {
                _error.value = "Title cannot be empty"
                false
            }

            _amount.value.isBlank() || _amount.value.toDoubleOrNull() == null -> {
                _error.value = "Amount must be a valid number"
                false
            }

            _selectedCategoryId.value == null -> {
                _error.value = "Please select a category"
                false
            }

            else -> true
        }
    }

    private fun resetForm() {
        _title.value = ""
        _amount.value = ""
        _date.value = Clock.System.todayIn(TimeZone.currentSystemDefault())
        _selectedCategoryId.value = null
        _notes.value = ""
        _images.value = emptyList()
        _error.value = null
    }
}

data class AddExpenseUiState(
    val title: String = "",
    val amount: String = "",
    val date: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
    val selectedCategoryId: String? = null,
    val notes: String = "",
    val images: List<ExpenseImage> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)
