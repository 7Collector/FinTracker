package seven.collector.fintracker.helpers

import seven.collector.fintracker.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import seven.collector.fintracker.data.MainData

class InsightGenerator {
    private val model = GenerativeModel(
        "gemini-1.5-flash",
        BuildConfig.geminiApiKey,
        generationConfig = generationConfig {
            temperature = 1.15f
            topK = 64
            topP = 0.95f
            maxOutputTokens = 8192
            responseMimeType = "application/json"
        },
        systemInstruction = content { text("You will receive a JSON object with the following structure:\n\nContext: All values are in INR (Indian Rupees). The taxRate represents the percentage of income that the user needs to pay in taxes to the government.\n\n{\n  \"balance\": 1500.0,\n  \"income\": 4000.0,\n  \"expense\": 2500.0,\n  \"savings\": 500.0,\n  \"categories\": [\n    {\n      \"name\": \"Groceries\",\n      \"limit\": 300.0,\n      \"used\": 200.0\n    },\n    {\n      \"name\": \"Rent\",\n      \"limit\": 1200.0,\n      \"used\": 1200.0\n    },\n    {\n      \"name\": \"Entertainment\",\n      \"limit\": 150.0,\n      \"used\": 150.0\n    }\n  ],\n  \"transactions\": [\n    {\n      \"name\": \"Grocery Store\",\n      \"category\": {\n        \"name\": \"Groceries\",\n        \"limit\": 300.0,\n        \"used\": 200.0\n      },\n      \"amount\": 100.0,\n      \"time\": 1692480123\n    }\n  ],\n  \"goals\": [\n    {\n      \"name\": \"Vacation\",\n      \"total\": 2000.0,\n      \"collected\": 500.0\n    }\n  ],\n  \"taxRate\": 0.2,\n  \"taxableAmount\": 4000.0,\n  \"taxCollected\": 800.0\n}\nTask: Analyze the financial data provided in the JSON. Summarize the user's financial health. Comment on their spending habits in relation to category limits, and offer a brief analysis of their tax situation. Your insight should be motivating and constructive, providing both encouragement and areas for improvement. The user should feel engaged and aware of their financial status.\n\nAdditionally, you will receive a prompt key in a separate JSON object. Only answer finance and spending-related queries based on the provided data.\n\nOutput: A JSON object with the following format:\n\n{\n  \"message\": \"...\"\n}") },
    )

    private val chat = model.startChat()

    suspend fun generateInsight(mainData: MainData): String{
        val response = chat.sendMessage(createPayload(mainData).toString())
        return response.text?.let {
            try {
                val responseJson = JSONObject(it)
                responseJson.optString("message", "No insight generated.")
            } catch (e: JSONException) {
                "Error processing the response."
            }
        } ?: "Error: No response from the model."
    }

    suspend fun chat(message: String): String{
        val response = chat.sendMessage(message)
        return response.text?.let {
            try {
                val responseJson = JSONObject(it)
                responseJson.optString("message", "No comment.")
            } catch (e: JSONException) {
                "Error processing the response."
            }
        } ?: "Error: No response from the model."
    }

    private fun createPayload(financialData: MainData): JSONObject {
        val payload = JSONObject()
        payload.put("balance", financialData.balance)
        payload.put("income", financialData.income)
        payload.put("expense", financialData.expense)
        payload.put("savings", financialData.savings)
        payload.put("taxRate", financialData.taxRate)
        payload.put("taxableAmount", financialData.taxableAmount)
        payload.put("taxCollected", financialData.taxCollected)

        val categoriesArray = JSONArray()
        for (category in financialData.categories) {
            val categoryObject = JSONObject()
            categoryObject.put("name", category.name)
            categoryObject.put("limit", category.limit)
            categoryObject.put("used", category.used)
            categoriesArray.put(categoryObject)
        }
        payload.put("categories", categoriesArray)

        val transactionsArray = JSONArray()
        for (transaction in financialData.transactions) {
            val transactionObject = JSONObject()
            transactionObject.put("name", transaction.name)
            val transactionCategoryObject = JSONObject()
            transactionCategoryObject.put("name", transaction.category.name)
            transactionCategoryObject.put("limit", transaction.category.limit)
            transactionCategoryObject.put("used", transaction.category.used)
            transactionObject.put("category", transactionCategoryObject)
            transactionObject.put("amount", transaction.amount)
            transactionObject.put("time", transaction.time)
            transactionsArray.put(transactionObject)
        }
        payload.put("transactions", transactionsArray)

        val goalsArray = JSONArray()
        for (goal in financialData.goals) {
            val goalObject = JSONObject()
            goalObject.put("name", goal.name)
            goalObject.put("total", goal.total)
            goalObject.put("collected", goal.collected)
            goalsArray.put(goalObject)
        }
        payload.put("goals", goalsArray)

        return payload
    }
}