package com.example.stockapp.Structures

import android.content.Context
import android.util.Log
import com.example.stockapp.Services.APIService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray

class AutocompleteItem{
    suspend fun retrieveAutocompleteData(apiService: APIService, inputQuery: String): List<String> {
        return withContext(Dispatchers.IO) {
            val results = apiService.fetchAutocompleteData(inputQuery)
            val autoCompleteItems = mutableListOf<String>()
            if (results != null) {
                for (i in 0 until results.length()) {
                    val item = results.getJSONObject(i)
                    autoCompleteItems.add(item.getString("symbol") + " | " + item.getString("description"))
                }
            }
            autoCompleteItems  // Return the processed list
        }
    }
}