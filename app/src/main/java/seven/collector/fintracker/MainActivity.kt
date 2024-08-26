package seven.collector.fintracker

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
import seven.collector.fintracker.data.Category
import seven.collector.fintracker.data.Goal
import seven.collector.fintracker.data.MainData
import seven.collector.fintracker.data.Transaction
import seven.collector.fintracker.ui.theme.FinTrackerTheme
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {

    private lateinit var obj: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        obj = getSharedPreferences("file", MODE_PRIVATE).getString("mainData", "") ?: ""
        if (obj.isEmpty()) {
            navigateTo("onboarding")
        }
        setContent {
            FinTrackerTheme {
                FinTrackerApp(Gson().fromJson(obj, MainData::class.java), navigateTo = ::navigateTo)
            }
        }
    }

    private fun navigateTo(destination: String) {
        when (destination) {
            "transactions" -> {
                intent = Intent(this, TransactionsActivity::class.java)
                intent.putExtra("mainData", obj)
                startActivity(intent)
            }

            "addTransaction" -> {
                intent = Intent(this, AddTransactionActivity::class.java)
                intent.putExtra("mainData", obj)
                startActivity(intent)
            }

            "onboarding" -> {
                intent = Intent(this, OnboardingActivity::class.java)
                startActivity(intent)
                finish()
            }

            "profile" -> {
                intent = Intent(this, ProfileActivity::class.java)
                startActivity(intent)
            }

            "chat" -> {
                intent = Intent(this, ChatActivity::class.java).putExtra("mainDataJson", obj)
                startActivity(intent)
            }
        }
    }
}

@Composable
fun FinTrackerApp(data: MainData, navigateTo: (String) -> Unit = {}) {
    Scaffold(
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End
            ) {
                SmallFloatingActionButton(
                    onClick = { navigateTo("chat") },
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Refresh, "Bottom FAB 1")
                }
                Spacer(modifier = Modifier.height(8.dp))
                FloatingActionButton(
                    onClick = { navigateTo("addTransaction") },
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Bottom FAB 2")
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .padding(top = 12.dp)
                .padding(innerPadding)
        ) {
            item {
                ProfileGreeting(name = data.name, navigateTo = navigateTo)
            }
            item {
                BalanceCard(
                    balance = data.balance,
                    income = data.income,
                    expense = data.expense,
                    savings = data.savings
                )
            }
            item {
                LimitsCard(categories = data.categories)
            }
            item {
                TransactionsCard(
                    transactions = data.transactions,
                    onViewAllClicked = { navigateTo("transactions") }
                )
            }
            item {
                GoalsCard(goals = data.goals)
            }
            item {
                TaxesCard(
                    taxCollected = data.taxCollected,
                    taxableAmount = data.taxableAmount
                )
            }
        }
    }
}

