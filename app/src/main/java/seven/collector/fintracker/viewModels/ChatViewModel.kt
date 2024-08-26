package seven.collector.fintracker.viewModels

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import seven.collector.fintracker.data.MainData
import seven.collector.fintracker.helpers.InsightGenerator

class ChatViewModel : ViewModel() {
    private val insightGenerator = InsightGenerator()
    var insight by mutableStateOf("")
    var isLoading by mutableStateOf(false)

    fun generateInsight(mainData: MainData) {
        viewModelScope.launch {
            isLoading = true
            insight = insightGenerator.generateInsight(mainData)
            isLoading = false
        }
    }
}
