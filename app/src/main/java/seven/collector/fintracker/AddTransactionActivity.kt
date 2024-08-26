package seven.collector.fintracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import seven.collector.fintracker.data.Category
import seven.collector.fintracker.data.Goal
import seven.collector.fintracker.data.MainData
import seven.collector.fintracker.data.Transaction
import seven.collector.fintracker.viewModels.AddTransactionViewModel

class AddTransactionActivity : ComponentActivity() {
    private lateinit var addTransactionViewModel: AddTransactionViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val jsonData = intent.getStringExtra("mainData") ?: ""
        val mainData = Gson().fromJson(jsonData, MainData::class.java)

        addTransactionViewModel = AddTransactionViewModel(mainData)

        setContent {
            MaterialTheme {
                AddTransactionScreen(viewModel = addTransactionViewModel)
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(viewModel: AddTransactionViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Transaction") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            TransactionTypeSelector(
                selectedType = uiState.transactionType,
                onTypeSelected = viewModel::updateTransactionType,
                categories = uiState.categories,
                goals = uiState.goals
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

            if (uiState.transactionType in uiState.categories) {
                CategoryChips(
                    categories = uiState.categories + uiState.goals,
                    selectedCategory = uiState.category,
                    onCategorySelected = viewModel::updateCategory
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = viewModel::addOrUpdateTransaction,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add Transaction")
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TransactionTypeSelector(
    selectedType: String,
    categories: List<String>,
    goals: List<String>,
    onTypeSelected: (String) -> Unit
) {
    Column {
        Text(
            text = "Transaction Type",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
        ) {
            (categories + goals ).forEach { type ->
                FilterChip(
                    selected = type == selectedType,
                    onClick = { onTypeSelected(type) },
                    label = { Text(type) }
                )
            }
        }
    }
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryChips(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    Column {
        Text(
            text = "Category",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
        ) {
            categories.forEach { category ->
                FilterChip(
                    selected = category == selectedCategory,
                    onClick = { onCategorySelected(category) },
                    label = { Text(category) }
                )
            }
            // Adding Savings and Taxes as options
            listOf("Savings", "Taxes").forEach { option ->
                FilterChip(
                    selected = option == selectedCategory,
                    onClick = { onCategorySelected(option) },
                    label = { Text(option) }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewAddTransactionScreen() {
    AddTransactionScreen(viewModel = AddTransactionViewModel(MainData(
        name = "John Doe",
        balance = 5000.00,
        income = 10000.00,
        expense = 5000.00,
        savings = 2000.00,
        categories = listOf(
            Category(name = "Food", limit = 2000.00, used = 1500.00),
            Category(name = "Transport", limit = 1000.00, used = 700.00)
        ),
        transactions = listOf(
            Transaction(name = "Grocery", Category(name = "Food", limit = 2000.00, used = 1500.00), amount = 500.00, time = 478383948, id = ""),
            Transaction(name = "Bus Fare", Category(name = "Transport", limit = 1000.00, used = 700.00), amount = 50.00, time = 865894994, id = "")
        ),
        goals = listOf(
            Goal(name = "Emergency Fund", total = 10000.00, collected = 3000.00),
            Goal(name = "Vacation", total = 5000.00, collected = 1000.00)
        ),
        taxRate = 0.10,
        taxableAmount = 10000.00,
        taxCollected = 1000.00
    )))
}
