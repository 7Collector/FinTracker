package seven.collector.fintracker

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import seven.collector.fintracker.data.Category
import seven.collector.fintracker.data.Goal
import seven.collector.fintracker.data.MainData
import seven.collector.fintracker.helpers.DataHelper
import seven.collector.fintracker.ui.theme.FinTrackerTheme
import seven.collector.fintracker.viewModels.OnboardingViewModel

class OnboardingActivity : ComponentActivity() {
    private val onboardingViewModel: OnboardingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FinTrackerTheme {
                OnboardingScreen(
                    viewModel = onboardingViewModel,
                    context = this
                )
            }
        }
    }
}

@Composable
fun OnboardingScreen(viewModel: OnboardingViewModel, context: Context) {
    val steps = listOf("Basic Info", "Income & Savings", "Select Categories", "Set Limits", "Goals")
    var currentStep by rememberSaveable { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            LinearProgressIndicator(
                progress = (currentStep + 1) / steps.size.toFloat(),
                modifier = Modifier.fillMaxWidth()
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (currentStep) {
                0 -> BasicInfoScreen(
                    onNext = { currentStep++ },
                    viewModel = viewModel
                )

                1 -> IncomeSavingsScreen(
                    onNext = { currentStep++ },
                    viewModel = viewModel
                )

                2 -> SelectCategoriesScreen(
                    onNext = { currentStep++ },
                    viewModel = viewModel
                )

                3 -> SetLimitsScreen(
                    onNext = { currentStep++ },
                    viewModel = viewModel
                )

                4 -> GoalsScreen(
                    onFinish = {
                        DataHelper(context).saveMainDataToSharedPreferencesAndLeadToMain(it)
                    },
                    viewModel = viewModel
                )
            }
        }
    }
}

@Composable
fun BasicInfoScreen(onNext: () -> Unit, viewModel: OnboardingViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        // App Logo (Top)
        Text(
            "App Logo",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(16.dp)
        )

        Spacer(modifier = Modifier.weight(1.0f))
        Text(
            "Personal Details",
            style = MaterialTheme.typography.headlineMedium.copy(color = MaterialTheme.colorScheme.primary)
        )
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = viewModel.name.value,
            onValueChange = { viewModel.name.value = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "Age: ${viewModel.age.value}",
            style = MaterialTheme.typography.labelLarge.copy(color = MaterialTheme.colorScheme.primary)
        )
        Slider(
            value = viewModel.age.value.toFloat(),
            onValueChange = { viewModel.age.value = it.toInt() },
            valueRange = 0f..100f,
            steps = 100,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "Gender",
            style = MaterialTheme.typography.labelLarge.copy(color = MaterialTheme.colorScheme.primary)
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            RadioButton(
                selected = viewModel.gender.value == "Male",
                onClick = { viewModel.gender.value = "Male" }
            )
            Text("Male", modifier = Modifier.padding(end = 16.dp))
            RadioButton(
                selected = viewModel.gender.value == "Female",
                onClick = { viewModel.gender.value = "Female" }
            )
            Text("Female", modifier = Modifier.padding(end = 16.dp))
            RadioButton(
                selected = viewModel.gender.value == "Other",
                onClick = { viewModel.gender.value = "Other" }
            )
            Text("Other")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (viewModel.name.value.isNotEmpty() && viewModel.age.value >= 0 && viewModel.gender.value.isNotEmpty()) {
                    onNext()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = viewModel.name.value.isNotEmpty() && viewModel.age.value >= 0 && viewModel.gender.value.isNotEmpty()
        ) {
            Text("Next")
        }
    }
}

@Composable
fun IncomeSavingsScreen(onNext: () -> Unit, viewModel: OnboardingViewModel) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Text(
            "App Logo",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(16.dp)
        )
        Spacer(modifier = Modifier.weight(1.0f))
        Text(
            "Financial Details",
            style = MaterialTheme.typography.headlineMedium.copy(color = MaterialTheme.colorScheme.primary)
        )
        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = viewModel.income.value.toString(),
            onValueChange = { viewModel.income.value = it.toDouble() },
            label = { Text("Monthly Income") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "Monthly Savings: ${viewModel.savings.value}",
            style = MaterialTheme.typography.labelLarge.copy(color = MaterialTheme.colorScheme.primary)
        )
        Slider(
            value = viewModel.savings.value.toFloat(),
            onValueChange = { viewModel.savings.value = it.toDouble() },
            valueRange = 0f..viewModel.income.value.toFloat(),
            steps = 100,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "Tax Rate (%): ${viewModel.taxRate.value}",
            style = MaterialTheme.typography.labelLarge.copy(color = MaterialTheme.colorScheme.primary)
        )
        Slider(
            value = viewModel.taxRate.value.toFloat(),
            onValueChange = { viewModel.taxRate.value = it.toDouble() },
            valueRange = 0f..100f,
            steps = 100,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (viewModel.income.value > 0 && viewModel.savings.value >= 0 && viewModel.taxRate.value >= 0) {
                    onNext()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = viewModel.income.value > 0 && viewModel.savings.value >= 0 && viewModel.taxRate.value >= 0
        ) {
            Text("Next")
        }
    }
}

