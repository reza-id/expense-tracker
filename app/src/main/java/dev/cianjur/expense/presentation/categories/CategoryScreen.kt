package dev.cianjur.expense.presentation.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.cianjur.expense.domain.model.Category
import dev.cianjur.expense.ui.components.CategoryEmptyState
import dev.cianjur.expense.ui.components.ErrorView
import dev.cianjur.expense.ui.components.LoadingIndicator
import dev.cianjur.expense.ui.components.getCategoryIcon
import org.koin.androidx.compose.koinViewModel
import androidx.core.graphics.toColorInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(
    viewModel: CategoryViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showAddEditCategoryDialog by remember { mutableStateOf(false) }
    var showColorPickerDialog by remember { mutableStateOf(false) }
    var showIconPickerDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var categoryToDelete by remember { mutableStateOf<Category?>(null) }

    // Observe category saved event
    LaunchedEffect(viewModel) {
        viewModel.categorySaved.collect {
            snackbarHostState.showSnackbar(
                if (uiState.isEditing) "Category updated" else "Category created"
            )
        }
    }

    // Show error in snackbar
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    // Add/Edit Category Dialog
    if (showAddEditCategoryDialog) {
        CategoryDialog(
            name = uiState.name,
            icon = uiState.icon,
            color = uiState.color,
            isEditing = uiState.isEditing,
            onNameChange = { viewModel.setName(it) },
            onIconClick = { showIconPickerDialog = true },
            onColorClick = { showColorPickerDialog = true },
            onSave = {
                viewModel.saveCategory()
                showAddEditCategoryDialog = false
            },
            onDismiss = {
                viewModel.cancelEditing()
                showAddEditCategoryDialog = false
            }
        )
    }

    // Icon Picker Dialog
    if (showIconPickerDialog) {
        IconPickerDialog(
            selectedIcon = uiState.icon,
            onIconSelected = {
                viewModel.setIcon(it)
                showIconPickerDialog = false
            },
            onDismiss = { showIconPickerDialog = false }
        )
    }

    // Color Picker Dialog
    if (showColorPickerDialog) {
        ColorPickerDialog(
            selectedColor = uiState.color,
            onColorSelected = {
                viewModel.setColor(it)
                showColorPickerDialog = false
            },
            onDismiss = { showColorPickerDialog = false }
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirmation && categoryToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteConfirmation = false
                categoryToDelete = null
            },
            title = { Text("Delete Category") },
            text = { Text("Are you sure you want to delete this category? Expenses in this category will not be deleted.") },
            confirmButton = {
                Button(
                    onClick = {
                        categoryToDelete?.id?.let { viewModel.deleteCategory(it) }
                        showDeleteConfirmation = false
                        categoryToDelete = null
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmation = false
                        categoryToDelete = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Categories") })
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.cancelEditing() // Ensure we're not in edit mode
                    showAddEditCategoryDialog = true
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Category")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> LoadingIndicator()
                uiState.error != null && categories.isEmpty() -> ErrorView(
                    message = uiState.error ?: "An error occurred",
                    onRetry = { /* Retry logic */ }
                )
                categories.isEmpty() -> CategoryEmptyState(
                    onAddCategory = { showAddEditCategoryDialog = true }
                )
                else -> CategoryList(
                    categories = categories,
                    onEditCategory = { category ->
                        viewModel.startEditing(category)
                        showAddEditCategoryDialog = true
                    },
                    onDeleteCategory = { category ->
                        categoryToDelete = category
                        showDeleteConfirmation = true
                    }
                )
            }
        }
    }
}

@Composable
fun CategoryList(
    categories: List<Category>,
    onEditCategory: (Category) -> Unit,
    onDeleteCategory: (Category) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { category ->
            CategoryListItem(
                category = category,
                onEditClick = { onEditCategory(category) },
                onDeleteClick = { onDeleteCategory(category) }
            )
        }
    }
}

@Composable
fun CategoryListItem(
    category: Category,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category icon with color background
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(category.color.toColorInt())),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getCategoryIcon(category.icon),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Category name
            Text(
                text = category.name,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )

            // Default indicator
            if (category.isDefault) {
                Text(
                    text = "Default",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.width(8.dp))
            }

            // Menu
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More Options"
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        leadingIcon = {
                            Icon(Icons.Default.Edit, contentDescription = null)
                        },
                        text = { Text("Edit") },
                        onClick = {
                            onEditClick()
                            showMenu = false
                        }
                    )

                    if (!category.isDefault) {
                        DropdownMenuItem(
                            leadingIcon = {
                                Icon(Icons.Default.Delete, contentDescription = null)
                            },
                            text = { Text("Delete") },
                            onClick = {
                                onDeleteClick()
                                showMenu = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryDialog(
    name: String,
    icon: String,
    color: String,
    isEditing: Boolean,
    onNameChange: (String) -> Unit,
    onIconClick: () -> Unit,
    onColorClick: () -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEditing) "Edit Category" else "Add Category") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Name field
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = { Text("Category Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Icon preview and selector
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Icon",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color(color.toColorInt()))
                                .clickable(onClick = onIconClick),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = getCategoryIcon(icon),
                                contentDescription = "Select Icon",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    // Color preview and selector
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Color",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color(color.toColorInt()))
                                .border(
                                    width = 2.dp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                                    shape = CircleShape
                                )
                                .clickable(onClick = onColorClick)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onSave,
                enabled = name.isNotBlank() && icon.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun IconPickerDialog(
    selectedIcon: String,
    onIconSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val icons = remember {
        listOf(
            "home" to Icons.Default.Home,
            "restaurant" to Icons.Default.LocalDining,
            "shopping_cart" to Icons.Default.ShoppingCart,
            "category" to Icons.Default.Category
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Icon") },
        text = {
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(icons) { (iconName, iconVector) ->
                    val isSelected = selectedIcon == iconName

                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surface
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                                shape = CircleShape
                            )
                            .clickable { onIconSelected(iconName) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = iconVector,
                            contentDescription = iconName,
                            tint = if (isSelected) Color.White
                            else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(24.dp)
                        )

                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Selected",
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}

@Composable
fun ColorPickerDialog(
    selectedColor: String,
    onColorSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = remember {
        listOf(
            "#4CAF50", // Green
            "#2196F3", // Blue
            "#FFC107", // Yellow
            "#9C27B0", // Purple
            "#F44336", // Red
            "#00BCD4", // Cyan
            "#607D8B", // Blue Grey
            "#FF5722", // Deep Orange
            "#795548", // Brown
            "#8BC34A", // Light Green
            "#3F51B5", // Indigo
            "#CDDC39"  // Lime
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Color") },
        text = {
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(colors) { color ->
                    val isSelected = selectedColor == color

                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color(color.toColorInt()))
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                                shape = CircleShape
                            )
                            .clickable { onColorSelected(color) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Selected",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}
