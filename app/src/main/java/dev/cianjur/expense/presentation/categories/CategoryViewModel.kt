package dev.cianjur.expense.presentation.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.cianjur.expense.domain.model.Category
import dev.cianjur.expense.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class CategoryViewModel(
    private val categoryRepository: CategoryRepository,
) : ViewModel() {

    private val _name = MutableStateFlow("")
    private val _icon = MutableStateFlow("")
    private val _color = MutableStateFlow("#000000")
    private val _isLoading = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)
    private val _editingCategory = MutableStateFlow<Category?>(null)
    private val _categorySaved = MutableSharedFlow<Unit>()

    val categories = categoryRepository.getCategories()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val uiState: StateFlow<CategoryUiState> = MutableStateFlow(
        CategoryUiState(
            name = _name.value,
            icon = _icon.value,
            color = _color.value,
            isEditing = _editingCategory.value != null,
            isLoading = _isLoading.value,
            error = _error.value
        )
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CategoryUiState()
    )

    val categorySaved: SharedFlow<Unit> = _categorySaved.asSharedFlow()

    fun setName(name: String) {
        _name.value = name
    }

    fun setIcon(icon: String) {
        _icon.value = icon
    }

    fun setColor(color: String) {
        _color.value = color
    }

    fun startEditing(category: Category) {
        _editingCategory.value = category
        _name.value = category.name
        _icon.value = category.icon
        _color.value = category.color
    }

    fun cancelEditing() {
        _editingCategory.value = null
        resetForm()
    }

    fun saveCategory() {
        viewModelScope.launch {
            if (!validateInput()) {
                return@launch
            }

            _isLoading.value = true
            try {
                val category = _editingCategory.value?.copy(
                    name = _name.value,
                    icon = _icon.value,
                    color = _color.value,
                    isSync = false,
                    updatedAt = System.currentTimeMillis()
                ) ?: Category(
                    id = UUID.randomUUID().toString(),
                    name = _name.value,
                    icon = _icon.value,
                    color = _color.value,
                    isDefault = false,
                    isSync = false
                )

                if (_editingCategory.value == null) {
                    categoryRepository.addCategory(category)
                } else {
                    categoryRepository.updateCategory(category)
                }

                // Sync changes
                try {
                    categoryRepository.syncCategories()
                } catch (e: Exception) {
                    // Log but don't fail on sync error
                    e.printStackTrace()
                }

                _categorySaved.emit(Unit)
                cancelEditing()
            } catch (e: Exception) {
                _error.value = "Error saving category: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteCategory(categoryId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                categoryRepository.deleteCategory(categoryId)

                if (_editingCategory.value?.id == categoryId) {
                    cancelEditing()
                }

                // Sync changes
                try {
                    categoryRepository.syncCategories()
                } catch (e: Exception) {
                    // Log but don't fail on sync error
                    e.printStackTrace()
                }
            } catch (e: Exception) {
                _error.value = "Error deleting category: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun validateInput(): Boolean {
        return when {
            _name.value.isBlank() -> {
                _error.value = "Name cannot be empty"
                false
            }

            _icon.value.isBlank() -> {
                _error.value = "Please select an icon"
                false
            }

            else -> true
        }
    }

    private fun resetForm() {
        _name.value = ""
        _icon.value = ""
        _color.value = "#000000"
        _error.value = null
    }
}

data class CategoryUiState(
    val name: String = "",
    val icon: String = "",
    val color: String = "#000000",
    val isEditing: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
)