@Composable
fun SelectCategoriesScreen(onNext: () -> Unit, viewModel: OnboardingViewModel) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Text(
            "App Logo",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(16.dp)
        )
        Spacer(modifier = Modifier.weight(1.0f))
        Text(
            "Categories",
            style = MaterialTheme.typography.headlineMedium.copy(color = MaterialTheme.colorScheme.primary)
        )
        Spacer(modifier = Modifier.height(8.dp))
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 150.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(viewModel.predefinedCategories) { category ->
                val isSelected = viewModel.selectedCategories.any { it.name == category }
                CategoryChip(
                    category = category,
                    isSelected = isSelected,
                    onClick = { toggleCategorySelection(category, viewModel) }
                )
            }
        }
        CustomCategoryInput(
            customCategory = viewModel.customCategory.value,
            onCategoryChange = { viewModel.customCategory.value = it },
            onAddCategory = {
                if (viewModel.customCategory.value.isNotEmpty()) {
                    viewModel.selectedCategories.add(
                        Category(viewModel.customCategory.value, 0.0, 0.0)
                    )
                    viewModel.predefinedCategories.add(viewModel.customCategory.value)

                    viewModel.customCategory.value = ""
                }
            }
        )


        Button(
            onClick = {
                val allLimitsSet = viewModel.selectedCategories.isNotEmpty()
                if (allLimitsSet) {
                    viewModel.processLimits()
                    onNext()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = viewModel.selectedCategories.isNotEmpty()
        ) {
            Text("Next")
        }
    }
    LaunchedEffect(Unit) {
        viewModel.processLimits()
    }
}

@Composable
fun SetLimitsScreen(onNext: () -> Unit, viewModel: OnboardingViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        if (viewModel.isLoading.value) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Processing with AI...",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    )
                }
            }
        } else {
            Text(
                "App Logo",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(16.dp)
            )
            Spacer(modifier = Modifier.weight(1.0f))
            Text(
                "Limits",
                style = MaterialTheme.typography.headlineMedium.copy(color = MaterialTheme.colorScheme.primary)
            )
            Spacer(modifier = Modifier.height(8.dp))
            viewModel.categoryLimits.forEach { category ->
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        category.name,
                        style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.primary)
                    )
                    Spacer(modifier = Modifier.weight(1.0f))
                    Text(
                        getFormattedMoney(category.limit),
                        style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.primary)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Slider(
                    value = category.limit.toFloat(),
                    onValueChange = { newLimit ->
                        category.limit = newLimit.toDouble()
                    },
                    valueRange = 0f..category.limit.toFloat()+1000,
                    steps = 1000,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = category.limit > 0
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            Button(
                onClick = onNext,
                modifier = Modifier.fillMaxWidth(),
                enabled = viewModel.categoryLimits.all { it.limit > 0 }
            ) {
                Text("Next")
            }
        }
    }


}

@Composable
fun GoalsScreen(onFinish: (mainData: MainData) -> Unit, viewModel: OnboardingViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Text(
            "App Logo",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(16.dp)
        )

        Spacer(modifier = Modifier.weight(1.0f))

        Text(
            "Goals",
            style = MaterialTheme.typography.headlineMedium.copy(color = MaterialTheme.colorScheme.primary)
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (viewModel.goals.isNotEmpty()) {
            LazyColumn(
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(viewModel.goals) { goal ->
                    GoalItem(goal = goal)
                }
            }
        } else {
            Text(
                "No goals added yet",
                style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onBackground)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        var goalName by remember { mutableStateOf("") }
        var goalAmount by remember { mutableStateOf("") }

        TextField(
            value = goalName,
            onValueChange = { goalName = it },
            label = { Text("Goal Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = goalAmount,
            onValueChange = { goalAmount = it },
            label = { Text("Goal Amount") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (goalName.isNotEmpty() && goalAmount.isNotEmpty()) {
                    viewModel.goals.add(Goal(goalName, goalAmount.toDouble(), 0.0))
                    goalName = ""
                    goalAmount = ""
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = goalName.isNotEmpty() && goalAmount.isNotEmpty()
        ) {
            Text("Add Goal")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val mainData = MainData(
                    name = viewModel.name.value,
                    balance = viewModel.income.value-viewModel.savings.value,
                    income = viewModel.income.value,
                    expense = viewModel.savings.value,
                    savings = viewModel.savings.value,
                    categories = viewModel.categoryLimits.toList(),
                    transactions = emptyList(),
                    goals = viewModel.goals.toList(),
                    taxRate = viewModel.taxRate.value,
                    taxableAmount = viewModel.income.value*12,
                    taxCollected = 0.0
                )
                onFinish(mainData)
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Finish")
        }
    }
}

@Composable
fun GoalItem(goal: Goal) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = "${goal.name}",
            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.primary)
        )
        Text(
            text = "Collected: ${goal.collected} / Total: ${goal.total}",
            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onBackground)
        )
    }
}

@Composable
fun CategoryChip(category: String, isSelected: Boolean, onClick: () -> Unit) {
    FilterChip(
        onClick = onClick,
        label = { Text(category) },
        selected = isSelected,
        modifier = Modifier.padding(end = 8.dp)
    )
}

@Composable
fun CustomCategoryInput(
    customCategory: String,
    onCategoryChange: (String) -> Unit,
    onAddCategory: () -> Unit
) {
    TextField(
        value = customCategory,
        onValueChange = onCategoryChange,
        label = { Text("Add Custom Category") },
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions.Default.copy(
            imeAction = ImeAction.Done // Set the ImeAction to Done or any other appropriate action
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                onAddCategory()
            }
        )
    )
    Spacer(modifier = Modifier.height(8.dp))
}

fun toggleCategorySelection(category: String, viewModel: OnboardingViewModel) {
    val isSelected = viewModel.selectedCategories.any { it.name == category }
    if (isSelected) {
        viewModel.selectedCategories.removeAll { it.name == category }
    } else {
        viewModel.selectedCategories.add(Category(category, 0.0, 0.0))
    }
}

@Preview(showBackground = true)
@Composable
fun OnboardingScreenPreview() {
    val onboardingViewModel = OnboardingViewModel()
    FinTrackerTheme {
        BasicInfoScreen(onNext = { /*TODO*/ }, viewModel = OnboardingViewModel())
    }
}
