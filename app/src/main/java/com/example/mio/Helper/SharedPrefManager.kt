package com.example.mio.Helper

import android.content.Context
import android.util.Log
import com.example.mio.Model.LocationReadAllResponse
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*
import kotlin.collections.LinkedHashSet

object SharedPrefManager {

    private const val RECENT_SEARCH_PREF = "recent_search_pref"
    private const val RECENT_SEARCH_KEY = "recent_search_key"
    private const val RECENT_ACCOUNT_LOCATION_SEARCH_PREF = "recent_account_location_search_pref"
    private const val RECENT_ACCOUNT_LOCATION_SEARCH_KEY = "recent_account_location_search_key"

    fun loadRecentSearch(context: Context): List<String>? {
        val prefs = context.getSharedPreferences(RECENT_SEARCH_PREF, Context.MODE_PRIVATE)
        val jsonListString = prefs.getString(RECENT_SEARCH_KEY, null) ?: return null

        // Assuming that the stored JSON represents a list of LocationReadAllResponse objects
        return Gson().fromJson(jsonListString, object : TypeToken<List<String>>() {}.type)
    }

    fun saveRecentSearch(context: Context, newLocation: String) {
        val prefs = context.getSharedPreferences(RECENT_SEARCH_PREF, Context.MODE_PRIVATE)

        // 기존 저장된 위치 리스트를 로드
        val currentLocations: MutableList<String> = loadRecentSearch(context)?.toMutableList() ?: mutableListOf()

        // 새로운 위치를 정규화하여 대소문자 무시 및 공백 제거하여 비교
        val normalizedNewLocation = newLocation.trim().lowercase(Locale.getDefault())

        // 리스트에서 중복된 위치 제거
        val iterator = currentLocations.iterator()
        while (iterator.hasNext()) {
            val location = iterator.next()
            if (location.trim().lowercase(Locale.getDefault()) == normalizedNewLocation) {
                iterator.remove()
            }
        }

        // 새로운 위치를 리스트의 맨 앞에 추가
        currentLocations.add(0, newLocation)

        // 중복 제거를 위해 Set으로 변환한 후 다시 리스트로 변환
        val uniqueLocations = LinkedHashSet(currentLocations)

        // JSON 형식으로 변환하여 SharedPreferences에 저장
        val editor = prefs.edit()
        val json = Gson().toJson(uniqueLocations)
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

    /////////////////////////////////////////////////////////////////////////////////////////////

    fun loadAccountLocationRecentSearch(context: Context): List<String>? {
        val prefs = context.getSharedPreferences(RECENT_ACCOUNT_LOCATION_SEARCH_PREF, Context.MODE_PRIVATE)
        val jsonListString = prefs.getString(RECENT_ACCOUNT_LOCATION_SEARCH_KEY, null) ?: return null
        Log.e("saveAccountLocationRecent1", jsonListString)
        Log.e("saveAccountLocationRecent2", jsonListString.toString())
        // Assuming that the stored JSON represents a list of LocationReadAllResponse objects
        return Gson().fromJson(jsonListString, object : TypeToken<List<String>>() {}.type)
    }

    fun saveAccountLocationRecentSearch(context: Context, newLocation: String) {
        // 새로운 위치가 공백인 경우에는 저장하지 않음
        val prefs = context.getSharedPreferences(RECENT_ACCOUNT_LOCATION_SEARCH_PREF, Context.MODE_PRIVATE)
        val currentLocations: MutableList<String> = loadAccountLocationRecentSearch(context)?.toMutableList() ?: mutableListOf()
        Log.e("saveAccountLocationRecent1", newLocation)
        Log.e("saveAccountLocationRecent2", currentLocations.toString())
        // Remove the location if it already exists in the list
        currentLocations.remove(newLocation)

        // Add the new location to the beginning of the list
        currentLocations.add(0, newLocation)
        Log.e("saveAccountLocationRecent3", newLocation)
        Log.e("saveAccountLocationRecent4", currentLocations.toString())

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
        Log.e("REmoveAccountLocationRecent", currentLocations.toString())
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
