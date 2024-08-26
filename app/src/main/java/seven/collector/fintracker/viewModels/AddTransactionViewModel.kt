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
import seven.collector.fintracker.helpers.DataHelper

class AddTransactionViewModel(private val mainData: MainData, private val dataHelper: DataHelper) :
    ViewModel() {
    private val _uiState = MutableStateFlow(AddTransactionUiState())
    val uiState: StateFlow<AddTransactionUiState> = _uiState

    init {
        println("Initializing ViewModel with categories: ${mainData.categories} and goals: ${mainData.goals}")
        val categories = mainData.categories.map { it.name }
        val goals = mainData.goals.map { it.name }

        // Log or debug to ensure data is correctly mapped
        println("Mapped categories: $categories")
        println("Mapped goals: $goals")

        _uiState.update { it ->
            it.copy(
                categories = categories,
                goals = goals
            )
        }
    }

    fun updateTransactionType(type: TransactionType) {
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
                        transaction.category.name == "Income" -> TransactionType.INCOME
                        transaction.category.name == "Savings" -> TransactionType.SAVINGS
                        transaction.category.name == "Taxes" -> TransactionType.TAXES
                        mainData.goals.any { it.name == transaction.category.name } -> TransactionType.GOAL
                        else -> TransactionType.EXPENSE
                    },
                    amount = transaction.amount.toString(),
                    description = transaction.name,
                    category = transaction.category.name,
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
                category = when (currentState.transactionType) {
                    TransactionType.INCOME -> Category("Income", 0.0, 0.0)
                    TransactionType.SAVINGS -> Category("Savings", 0.0, 0.0)
                    TransactionType.TAXES -> Category("Taxes", 0.0, 0.0)
                    TransactionType.GOAL -> mainData.goals.find { it.name == currentState.goal }
                        ?.let { Category(it.name, it.total, it.collected) } ?: Category(
                        "Unknown",
                        0.0,
                        0.0
                    )

                    TransactionType.EXPENSE -> mainData.categories.find { it.name == currentState.category }
                        ?: Category("Unknown", 0.0, 0.0)
                },
                amount = currentState.amount.toDoubleOrNull() ?: 0.0,
                time = (currentState.date / 1000).toInt() // Convert milliseconds to seconds
            )

            if (currentState.isEditing) {
                val updatedTransactions =
                    mainData.transactions.map { if (it.id == newTransaction.id) newTransaction else it }
                mainData.transactions = updatedTransactions
            } else {
                mainData.transactions += newTransaction
            }

            updateMainData(newTransaction, currentState.isEditing)

            _uiState.update {
                it.copy(
                    isEditing = false,
                    editingTransactionId = "",
                    transactionType = TransactionType.EXPENSE,
                    amount = "",
                    description = "",
                    category = "",
                    goal = "",
                    date = System.currentTimeMillis()
                )
            }
        }
    }

    private fun updateMainData(transaction: Transaction, isEditing: Boolean) {
        when (transaction.category.name) {
            "Income" -> mainData.income += transaction.amount
            "Savings" -> {
                mainData.savings += transaction.amount
                mainData.expense += transaction.amount
            }

            "Taxes" -> {
                mainData.taxCollected += transaction.amount
                mainData.expense += transaction.amount
            }

            else -> {
                if (isEditing) {
                    // Revert previous transaction effects
                    val oldTransaction = mainData.transactions.find { it.id == transaction.id }
                    oldTransaction?.let {
                        mainData.expense -= it.amount
                        val oldCategory =
                            mainData.categories.find { cat -> cat.name == it.category.name }
                        oldCategory?.used = (oldCategory?.used ?: 0.0) - it.amount
                    }
                }
                mainData.expense += transaction.amount
                val category = mainData.categories.find { it.name == transaction.category.name }
                category?.used = (category?.used ?: 0.0) + transaction.amount
            }
        }

        val goal = mainData.goals.find { it.name == transaction.category.name }
        goal?.let {
            it.collected += transaction.amount
            mainData.expense += transaction.amount
        }

        mainData.balance = mainData.income - mainData.expense

        dataHelper.saveMainDataToSharedPreferencesAndLeadToMain(mainData)
    }

    private fun generateUniqueId(): String {
        return System.currentTimeMillis().toString() + (0..9999).random().toString()
            .padStart(4, '0')
    }
}

data class AddTransactionUiState(
    val transactionType: TransactionType = TransactionType.EXPENSE,
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

enum class TransactionType {
    EXPENSE, INCOME, SAVINGS, GOAL, TAXES
}