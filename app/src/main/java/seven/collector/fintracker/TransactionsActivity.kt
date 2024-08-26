package seven.collector.fintracker

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
import seven.collector.fintracker.data.Category
import seven.collector.fintracker.data.MainData
import seven.collector.fintracker.data.Transaction
import seven.collector.fintracker.ui.theme.FinTrackerTheme

class TransactionsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val jsonData = intent.getStringExtra("mainData") ?: ""
        val mainData = Gson().fromJson(jsonData, MainData::class.java)

        setContent {
            FinTrackerTheme {
                TransactionsScreen(mainData)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(mainData: MainData) {
    val context = LocalContext.current

    Scaffold{ innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Text(
                text = "Transactions",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
            ) {
                items(mainData.transactions) { transaction ->
                    TransactionItem(transaction) { transactionId ->
                        val intent = Intent(context, AddTransactionActivity::class.java).apply {
                            putExtra("transactionId", transactionId)
                        }
                        context.startActivity(intent)
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionItem(transaction: Transaction, onClick: (String) -> Unit) {
    Card(
        onClick = { onClick(transaction.id) }, // Call onClick with transaction.id
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = transaction.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "₹${getFormattedMoney(transaction.amount)}",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = getFormattedDate(transaction.time),
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Category: ${transaction.category.name}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TransactionsScreenPreview() {
    val sampleMainData = MainData(
        name = "John Doe",
        balance = 10000.00,
        income = 15000.00,
        expense = 5000.00,
        savings = 3000.00,
        categories = listOf(
            Category("Food", 2000.00, 1500.00),
            Category("Transport", 1000.00, 800.00),
            Category("Entertainment", 1500.00, 1000.00)
        ),
        transactions = listOf(
            Transaction(
                "Grocery Shopping",
                Category("Food", 2000.00, 1500.00),
                150.00,
                1628956800,
                id = "1"
            ),
            Transaction(
                "Movie Tickets",
                Category("Entertainment", 1500.00, 1000.00),
                50.00,
                1629043200,
                id = "2"
            ),
            Transaction(
                "Bus Fare",
                Category("Transport", 1000.00, 800.00),
                20.00,
                1629129600,
                id = "3"
            ),
            Transaction(
                "Restaurant Dinner",
                Category("Food", 2000.00, 1500.00),
                80.00,
                1629216000,
                id = "4"
            ),
            Transaction(
                "Taxi Ride",
                Category("Transport", 1000.00, 800.00),
                30.00,
                1629302400,
                id = "5"
            )
        ),
        goals = listOf(), // Add sample goals if needed
        taxRate = 0.15,
        taxableAmount = 12000.00,
        taxCollected = 1800.00
    )

    FinTrackerTheme {
        TransactionsScreen(sampleMainData)
    }
}
