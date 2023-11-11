package com.example.mio.Helper

import android.content.Context
import com.example.mio.Model.LocationReadAllResponse
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object SharedPrefManager {

    private const val RECENT_SEARCH_PREF = "recent_search_pref"
    private const val RECENT_SEARCH_KEY = "recent_search_key"

    fun loadRecentSearch(context: Context): List<String>? {
        val prefs = context.getSharedPreferences(RECENT_SEARCH_PREF, Context.MODE_PRIVATE)
        val jsonListString = prefs.getString(RECENT_SEARCH_KEY, null) ?: return null

        // Assuming that the stored JSON represents a list of LocationReadAllResponse objects
        return Gson().fromJson(jsonListString, object : TypeToken<List<String>>() {}.type)
    }

    fun saveRecentSearch(context: Context, newLocation: String) {
        val prefs = context.getSharedPreferences(RECENT_SEARCH_PREF, Context.MODE_PRIVATE)
        val currentLocations: MutableList<String> = loadRecentSearch(context)?.toMutableList() ?: mutableListOf()

        // Remove the location if it already exists in the list
        currentLocations.remove(newLocation)

        // Add the new location to the beginning of the list
        currentLocations.add(0, newLocation)

        val editor = prefs.edit()
        val json = Gson().toJson(currentLocations)
        editor.putString(RECENT_SEARCH_KEY, json)
        editor.apply()
    }

    fun convertLocationToJSON(location: LocationReadAllResponse): String {
        val gson = Gson()
        return gson.toJson(location)
    }

    fun convertJSONToLocation(json: String): LocationReadAllResponse {
        val gson = Gson()
        return gson.fromJson(json, LocationReadAllResponse::class.java)
    }

    fun removeRecentSearch(context: Context, locationToRemove: String) {
        val prefs = context.getSharedPreferences(RECENT_SEARCH_PREF, Context.MODE_PRIVATE)
        val currentLocations: List<String> = loadRecentSearch(context) ?: listOf()

        // 제거할 위치를 제외한 나머지 위치들을 가져옵니다.
        val updatedLocations = currentLocations.filter { it != locationToRemove }
        val editor = prefs.edit()
        val json = Gson().toJson(updatedLocations)
        editor.putString(RECENT_SEARCH_KEY, json)
        editor.apply()
    }

    fun isLocationInRecentSearch(context: Context, locationJson: String): Boolean {
/*        val recentSearchListJson: List<String> = loadRecentSearch(context) ?: listOf()
        return recentSearchListJson.contains(locationJson)*/
        val recentSearches = loadRecentSearch(context) ?: return false
        return recentSearches.contains(locationJson)
    }
}