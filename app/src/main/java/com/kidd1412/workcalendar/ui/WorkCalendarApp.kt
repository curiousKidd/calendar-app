package com.kidd1412.workcalendar.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

@Composable
fun WorkCalendarApp() {
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
			0 -> CalendarScreen()
			1 -> SettingsScreen()
		}
	}
}

@Composable
fun CalendarScreen() {
	Surface(modifier = Modifier.fillMaxSize()) {
		Text("Calendar will show generated shifts")
	}
}

@Composable
fun SettingsScreen() {
	Surface(modifier = Modifier.fillMaxSize()) {
		Text("Settings: routine builder, workplace/year, monthly overrides")
	}
}
