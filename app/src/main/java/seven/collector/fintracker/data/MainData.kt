package seven.collector.fintracker.data

data class MainData(
    val name: String,
    var balance: Double,
    var income: Double,
    var expense: Double,
    var savings: Double,
    val categories: List<Category>,
    var transactions: List<Transaction>,
    val goals: List<Goal>,
    val taxRate: Double,
    val taxableAmount: Double,
    var taxCollected: Double
)