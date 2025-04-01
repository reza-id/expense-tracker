package dev.cianjur.expense.presentation.dashboard

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.cianjur.expense.ui.components.ErrorView
import dev.cianjur.expense.ui.components.ExpenseEmptyState
import dev.cianjur.expense.ui.components.LoadingIndicator
import dev.cianjur.expense.presentation.dashboard.components.ExpenseChart
import dev.cianjur.expense.presentation.dashboard.components.SummaryCard
import dev.cianjur.expense.ui.components.getCategoryIcon
import dev.cianjur.expense.util.CurrencyFormatter
import dev.cianjur.expense.util.DateFormatter
import org.koin.androidx.compose.getViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import androidx.core.graphics.toColorInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToAddExpense: () -> Unit,
    onNavigateToExpenseDetail: (String) -> Unit,
    viewModel: DashboardViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currencyFormatter = koinInject<CurrencyFormatter>()
    val dateFormatter = koinInject<DateFormatter>()
    var showPeriodMenu by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = true) {
        viewModel.syncData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard") },
                actions = {
                    IconButton(onClick = { showPeriodMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Select Period"
                        )
                    }
                    DropdownMenu(
                        expanded = showPeriodMenu,
                        onDismissRequest = { showPeriodMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Week") },
                            onClick = {
                                viewModel.setPeriod(Period.WEEK)
                                showPeriodMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Month") },
                            onClick = {
                                viewModel.setPeriod(Period.MONTH)
                                showPeriodMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Year") },
                            onClick = {
                                viewModel.setPeriod(Period.YEAR)
                                showPeriodMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("All Time") },
                            onClick = {
                                viewModel.setPeriod(Period.ALL)
                                showPeriodMenu = false
                            }
                        )
                    }
                }
            )
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
                    onRetry = { viewModel.syncData() }
                )
                uiState.categoryStats.isEmpty() -> ExpenseEmptyState(
                    onAddExpense = onNavigateToAddExpense
                )
                else -> DashboardContent(
                    uiState = uiState,
                    currencyFormatter = currencyFormatter,
                    dateFormatter = dateFormatter,
                    onCategoryClick = { /* Navigate to category detail */ },
                    onExpenseClick = onNavigateToExpenseDetail
                )
            }
        }
    }
}

@Composable
fun DashboardContent(
    uiState: DashboardUiState,
    currencyFormatter: CurrencyFormatter,
    dateFormatter: DateFormatter,
    onCategoryClick: (String) -> Unit,
    onExpenseClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Period info
            Text(
                text = when (uiState.selectedPeriod) {
                    Period.WEEK -> "This Week"
                    Period.MONTH -> "This Month"
                    Period.YEAR -> "This Year"
                    Period.ALL -> "All Time"
                },
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Total amount
            SummaryCard(
                title = "Total Expenses",
                amount = uiState.totalAmount,
                currencyFormatter = currencyFormatter
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Expense chart
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Expense Trend",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    ExpenseChart(
                        dailyExpenses = uiState.dailyExpenses,
                        dateFormatter = dateFormatter,
                        currencyFormatter = currencyFormatter,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Category breakdown title
            Text(
                text = "Category Breakdown",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        items(uiState.categoryStats) { categoryStat ->
            CategoryExpenseItem(
                categoryStat = categoryStat,
                currencyFormatter = currencyFormatter,
                onClick = { onCategoryClick(categoryStat.category.id) }
            )
        }
    }
}

@Composable
fun CategoryExpenseItem(
    categoryStat: CategoryStat,
    currencyFormatter: CurrencyFormatter,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        Color(categoryStat.category.color.toColorInt()),
                        shape = MaterialTheme.shapes.small
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getCategoryIcon(categoryStat.category.icon),
                    contentDescription = null,
                    tint = Color.White
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = categoryStat.category.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = "${categoryStat.percentage.toInt()}% of total",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            Text(
                text = currencyFormatter.format(categoryStat.amount),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
