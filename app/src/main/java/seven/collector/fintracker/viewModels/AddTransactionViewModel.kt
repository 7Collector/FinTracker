package seven.collector.fintracker.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import seven.collector.fintracker.data.Category
import seven.collector.fintracker.data.MainData
import seven.collector.fintracker.data.Transaction

class AddTransactionViewModel(val mainData: MainData) : ViewModel() {
    private val _uiState = MutableStateFlow(AddTransactionUiState())
    val uiState: StateFlow<AddTransactionUiState> = _uiState

    init {
        _uiState.update { it ->
            it.copy(
                categories = mainData.categories.map { it.name },
                goals = mainData.goals.map { it.name } + listOf("Savings", "Taxes")
            )
        }
    }

    fun updateTransactionType(type: String) {
        _uiState.update { it.copy(transactionType = type) }
    }

    fun updateAmount(amount: String) {
        _uiState.update { it.copy(amount = amount) }
    }

    fun updateDescription(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    fun updateCategory(category: String) {
        _uiState.update { it.copy(category = category) }
    }

    fun updateGoal(goal: String) {
        _uiState.update { it.copy(goal = goal) }
    }

    fun updateDate(date: Long) {
        _uiState.update { it.copy(date = date) }
    }

    fun setEditingTransaction(transaction: Transaction?) {
        transaction?.let {
            _uiState.update { state ->
                state.copy(
                    isEditing = true,
                    editingTransactionId = transaction.id,
                    transactionType = when {
                        transaction.category.name in mainData.categories.map { it.name } -> transaction.category.name
                        transaction.category.name in mainData.goals.map { it.name } -> transaction.category.name
                        else -> "Unknown"
                    },
                    amount = transaction.amount.toString(),
                    description = transaction.name,
                    category = if (transaction.category.name in mainData.categories.map { it.name }) transaction.category.name else "",
                    goal = if (mainData.goals.any { it.name == transaction.category.name }) transaction.category.name else "",
                    date = transaction.time.toLong() * 1000 // Convert seconds to milliseconds
                )
            }
        }
    }

    fun addOrUpdateTransaction() {
        viewModelScope.launch {
            val currentState = _uiState.value
            val newTransaction = Transaction(
                id = if (currentState.isEditing) currentState.editingTransactionId else generateUniqueId(),
                name = currentState.description,
                category = when {
                    currentState.transactionType == "Savings" -> Category("Savings", 0.0, 0.0)
                    currentState.transactionType == "Taxes" -> Category("Taxes", 0.0, 0.0)
                    currentState.transactionType in mainData.goals.map { it.name } -> mainData.goals.find { it.name == currentState.transactionType }?.let { Category(it.name, it.total, it.collected) } ?: Category("Unknown", 0.0, 0.0)
                    currentState.transactionType in mainData.categories.map { it.name } -> mainData.categories.find { it.name == currentState.transactionType } ?: Category("Unknown", 0.0, 0.0)
                    else -> Category("Unknown", 0.0, 0.0)
                },
                amount = currentState.amount.toDoubleOrNull() ?: 0.0,
                time = (currentState.date / 1000).toInt() // Convert milliseconds to seconds
            )

            if (currentState.isEditing) {
                // Update existing transaction
                val updatedTransactions = mainData.transactions.map { if (it.id == newTransaction.id) newTransaction else it }
                mainData.transactions = updatedTransactions
            } else {
                // Add new transaction
                mainData.transactions = mainData.transactions + newTransaction
            }

            // Update balances, category usage, goals, etc.
            updateMainData(newTransaction, currentState.isEditing)

            // Reset UI state
            _uiState.update { it.copy(
                isEditing = false,
                editingTransactionId = "",
                transactionType = "",
                amount = "",
                description = "",
                category = "",
                goal = "",
                date = System.currentTimeMillis()
            ) }
        }
    }

    private fun updateMainData(transaction: Transaction, isEditing: Boolean) {
        when (transaction.category.name) {
            "Income" -> mainData.income += transaction.amount
            "Savings" -> mainData.savings += transaction.amount
            "Taxes" -> mainData.taxCollected += transaction.amount
            else -> {
                if (isEditing) {
                    // Revert previous transaction effects
                    val oldTransaction = mainData.transactions.find { it.id == transaction.id }
                    oldTransaction?.let {
                        mainData.expense -= it.amount
                        val oldCategory = mainData.categories.find { cat -> cat.name == it.category.name }
                        oldCategory?.used = (oldCategory?.used ?: 0.0) - it.amount
                    }
                }
                mainData.expense += transaction.amount
                val category = mainData.categories.find { it.name == transaction.category.name }
                category?.used = (category?.used ?: 0.0) + transaction.amount
            }
        }

        // Update goals
        val goal = mainData.goals.find { it.name == transaction.category.name }
        goal?.let {
            it.collected += transaction.amount
        }

        // Update balance
        mainData.balance = mainData.income - mainData.expense

        // TODO: Implement logic to save updated mainData to shared preferences or database
    }

    private fun generateUniqueId(): String {
        return System.currentTimeMillis().toString() + (0..9999).random().toString().padStart(4, '0')
    }
}

data class AddTransactionUiState(
    val transactionType: String = "",
    val amount: String = "",
    val description: String = "",
    val category: String = "",
    val goal: String = "",
    val date: Long = System.currentTimeMillis(),
    val isEditing: Boolean = false,
    val editingTransactionId: String = "",
    val categories: List<String> = emptyList(),
    val goals: List<String> = emptyList(),
    val isAddingTransaction: Boolean = false,
    val errorMessage: String? = null
)
