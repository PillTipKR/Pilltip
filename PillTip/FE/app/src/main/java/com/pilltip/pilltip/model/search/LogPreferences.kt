package com.pilltip.pilltip.model.search

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


object SearchPreferencesKeys {
    val RECENT_SEARCHES = stringPreferencesKey("recent_searches_ordered")
}

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "search_preferences")

class SearchPreferencesManager(private val context: Context) {
    suspend fun saveSearchQuery(query: String) {
        context.dataStore.edit { preferences ->
            val json = preferences[SearchPreferencesKeys.RECENT_SEARCHES]
            val currentList = if (json != null) {
                Json.decodeFromString<List<String>>(json)
            } else emptyList()
            val updated = (currentList - query) + query
            preferences[SearchPreferencesKeys.RECENT_SEARCHES] = Json.encodeToString(updated.takeLast(10))
        }
    }

    suspend fun setSearchQueries(list: List<String>) {
        context.dataStore.edit { preferences ->
            preferences[SearchPreferencesKeys.RECENT_SEARCHES] = Json.encodeToString(list)
        }
    }

    val recentSearches: Flow<List<String>> = context.dataStore.data
        .map { preferences ->
            val json = preferences[SearchPreferencesKeys.RECENT_SEARCHES]
            if (json != null) {
                Json.decodeFromString(json)
            } else emptyList()
        }

    suspend fun clearSearchQueries() {
        context.dataStore.edit { preferences ->
            preferences.remove(SearchPreferencesKeys.RECENT_SEARCHES)
        }
    }
}