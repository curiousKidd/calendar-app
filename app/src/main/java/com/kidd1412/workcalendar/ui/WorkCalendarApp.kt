package com.kidd1412.workcalendar.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.YearMonth
import java.time.LocalDate
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
private val WORK_CYCLE: List<String> = listOf("주", "주", "야", "야", "휴", "휴")

private fun workCycleLabel(date: LocalDate, startOffset: Int = 0): String {
    // startOffset을 바꾸면 시작 기준을 조정 가능 (예: 0이면 1일이 "주")
    val idx = (date.dayOfMonth - 1 + startOffset).floorMod(WORK_CYCLE.size)
    return WORK_CYCLE[idx]
}

// Int의 floorMod 확장 (음수 안전)
private fun Int.floorMod(m: Int): Int = ((this % m) + m) % m
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.DayOfWeek
import java.util.Locale
import kotlin.math.ceil

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
			0 -> CalendarScreen(modifier = Modifier.padding(padding))
			1 -> SettingsScreen(vm, modifier = Modifier.padding(padding))
		}
	}
}

@Composable
fun CalendarScreen(
    modifier: Modifier = Modifier,
    locale: Locale = Locale.KOREA,
    weekStart: DayOfWeek = DayOfWeek.SUNDAY,
    onDayClick: (LocalDate) -> Unit = {}
) {
    Surface(modifier = modifier.fillMaxSize()) {
        var currentYm by remember { mutableStateOf(YearMonth.now()) }
        var selectedDate by remember { mutableStateOf(LocalDate.now()) }
        val today = LocalDate.now()

        val dates = remember(currentYm, weekStart) {
            buildMonthCells(currentYm, weekStart)
        }

        val monthTitle = remember(currentYm, locale) {
            "${currentYm.year}년 ${currentYm.monthValue}월"
        }

        Column(modifier = Modifier.padding(16.dp)) {
            // Header with month navigation
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { currentYm = currentYm.minusMonths(1) }) {
                    Text("〈", style = MaterialTheme.typography.titleLarge)
                }
                Spacer(Modifier.weight(1f))
                Text(
                    text = monthTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.weight(1f))
                TextButton(onClick = { currentYm = currentYm.plusMonths(1) }) {
                    Text("〉", style = MaterialTheme.typography.titleLarge)
                }
            }

            // Weekday header
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                dayOfWeekSequence(weekStart).forEach { dow ->
                    val label = dow.getDisplayName(java.time.format.TextStyle.SHORT, locale)
                    Text(
                        text = label,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // 7-column calendar grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(dates) { cell ->
                    when (cell) {
                        is CalendarCell.Blank -> {
                            Spacer(modifier = Modifier.aspectRatio(1f))
                        }
                        is CalendarCell.Day -> {
                            val isToday = cell.date == today
                            val isSelected = cell.date == selectedDate
                            val inThisMonth = cell.date.month == currentYm.month && cell.date.year == currentYm.year

                            val textStyle = if (inThisMonth) MaterialTheme.typography.bodyLarge
                            else MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                            )

							val borderStroke: BorderStroke = when {
								isSelected -> BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
								isToday    -> BorderStroke(1.dp, MaterialTheme.colorScheme.secondary)
								else       -> BorderStroke(0.dp, Color.Transparent) // null 대신 투명 보더
							}

                            OutlinedCard(
                                modifier = Modifier.aspectRatio(1f),
                                border = borderStroke,
                                onClick = {
                                    selectedDate = cell.date
                                    onDayClick(cell.date)
                                }
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = cell.date.dayOfMonth.toString(),
                                            style = textStyle,
                                            textAlign = TextAlign.Center,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                        Spacer(Modifier.size(2.dp))
                                        val cycle = workCycleLabel(cell.date)
                                        val cycleColor = when (cycle) {
                                            "주" -> MaterialTheme.colorScheme.primary
                                            "야" -> MaterialTheme.colorScheme.secondary
                                            else -> MaterialTheme.colorScheme.tertiary
                                        }.copy(alpha = if (inThisMonth) 1f else 0.35f)
                                        Text(
                                            text = cycle,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = cycleColor,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private sealed interface CalendarCell {
    data object Blank : CalendarCell
    data class Day(val date: LocalDate) : CalendarCell
}

private fun buildMonthCells(
    ym: YearMonth,
    weekStart: DayOfWeek
): List<CalendarCell> {
    val firstOfMonth = ym.atDay(1)
    val lengthOfMonth = ym.lengthOfMonth()
    val leadBlanks = dayOfWeekDistance(weekStart, firstOfMonth.dayOfWeek)

    val totalCells = leadBlanks + lengthOfMonth
    val rows = ceil(totalCells / 7.0).toInt()
    val paddedTotal = rows * 7
    val tailBlanks = paddedTotal - totalCells

    val cells = mutableListOf<CalendarCell>()
    repeat(leadBlanks) { cells += CalendarCell.Blank }
    for (d in 1..lengthOfMonth) cells += CalendarCell.Day(ym.atDay(d))
    repeat(tailBlanks) { cells += CalendarCell.Blank }
    return cells
}

private fun dayOfWeekSequence(weekStart: DayOfWeek): List<DayOfWeek> =
    (0 until 7).map { weekStart.plus(it.toLong()) }

private fun dayOfWeekDistance(weekStart: DayOfWeek, target: DayOfWeek): Int {
    val startIndex = weekStart.ordinal
    val targetIndex = target.ordinal
    return (targetIndex - startIndex + 7) % 7
}

@Composable
fun SettingsScreen(vm: MainViewModel, modifier: Modifier = Modifier) {
	val workplace by vm.currentWorkplaceLabel.collectAsStateWithLifecycle()
	Surface(modifier = modifier.fillMaxSize()) {
		Column {
			Text(text = "올해 근무지: $workplace")
			Button(onClick = { vm.toggleWorkplace() }) { Text("근무지 토글") }
		}
	}
}
