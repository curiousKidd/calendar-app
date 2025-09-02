package com.kidd1412.workcalendar.ui

import android.content.Context
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun WorkCalendarApp(vm: MainViewModel = viewModel()) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Calendar", "Settings")

    Scaffold(
        topBar = {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }
        }
    ) { padding ->
        when (selectedTab) {
            0 -> CalendarScreen(vm = vm, modifier = Modifier.padding(padding))
            1 -> SettingsScreen(vm, modifier = Modifier.padding(padding))
        }
    }
}


val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "workcalendar_settings"
)