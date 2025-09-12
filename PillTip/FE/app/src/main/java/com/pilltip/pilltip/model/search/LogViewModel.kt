package com.pilltip.pilltip.model.search

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LogViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = SearchPreferencesManager(application)

    val recentSearches = prefs.recentSearches
        .map { it.reversed() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun addSearchQuery(query: String) {
        viewModelScope.launch {
            prefs.saveSearchQuery(query)
        }
    }
    fun deleteSearchQuery(query: String) {
        viewModelScope.launch {
            val current = prefs.recentSearches.first()
            val updated = current.filterNot { it == query }
            prefs.setSearchQueries(updated)
        }
    }

    fun clearSearchQueries() {
        viewModelScope.launch {
            prefs.clearSearchQueries()
        }
    }
}