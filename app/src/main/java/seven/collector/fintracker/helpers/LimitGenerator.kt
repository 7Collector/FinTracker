package seven.collector.fintracker.helpers

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import seven.collector.fintracker.BuildConfig
import seven.collector.fintracker.data.Category

class LimitGenerator {
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
        systemInstruction = content { text("Task: You will receive a JSON object with the following data:\n\nCategories:\n\nEach category has:\nname: The name of the category.\nlimit: The maximum value for that category (to be set by you).\nused: The amount already used in that category.\nage: The user's age.\ngender: The user's gender.\nmonthly_income: The user's monthly income.\nmonthly_saving: The user's monthly savings.\ntax_rate: The user's tax rate.\nOutput: Return a JSON object with the same categories. For each category, update the limit value intelligently based on the provided user details and the used amount. Ensure the limits are reasonable and tailored to the user's financial situation.") },
    )

    private val chat = model.startChat()

    private fun categoriesToJSON(categories: List<Category>): JSONArray {
        val jsonArray = JSONArray()
        for (category in categories) {
            val jsonObject = JSONObject()
            jsonObject.put("used", category.used)
            jsonObject.put("limit", category.limit)
            jsonObject.put("name", category.name)
            jsonArray.put(jsonObject)
        }
        return jsonArray
    }

    suspend fun generateLimit(categories: List<Category>, age:Int, gender:String, monthlyIncome:Double, monthlySaving:Double, taxRate:Double): List<Category> {
        val payload = JSONObject()
        payload.put("categories",categoriesToJSON(categories))
        payload.put("monthly_income",monthlyIncome)
        payload.put("monthly_saving",monthlySaving)
        payload.put("tax_rate",taxRate)
        payload.put("age",age)
        payload.put("gender",gender)

        val response = chat.sendMessage(payload.toString())

        response.text?.let { JSONObject(it) }
        val updatedCategories = response.text?.let{
            try {
                val responseJson = JSONObject(it)
                val categoriesArray = responseJson.getJSONArray("categories")
                val result = mutableListOf<Category>()
                for (i in 0 until categoriesArray.length()) {val categoryJson = categoriesArray.getJSONObject(i)
                    val name = categoryJson.optString("name", "")
                    val limit = categoryJson.optDouble("limit", 0.0)
                    val used = categoryJson.optDouble("used", 0.0)
                    result.add(Category(name, limit, used))
                }
                result
            } catch (e: JSONException) {
                emptyList()
            }
        } ?: emptyList()

        return updatedCategories
    }
}