@Composable
fun ProfileGreeting(name: String, navigateTo: (String) -> Unit = {}) {
    Row(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Hello, $name!",
            style = MaterialTheme.typography.displaySmall.copy(color = MaterialTheme.colorScheme.primary)
        )
        IconButton(
            onClick = { navigateTo("profile") },
            modifier = Modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer)
        ) {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = "Profile Icon",
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
fun BalanceCard(balance: Double, income: Double, expense: Double, savings: Double) {
    val proportionUsed = if (income > 0) expense / income else 0.0
    InfoCard(title = "Balance") {
        Text(
            text = "₹${getFormattedMoney(balance)}",
            style = MaterialTheme.typography.displaySmall.copy(color = MaterialTheme.colorScheme.primary),
        )
        Spacer(modifier = Modifier.height(16.dp))
        LinearProgressIndicator(
            progress = proportionUsed.toFloat(),
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(MaterialTheme.shapes.medium),
            trackColor = MaterialTheme.colorScheme.surfaceContainer
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Income: ₹${getFormattedMoney(income)}")
            Text("Expense: ₹${getFormattedMoney(expense)}")
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text("Savings: ₹${getFormattedMoney(savings)}")
    }
}

@Composable
fun LimitsCard(categories: List<Category>) {
    InfoCard(title = "Limits") {
        if (categories.isEmpty()) {
            Text(text = "No categories available", style = MaterialTheme.typography.bodyMedium)
        } else {
            Column {
                categories.forEach { category ->
                    val proportionUsed =
                        if (category.limit > 0) category.used / category.limit else 0.0
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = category.name,
                                style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.primary)
                            )
                            Text(
                                text = "₹${getFormattedMoney(category.limit - category.used)} left"
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = proportionUsed.toFloat(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(MaterialTheme.shapes.medium),
                            trackColor = MaterialTheme.colorScheme.surfaceContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionsCard(transactions: List<Transaction>, onViewAllClicked: () -> Unit) {
    InfoCard(title = "Transactions") {
        if (transactions.isEmpty()) {
            Text(text = "No transactions available", style = MaterialTheme.typography.bodyMedium)
        } else {
            Column {
                transactions.forEach { transaction ->
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = transaction.name,
                                style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.primary)
                            )
                            Text(
                                text = "₹${getFormattedMoney(transaction.amount)}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Text(
                            text = getFormattedDate(transaction.time),
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onPrimaryContainer)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        ElevatedButton(
            onClick = onViewAllClicked,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "View All")
        }
    }
}

@Composable
fun GoalsCard(goals: List<Goal>) {
    InfoCard(title = "Goals") {
        if (goals.isEmpty()) {
            Text(text = "No goals available", style = MaterialTheme.typography.bodyMedium)
        } else {
            Column {
                goals.forEach { goal ->
                    val proportionUsed = if (goal.total > 0) goal.collected / goal.total else 0.0
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        CircularProgressIndicator(
                            progress = proportionUsed.toFloat(),
                            modifier = Modifier
                                .size(40.dp)
                                .padding(end = 8.dp),
                            strokeWidth = 4.dp,
                            trackColor = MaterialTheme.colorScheme.surfaceContainer
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = goal.name,
                                style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.primary)
                            )
                            Text(
                                text = "₹${getFormattedMoney(goal.collected)} collected",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onPrimaryContainer)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun TaxesCard(taxCollected: Double, taxableAmount: Double) {
    InfoCard(title = "Taxes") {
        Row {
            CircularProgressIndicator(
                progress = (taxCollected / taxableAmount).toFloat(),
                modifier = Modifier
                    .size(80.dp)
                    .padding(bottom = 8.dp),
                strokeWidth = 8.dp,
                trackColor = MaterialTheme.colorScheme.surfaceContainer
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "₹${getFormattedMoney(taxCollected)} contributed towards taxes",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Estimated Taxes: ₹${getFormattedMoney(taxableAmount)}",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onPrimaryContainer)
                )
            }
        }
    }
}

@Composable
fun InfoCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        modifier = Modifier
            .padding(all = 8.dp)
            .fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

fun getFormattedMoney(amount: Double): String = DecimalFormat("#,###.00").format(amount)

fun getFormattedDate(time: Int): String {
    val date = Date(time * 1000L)
    val format = SimpleDateFormat("dd MMM", Locale.getDefault())
    return format.format(date)
}

@Preview(showBackground = true)
@Composable
fun FinTrackerAppPreview() {
    FinTrackerTheme {
        FinTrackerApp(
            MainData(
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
                    Transaction(
                        name = "Grocery",
                        Category(name = "Food", limit = 2000.00, used = 1500.00),
                        amount = 500.00,
                        time = 478383948,
                        id = ""
                    ),
                    Transaction(
                        name = "Bus Fare",
                        Category(name = "Transport", limit = 1000.00, used = 700.00),
                        amount = 50.00,
                        time = 865894994,
                        id = ""
                    )
                ),
                goals = listOf(
                    Goal(name = "Emergency Fund", total = 10000.00, collected = 3000.00),
                    Goal(name = "Vacation", total = 5000.00, collected = 1000.00)
                ),
                taxRate = 0.10,
                taxableAmount = 10000.00,
                taxCollected = 1000.00
            )
        )
    }
}
