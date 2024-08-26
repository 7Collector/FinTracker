package seven.collector.fintracker

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
import seven.collector.fintracker.data.MainData
import seven.collector.fintracker.helpers.DataHelper
import seven.collector.fintracker.ui.theme.FinTrackerTheme
import seven.collector.fintracker.viewModels.AddTransactionViewModel
import seven.collector.fintracker.viewModels.TransactionType
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AddTransactionActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val jsonData = intent.getStringExtra("mainData") ?: ""
        val mainData = Gson().fromJson(jsonData, MainData::class.java)
        val viewModel = AddTransactionViewModel(mainData, DataHelper(this))
        // If editing an existing transaction, set it in the ViewModel
        intent.getStringExtra("transactionId")?.let { transactionId ->
            val transactionToEdit = mainData.transactions.find { it.id == transactionId }
            viewModel.setEditingTransaction(transactionToEdit)
        }

        setContent {
            FinTrackerTheme {
                AddTransactionScreen(
                    viewModel = viewModel,
                    onTransactionAdded = {
                        // Save updated MainData to your data source
                        finish()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    viewModel: AddTransactionViewModel,
    onTransactionAdded: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Text(
                text = "Add Transaction",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            TransactionTypeSelector(
                selectedType = uiState.transactionType,
                onTypeSelected = viewModel::updateTransactionType
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = uiState.amount,
                onValueChange = viewModel::updateAmount,
                label = { Text("Amount") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = uiState.description,
                onValueChange = viewModel::updateDescription,
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            when (uiState.transactionType) {
                TransactionType.EXPENSE -> {
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                        uiState.categories.forEach { category ->
                            FilterChip(
                                selected = uiState.category == category,
                                onClick = { viewModel.updateCategory(category) },
                                modifier = Modifier.padding(4.dp),
                                label = { Text(category) }
                            )
                        }
                    }
                }

                TransactionType.GOAL -> {
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                        uiState.goals.forEach { goal ->
                            FilterChip(
                                selected = uiState.goal == goal,
                                onClick = { viewModel.updateGoal(goal) },
                                modifier = Modifier.padding(4.dp),
                                label = { Text(goal) }
                            )
                        }
                    }
                }

                else -> { /* No additional fields needed for other transaction types */
                }
            }


            Spacer(modifier = Modifier.height(16.dp))

            val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
            OutlinedTextField(
                value = dateFormatter.format(Date(uiState.date)),
                onValueChange = { },
                label = { Text("Date") },
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val calendar = Calendar.getInstance()
                        calendar.timeInMillis = uiState.date
                        DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                calendar.set(year, month, dayOfMonth)
                                viewModel.updateDate(calendar.timeInMillis)
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    viewModel.addOrUpdateTransaction()
                    onTransactionAdded()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (uiState.isEditing) "Update Transaction" else "Add Transaction")
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TransactionTypeSelector(
    selectedType: TransactionType,
    onTypeSelected: (TransactionType) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(all = 8.dp),
    ) {
        Text(
            text = "Transaction Type",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TransactionType.entries.forEach { type ->
                FilterChip(
                    selected = type == selectedType,
                    onClick = { onTypeSelected(type) },
                    label = { Text(type.name.capitalize()) }
                )
            }
        }
    }
}