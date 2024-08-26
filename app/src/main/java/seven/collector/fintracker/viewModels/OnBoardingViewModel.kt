package seven.collector.fintracker.viewModels

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import seven.collector.fintracker.data.Category
import seven.collector.fintracker.data.Goal
import seven.collector.fintracker.helpers.LimitGenerator

class OnboardingViewModel : ViewModel() {
    var predefinedCategories = mutableListOf(
        "Food", "Transport", "Entertainment", "Health", "Utilities",
        "Rent/Mortgage", "Education", "Groceries", "Clothing", "Personal Care",
        "Dining Out", "Travel", "Gifts", "Subscriptions", "Other"
    )
    var name = mutableStateOf("")
    var age = mutableStateOf(0)
    var gender = mutableStateOf("")

    var income = mutableStateOf(0.0)
    var savings = mutableStateOf(0.0)
    var taxRate = mutableStateOf(0.0)

    var selectedCategories = mutableStateListOf<Category>()
    var customCategory = mutableStateOf("")

    var categoryLimits = mutableStateListOf<Category>()

    var goals = mutableStateListOf<Goal>()

    var isLoading = mutableStateOf(false)

    fun processLimits() {
        isLoading.value = true
        val limitGenerator = LimitGenerator()
        viewModelScope.launch {
            try {
                val updatedCategories = limitGenerator.generateLimit(
                    categories = selectedCategories.toList(),
                    age = age.value,
                    gender = gender.value,
                    monthlyIncome = income.value,
                    monthlySaving = savings.value,
                    taxRate = taxRate.value
                )
                categoryLimits.clear()
                categoryLimits.addAll(updatedCategories)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading.value = false
            }
        }
    }
}
