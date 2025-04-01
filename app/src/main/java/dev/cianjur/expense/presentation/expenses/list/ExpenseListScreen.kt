package dev.cianjur.expense.presentation.expenses.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.cianjur.expense.domain.model.Category
import dev.cianjur.expense.domain.model.Expense
import dev.cianjur.expense.presentation.expenses.list.components.ExpenseItem
import dev.cianjur.expense.ui.components.CategoryItem
import dev.cianjur.expense.ui.components.EmptyStateView
import dev.cianjur.expense.ui.components.ErrorView
import dev.cianjur.expense.ui.components.ExpenseEmptyState
import dev.cianjur.expense.ui.components.LoadingIndicator
import dev.cianjur.expense.util.CurrencyFormatter
import dev.cianjur.expense.util.DateFormatter
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseListScreen(
    onNavigateToAddExpense: () -> Unit,
    onNavigateToExpenseDetail: (String) -> Unit,
    viewModel: ExpenseListViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currencyFormatter = koinInject<CurrencyFormatter>()
    val dateFormatter = koinInject<DateFormatter>()
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = true) {
        viewModel.loadExpenses()
    }

    LaunchedEffect(key1 = searchQuery) {
        viewModel.setSearchQuery(searchQuery)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Expenses") },
                actions = {
                    IconButton(onClick = { isSearchActive = true }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search Expenses"
                        )
                    }
                    IconButton(onClick = { viewModel.clearFilters() }) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filter Expenses"
                        )
                    }
                }
            )

            if (isSearchActive) {
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    onSearch = { searchQuery = it },
                    active = true,
                    onActiveChange = { isSearchActive = it },
                    placeholder = { Text("Search expenses...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search Icon"
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear Search"
                                )
                            }
                        }
                    },
                    content = {}
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAddExpense) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Expense"
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> LoadingIndicator()
                uiState.error != null -> ErrorView(
                    message = uiState.error ?: "An error occurred",
                    onRetry = { viewModel.loadExpenses() }
                )
                uiState.expenses.isEmpty() && uiState.selectedCategoryId == null && searchQuery.isEmpty() ->
                    ExpenseEmptyState(onAddExpense = onNavigateToAddExpense)
                uiState.expenses.isEmpty() ->
                    EmptyStateView(
                        title = "No Matching Expenses",
                        message = "Try changing your search or filters to see more expenses.",
                        icon = Icons.Default.Search,
                        actionLabel = "Clear Filters",
                        onAction = {
                            viewModel.clearFilters()
                            searchQuery = ""
                        }
                    )
                else -> ExpenseListContent(
                    expenses = uiState.expenses,
                    categories = uiState.categories,
                    selectedCategoryId = uiState.selectedCategoryId,
                    onExpenseClick = onNavigateToExpenseDetail,
                    onCategoryClick = { viewModel.setSelectedCategory(it) },
                    onClearFilters = { viewModel.clearFilters() },
                    currencyFormatter = currencyFormatter,
                    dateFormatter = dateFormatter
                )
            }
        }
    }
}

@Composable
fun ExpenseListContent(
    expenses: List<Expense>,
    categories: List<Category>,
    selectedCategoryId: String?,
    onExpenseClick: (String) -> Unit,
    onCategoryClick: (String) -> Unit,
    onClearFilters: () -> Unit,
    currencyFormatter: CurrencyFormatter,
    dateFormatter: DateFormatter
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Category filter chips
        if (categories.isNotEmpty()) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedCategoryId == null,
                        onClick = { onClearFilters() },
                        label = { Text("All") }
                    )
                }

                items(categories) { category ->
                    CategoryItem(
                        category = category,
                        isSelected = category.id == selectedCategoryId,
                        onClick = { onCategoryClick(category.id) }
                    )
                }
            }
        }

        // Expense list
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Group expenses by date
            val groupedExpenses = expenses.groupBy { it.date }

            groupedExpenses.forEach { (date, expensesForDate) ->
                item {
                    Text(
                        text = dateFormatter.getRelativeDateDescription(date),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                items(expensesForDate) { expense ->
                    ExpenseItem(
                        expense = expense,
                        category = categories.find { it.id == expense.categoryId },
                        currencyFormatter = currencyFormatter,
                        onClick = { onExpenseClick(expense.id) }
                    )
                }
            }
        }
    }
}
