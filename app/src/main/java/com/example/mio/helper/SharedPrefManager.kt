package com.example.mio.helper

import android.content.Context
import com.example.mio.model.LocationReadAllResponse
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object SharedPrefManager {

    private const val RECENT_SEARCH_PREF = "recent_search_pref"
    private const val RECENT_SEARCH_KEY = "recent_search_key"
    private const val RECENT_ACCOUNT_LOCATION_SEARCH_PREF = "recent_account_location_search_pref"
    private const val RECENT_ACCOUNT_LOCATION_SEARCH_KEY = "recent_account_location_search_key"

    fun loadRecentSearch(context: Context): List<LocationReadAllResponse>? {
        val prefs = context.getSharedPreferences(RECENT_SEARCH_PREF, Context.MODE_PRIVATE)
        val jsonListString = prefs.getString(RECENT_SEARCH_KEY, "[]") ?: return null

        // Assuming that the stored JSON represents a list of LocationReadAllResponse objects
        return Gson().fromJson(jsonListString, object : TypeToken<List<LocationReadAllResponse>>() {}.type)
    }

    fun saveRecentSearch(context: Context, newLocation: LocationReadAllResponse) {
        val prefs = context.getSharedPreferences(RECENT_SEARCH_PREF, Context.MODE_PRIVATE)
        val gson = Gson()

        // Load recent search list using the modified loadRecentSearch function
        val recentSearchList: MutableList<LocationReadAllResponse> = try {
            loadRecentSearch(context)?.toMutableList() ?: mutableListOf()
        } catch (e: Exception) {
            mutableListOf()
        }

        // Check if the new location already exists in the current list
        val isDuplicate = recentSearchList.any { it.location == newLocation.location }

        if (!isDuplicate) {
            recentSearchList.add(0, newLocation)
        }
        // Save the updated list back to SharedPreferences
        val updatedJson = gson.toJson(recentSearchList)
        val editor = prefs.edit()
        editor.putString(RECENT_SEARCH_KEY, updatedJson)
        editor.apply()
    }

    fun convertLocationToJSON(location: LocationReadAllResponse): String {
        val gson = Gson()
        return gson.toJson(location)
    }

    /*fun convertJSONToLocation(json: String): LocationReadAllResponse {
        val gson = Gson()
        return gson.fromJson(json, LocationReadAllResponse::class.java)
    }*/

    fun removeRecentSearch(context: Context, locationToRemove: String) {
        val prefs = context.getSharedPreferences(RECENT_SEARCH_PREF, Context.MODE_PRIVATE)

        // Load the current list of recent searches
        val currentLocations: List<LocationReadAllResponse> = loadRecentSearch(context) ?: listOf()

        // Filter out the location that needs to be removed
        val updatedLocations = currentLocations.filter { it.location != locationToRemove }

        // Convert the updated list back to JSON and save it
        val editor = prefs.edit()
        val json = Gson().toJson(updatedLocations)
        editor.putString(RECENT_SEARCH_KEY, json)
        editor.apply()
    }


    /////////////////////////////////////////////////////////////////////////////////////////////

    fun loadAccountLocationRecentSearch(context: Context): List<String>? {
        val prefs = context.getSharedPreferences(RECENT_ACCOUNT_LOCATION_SEARCH_PREF, Context.MODE_PRIVATE)
        val jsonListString = prefs.getString(RECENT_ACCOUNT_LOCATION_SEARCH_KEY, null) ?: return null
        // Assuming that the stored JSON represents a list of LocationReadAllResponse objects
        return Gson().fromJson(jsonListString, object : TypeToken<List<String>>() {}.type)
    }

    fun saveAccountLocationRecentSearch(context: Context, newLocation: String) {
        // 새로운 위치가 공백인 경우에는 저장하지 않음
        val prefs = context.getSharedPreferences(RECENT_ACCOUNT_LOCATION_SEARCH_PREF, Context.MODE_PRIVATE)
        val currentLocations: MutableList<String> = loadAccountLocationRecentSearch(context)?.toMutableList() ?: mutableListOf()
        // Remove the location if it already exists in the list
        currentLocations.remove(newLocation)

        // Add the new location to the beginning of the list
        currentLocations.add(0, newLocation)

        val editor = prefs.edit()
        val json = Gson().toJson(currentLocations)
        editor.putString(RECENT_ACCOUNT_LOCATION_SEARCH_KEY, json)
        editor.apply()
    }

    fun convertAccountLocationToJSON(location: LocationReadAllResponse): String {
        val gson = Gson()
        return gson.toJson(location)
    }

    fun convertJSONToAccountLocation(json: String): LocationReadAllResponse {
        val gson = Gson()
        return gson.fromJson(json, LocationReadAllResponse::class.java)
    }

    fun removeAccountLocationRecentSearch(context: Context, locationToRemove: String) {
        val prefs = context.getSharedPreferences(RECENT_ACCOUNT_LOCATION_SEARCH_PREF, Context.MODE_PRIVATE)
        val currentLocations: List<String> = loadAccountLocationRecentSearch(context) ?: listOf()

        // 제거할 위치를 제외한 나머지 위치들을 가져옵니다.
        val updatedLocations = currentLocations.filter { it != locationToRemove }

        val editor = prefs.edit()
        val json = Gson().toJson(updatedLocations)
        editor.putString(RECENT_ACCOUNT_LOCATION_SEARCH_KEY, json)
        editor.apply()
    }

    fun isAccountLocationInRecentSearch(context: Context, locationJson: String): Boolean {
/*        val recentSearchListJson: List<String> = loadRecentSearch(context) ?: listOf()
        return recentSearchListJson.contains(locationJson)*/
        val recentSearches = loadAccountLocationRecentSearch(context) ?: return false
        return recentSearches.contains(locationJson)
    }
}
