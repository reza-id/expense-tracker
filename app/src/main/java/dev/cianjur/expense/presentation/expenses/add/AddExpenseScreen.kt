package dev.cianjur.expense.presentation.expenses.add

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.NoPhotography
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import dev.cianjur.expense.presentation.expenses.add.components.CategorySelector
import dev.cianjur.expense.ui.components.AddImageItem
import dev.cianjur.expense.ui.components.ErrorView
import dev.cianjur.expense.ui.components.ImageItem
import dev.cianjur.expense.ui.components.LoadingIndicator
import dev.cianjur.expense.util.DateFormatter
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.androidx.compose.getViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import java.io.File
import java.time.ZoneId
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddExpenseScreen(
    onExpenseSaved: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: AddExpenseViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val categoryList by viewModel.categories.collectAsState()
    val dateFormatter = koinInject<DateFormatter>()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Date picker
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = java.time.LocalDate.now()
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    )

    // Image picker
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            context.contentResolver.openInputStream(it)?.use { inputStream ->
                val file = File(context.cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
                file.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
                viewModel.addImage(file)
            }
        }
    }

    // Observe expense saved event
    LaunchedEffect(viewModel) {
        viewModel.expenseSaved.collect {
            onExpenseSaved()
        }
    }

    // Show error in snackbar
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Expense") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                LoadingIndicator()
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Title
                    OutlinedTextField(
                        value = uiState.title,
                        onValueChange = { viewModel.setTitle(it) },
                        label = { Text("Title") },
                        placeholder = { Text("What did you spend on?") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Amount
                    OutlinedTextField(
                        value = uiState.amount,
                        onValueChange = { viewModel.setAmount(it) },
                        label = { Text("Amount") },
                        placeholder = { Text("0.00") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Decimal
                        ),
                        singleLine = true
                    )

                    // Date picker
                    OutlinedTextField(
                        value = dateFormatter.formatFullDate(uiState.date),
                        onValueChange = { },
                        label = { Text("Date") },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(Icons.Default.CalendarMonth, contentDescription = "Select Date")
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    if (showDatePicker) {
                        DatePickerDialog(
                            onDismissRequest = { showDatePicker = false },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        datePickerState.selectedDateMillis?.let { millis ->
                                            val localDate = Instant
                                                .fromEpochMilliseconds(millis)
                                                .toLocalDateTime(TimeZone.currentSystemDefault())
                                                .date
                                            viewModel.setDate(localDate)
                                        }
                                        showDatePicker = false
                                    }
                                ) {
                                    Text("OK")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showDatePicker = false }) {
                                    Text("Cancel")
                                }
                            }
                        ) {
                            DatePicker(state = datePickerState)
                        }
                    }

                    // Category
                    Text(
                        text = "Category",
                        style = MaterialTheme.typography.titleMedium
                    )

                    CategorySelector(
                        categories = categoryList,
                        selectedCategoryId = uiState.selectedCategoryId,
                        onCategorySelected = { viewModel.setCategory(it) }
                    )

                    // Notes
                    OutlinedTextField(
                        value = uiState.notes,
                        onValueChange = { viewModel.setNotes(it) },
                        label = { Text("Notes") },
                        placeholder = { Text("Add any additional details") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5
                    )

                    // Images
                    Column {
                        Text(
                            text = "Images",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        if (uiState.images.isEmpty()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NoPhotography,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = "No images attached",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Existing images
                            uiState.images.forEach { image ->
                                ImageItem(
                                    imageUri = image.imageUri,
                                    onClick = { /* View full image */ },
                                    onDelete = { viewModel.removeImage(image.id) }
                                )
                            }

                            // Add image button
                            AddImageItem(
                                onClick = { imagePickerLauncher.launch("image/*") }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Save button
                    Button(
                        onClick = { viewModel.saveExpense() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save Expense")
                    }
                }
            }
        }
    }
}
