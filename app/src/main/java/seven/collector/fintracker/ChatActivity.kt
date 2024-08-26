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
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
import seven.collector.fintracker.data.ChatMessage
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

    var userInput by remember { mutableStateOf("") }
    val chatMessages by viewModel.chatMessages.collectAsState()

    // Reference to the LazyColumn's state
    val listState = rememberLazyListState()

    // Scroll to the end when new messages are added
    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            listState.scrollToItem(chatMessages.size - 1)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.generateInsight(mainData)
    }

    if (viewModel.isLoading) {
        Box(modifier = Modifier.fillMaxSize()) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Heading
            Text(
                text = "Chat with AI",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f),
                reverseLayout = false
            ) {
                items(chatMessages) { message ->
                    ChatMessage(message)
                }
            }

            // User input area
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = userInput,
                    onValueChange = { userInput = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Type a message") }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = {
                    if (userInput.isNotBlank()) {
                        viewModel.sendMessage(userInput)
                        userInput = ""
                    }
                }) {
                    Text("Send")
                }
            }
        }
    }
}


@Composable
fun ChatMessage(message: ChatMessage) {
    val alignment: Alignment = if (message.isUser) Alignment.CenterEnd else Alignment.CenterStart
    val backgroundColor = if (message.isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (message.isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .padding(8.dp),
        contentAlignment = alignment
    ) {
        Text(
            text = message.content,
            color = textColor,
            modifier = Modifier.background(backgroundColor, RoundedCornerShape(8.dp)).padding(8.dp)
        )
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
