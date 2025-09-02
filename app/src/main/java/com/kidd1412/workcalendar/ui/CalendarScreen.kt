package com.kidd1412.workcalendar.ui

// Use the shared DataStore extension declared in SettingsScreen.kt
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.emptyPreferences
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kidd1412.workcalendar.data.DEFAULT_DAY_TIME
import com.kidd1412.workcalendar.data.DEFAULT_NIGHT_TIME
import com.kidd1412.workcalendar.data.DEFAULT_WORK_CYCLE
import com.kidd1412.workcalendar.data.KEY_CYCLE_HISTORY
import com.kidd1412.workcalendar.data.KEY_DAY_DEFAULT
import com.kidd1412.workcalendar.data.KEY_NIGHT_DEFAULT
import com.kidd1412.workcalendar.data.KEY_WORK_CYCLE
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.ceil


// Colors (git-like)
private val GIT_ADD_BG = Color(0xFFE6FFED)
private val GIT_DEL_BG = Color(0xFFFFEEF0)
private val GIT_NEUTRAL_BG = Color(0xFFF6F8FA)

@Composable
fun CalendarScreen(
    vm: MainViewModel,
    modifier: Modifier = Modifier,
    locale: Locale = Locale.KOREA,
    weekStart: DayOfWeek = DayOfWeek.SUNDAY,
    onDayClick: (LocalDate) -> Unit = {}
) {
    val context = LocalContext.current

    // Ensure VM has fresh workplace
    LaunchedEffect(Unit) { vm.refreshWorkplace(context) }
    val workplace by vm.currentWorkplaceLabel.collectAsStateWithLifecycle()

    // Load settings from DataStore
    val savedCycle by remember(context) {
        context.dataStore.data
            .catch { emit(emptyPreferences()) }
            .map { prefs ->
                prefs[KEY_WORK_CYCLE]?.split(',')?.map { it.trim() }?.filter { it.isNotEmpty() }
                    ?: DEFAULT_WORK_CYCLE
            }
    }.collectAsState(initial = DEFAULT_WORK_CYCLE)

    val nightDefault by remember(context) {
        context.dataStore.data
            .catch { emit(emptyPreferences()) }
            .map { prefs -> prefs[KEY_NIGHT_DEFAULT] ?: DEFAULT_NIGHT_TIME }
    }.collectAsState(initial = DEFAULT_NIGHT_TIME)

    val dayDefault by remember(context) {
        context.dataStore.data
            .catch { emit(emptyPreferences()) }
            .map { prefs -> prefs[KEY_DAY_DEFAULT] ?: DEFAULT_DAY_TIME }
    }.collectAsState(initial = DEFAULT_DAY_TIME)

    val cycleHistoryMap by remember(context) {
        context.dataStore.data
            .catch { emit(emptyPreferences()) }
            .map { prefs ->
                val raw = prefs[KEY_CYCLE_HISTORY]
                raw?.split(',')?.mapNotNull { entry ->
                    val kv = entry.split('=')
                    if (kv.size == 2) kv[0].trim() to kv[1].trim() else null
                }?.toMap() ?: emptyMap()
            }
    }.collectAsState(initial = emptyMap())

    var currentYm by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    val today = LocalDate.now()

    val dates = remember(currentYm, weekStart) { buildMonthCells(currentYm, weekStart) }
    val monthTitle = remember(currentYm, locale) { "${currentYm.year}년 ${currentYm.monthValue}월" }

    Surface(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top summary card
            val focusCycle = workCycleLabel(selectedDate, savedCycle)
            val ymKey = String.format("%04d-%02d", selectedDate.year, selectedDate.monthValue)
            val pair = cycleHistoryMap[ymKey]?.split('|')
            val histDay = pair?.getOrNull(0)
            val histNight = pair?.getOrNull(1)
            val startTimeDisplay = when (focusCycle) {
                "야" -> (histNight ?: nightDefault)
                "주" -> (histDay ?: dayDefault)
                else -> "—"
            }

            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "근무 상태",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("날짜: ${selectedDate}", style = MaterialTheme.typography.bodyMedium)
                        Text("근무지: ${workplace}", style = MaterialTheme.typography.bodyMedium)
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("근무: ${focusCycle}", style = MaterialTheme.typography.bodyMedium)
                        Text("출근: ${startTimeDisplay}", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            // Month header
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { currentYm = currentYm.minusMonths(1) }) {
                    Text("〈", style = MaterialTheme.typography.titleLarge)
                }
                Spacer(Modifier.weight(1f))
                Text(
                    monthTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.weight(1f))
                TextButton(onClick = {
                    // 오늘로 이동: 보이는 달/선택 날짜 모두 현재로 갱신
                    currentYm = YearMonth.now()
                    selectedDate = LocalDate.now()
                }) {
                    Text("오늘", style = MaterialTheme.typography.titleMedium)
                }
                TextButton(onClick = { currentYm = currentYm.plusMonths(1) }) {
                    Text("〉", style = MaterialTheme.typography.titleLarge)
                }
            }

            // Weekday header
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                dayOfWeekSequence(weekStart).forEach { dow ->
                    val label = dow.getDisplayName(TextStyle.SHORT, locale)
                    Text(
                        label,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(dates) { cell ->
                    when (cell) {
                        is CalendarCell.Blank -> Spacer(modifier = Modifier.aspectRatio(1f))
                        is CalendarCell.Day -> {
                            val isToday = cell.date == today
                            val isSelected = cell.date == selectedDate
                            val inThisMonth =
                                cell.date.month == currentYm.month && cell.date.year == currentYm.year

                            val textStyle = if (inThisMonth) MaterialTheme.typography.bodyLarge else
                                MaterialTheme.typography.bodyLarge.copy(
                                    color = MaterialTheme.colorScheme.onSurface.copy(
                                        alpha = 0.35f
                                    )
                                )

                            val cycle = workCycleLabel(cell.date, savedCycle)
                            val baseContainer = when (cycle) {
                                "주" -> GIT_ADD_BG
                                "야" -> GIT_DEL_BG
                                else -> GIT_NEUTRAL_BG
                            }
                            val containerColor =
                                if (inThisMonth) baseContainer else baseContainer.copy(alpha = 0.35f)

                            val borderStroke: BorderStroke = when {
                                isSelected -> BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                                isToday -> BorderStroke(1.dp, MaterialTheme.colorScheme.secondary)
                                else -> BorderStroke(0.dp, Color.Transparent)
                            }

                            OutlinedCard(
                                modifier = Modifier.aspectRatio(1f),
                                border = borderStroke,
                                colors = CardDefaults.outlinedCardColors(containerColor = containerColor),
                                onClick = {
                                    selectedDate = cell.date
                                    onDayClick(cell.date)
                                }
                            ) {
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = cell.date.dayOfMonth.toString(),
                                            style = textStyle,
                                            textAlign = TextAlign.Center,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                        Spacer(Modifier.size(2.dp))
                                        val cycleColor = when (cycle) {
                                            "주" -> MaterialTheme.colorScheme.primary
                                            "야" -> MaterialTheme.colorScheme.secondary
                                            else -> MaterialTheme.colorScheme.tertiary
                                        }.copy(alpha = if (inThisMonth) 1f else 0.35f)
                                        Text(
                                            cycle,
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

// Calendar helpers
private sealed interface CalendarCell {
    data object Blank : CalendarCell
    data class Day(val date: LocalDate) : CalendarCell
}

private fun buildMonthCells(ym: YearMonth, weekStart: DayOfWeek): List<CalendarCell> {
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

private fun workCycleLabel(date: LocalDate, cycle: List<String>, startOffset: Int = 0): String {
    val base = if (cycle.isNotEmpty()) cycle else DEFAULT_WORK_CYCLE
    val idx = (date.dayOfMonth - 1 + startOffset).floorMod(base.size)
    return base[idx]
}

private fun Int.floorMod(m: Int): Int = ((this % m) + m) % m