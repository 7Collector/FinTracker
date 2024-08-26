package seven.collector.fintracker.viewModels

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import seven.collector.fintracker.data.ChatMessage
import seven.collector.fintracker.data.MainData
import seven.collector.fintracker.helpers.InsightGenerator

class ChatViewModel : ViewModel() {
    private val insightGenerator = InsightGenerator()
    var insight by mutableStateOf("")
    var isLoading by mutableStateOf(false)

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages

    fun generateInsight(mainData: MainData) {
        viewModelScope.launch {
            isLoading = true
            insight = insightGenerator.generateInsight(mainData)
            _chatMessages.value += ChatMessage(insight, isUser = false)
            isLoading = false
        }
    }

    fun sendMessage(message: String) {
        viewModelScope.launch {
            _chatMessages.value += ChatMessage(message)
            val response = insightGenerator.chat(message)
            _chatMessages.value += ChatMessage(response, isUser = false)
        }
    }
}
