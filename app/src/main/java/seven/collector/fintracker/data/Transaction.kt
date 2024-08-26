package seven.collector.fintracker.data

data class Transaction(val name: String, val category: Category, val amount: Double, val time: Int, var id: String)
