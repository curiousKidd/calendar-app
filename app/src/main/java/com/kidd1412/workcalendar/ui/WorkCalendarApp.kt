package com.kidd1412.workcalendar.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.YearMonth
import java.time.LocalDate

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
			0 -> CalendarScreen()
			1 -> SettingsScreen(vm)
		}
	}
}

@Composable
fun CalendarScreen() {
	Surface(modifier = Modifier.fillMaxSize()) {
		val ym = remember { YearMonth.now() }
		val days: List<LocalDate> = remember { (1..ym.lengthOfMonth()).map { ym.atDay(it) } }
		val pattern = listOf("주", "주", "야", "야", "휴", "휴")
		LazyColumn {
			items(days) { date ->
				val idx = (date.dayOfMonth - 1) % pattern.size
				Text(text = "${date.dayOfMonth}일 • ${pattern[idx]}")
			}
		}
	}
}

@Composable
fun SettingsScreen(vm: MainViewModel) {
	val workplace by vm.currentWorkplaceLabel.collectAsStateWithLifecycle()
	Surface(modifier = Modifier.fillMaxSize()) {
		Column {
			Text(text = "올해 근무지: $workplace")
			Button(onClick = { vm.toggleWorkplace() }) { Text("근무지 토글") }
		}
	}
}
