package com.kidd1412.workcalendar.ui

import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    // DataStore delegate and key
//    val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "workcalendar_settings")
    
    private val KEY_WORKPLACES = stringPreferencesKey("workplaces_csv")

    private val _currentWorkplaceLabel = MutableStateFlow("SiteA")
    val currentWorkplaceLabel: StateFlow<String> = _currentWorkplaceLabel

    private val _workplaces = MutableStateFlow(listOf("SiteA", "SiteB"))
    val workplaces: StateFlow<List<String>> = _workplaces

    init {
        _currentWorkplaceLabel.value = "SiteA"
    }

    fun refreshWorkplace(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            context.dataStore.data
                .map { prefs -> prefs[KEY_WORKPLACES] ?: "SiteA,SiteB" }
                .distinctUntilChanged()
                .collect { csv ->
                    applyWorkplacesCsv(csv)
                }
        }
    }

    fun applyWorkplacesCsv(csv: String) {
        val list = csv.split(',').map { it.trim() }.filter { it.isNotEmpty() }
        if (list.isNotEmpty()) {
            _workplaces.value = list
            if (_currentWorkplaceLabel.value !in list) {
                _currentWorkplaceLabel.value = list.first()
            }
        }
    }

    fun toggleWorkplace() {
        val list = _workplaces.value
        if (list.isEmpty()) return
        val cur = _currentWorkplaceLabel.value
        val idx = list.indexOf(cur).let { if (it == -1) 0 else (it + 1) % list.size }
        _currentWorkplaceLabel.value = list[idx]
    }

    fun setWorkplace(name: String) {
        val list = _workplaces.value
        if (name in list) {
            _currentWorkplaceLabel.value = name
        }
    }
}
