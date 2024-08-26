package seven.collector.fintracker.helpers

import com.google.gson.Gson
import seven.collector.fintracker.data.MainData
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import seven.collector.fintracker.MainActivity

class DataHelper(private val context: Context) {

    private val gson = Gson()

    fun saveMainDataToSharedPreferencesAndLeadToMain(mainData: MainData) {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences("file", Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        val json = gson.toJson(mainData)
        editor.putString("mainData", json)
        editor.apply()
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        context.startActivity(intent)
    }

    fun getMainDataFromSharedPreferences(): String? {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences("file", Context.MODE_PRIVATE)
        return sharedPreferences.getString("mainData", "") ?: ""
    }
}
