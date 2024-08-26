package seven.collector.fintracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.activity.viewModels
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
import seven.collector.fintracker.data.MainData
import seven.collector.fintracker.ui.theme.FinTrackerTheme
import seven.collector.fintracker.viewModels.ChatViewModel

class ChatActivity : ComponentActivity() {
    val viewModel: ChatViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FinTrackerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ChatScreen(
                        modifier = Modifier.padding(innerPadding),
                        mainDataJson = intent.getStringExtra("mainDataJson") ?: "",
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

@Composable
fun ChatScreen(modifier: Modifier = Modifier, mainDataJson: String, viewModel: ChatViewModel) {

    val gson = Gson()
    val mainData = gson.fromJson(mainDataJson, MainData::class.java)

    LaunchedEffect(Unit) {
        viewModel.generateInsight(mainData)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (viewModel.isLoading) {
            CircularProgressIndicator()
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Text(
                    text = viewModel.insight,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChatScreenPreview() {
    FinTrackerTheme {
        ChatScreen(mainDataJson = """{"balance":1500.0,"income":4000.0,"expense":2500.0,"savings":500.0,"categories":[{"name":"Groceries","limit":300.0,"used":200.0},{"name":"Rent","limit":1200.0,"used":1200.0},{"name":"Entertainment","limit":150.0,"used":150.0}],"transactions":[{"name":"Grocery Store","category":{"name":"Groceries","limit":300.0,"used":200.0},"amount":100.0,"time":1692480123}],"goals":[{"name":"Vacation","total":2000.0,"collected":500.0}],"taxRate":0.2,"taxableAmount":4000.0,"taxCollected":800.0}""",
            viewModel = ChatViewModel()
        )
    }
}
