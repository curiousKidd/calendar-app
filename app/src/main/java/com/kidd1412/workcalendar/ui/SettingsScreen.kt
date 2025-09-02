package com.kidd1412.workcalendar.ui

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import com.kidd1412.workcalendar.data.DEFAULT_DAY_TIME
import com.kidd1412.workcalendar.data.DEFAULT_NIGHT_TIME
import com.kidd1412.workcalendar.data.DEFAULT_WORKPLACES
import com.kidd1412.workcalendar.data.DEFAULT_WORK_CYCLE
import com.kidd1412.workcalendar.data.KEY_CYCLE_HISTORY
import com.kidd1412.workcalendar.data.KEY_DAY_DEFAULT
import com.kidd1412.workcalendar.data.KEY_NIGHT_DEFAULT
import com.kidd1412.workcalendar.data.KEY_WORKPLACES
import com.kidd1412.workcalendar.data.KEY_WORK_CYCLE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.YearMonth


private val TIME_RE = Regex("^\\d{2}:\\d{2}$")

private fun joinCsv(items: List<String>) = items.joinToString(",")

// cycle history: map[YYYY-MM] = "DAY|NIGHT" (e.g., 2025-09=09:00|22:00)
private fun parseCycleHistory(s: String?): Map<String, String> =
    s?.split(',')?.mapNotNull { entry ->
        val kv = entry.split('=')
        if (kv.size == 2) kv[0].trim() to kv[1].trim() else null
    }?.toMap() ?: emptyMap()

private fun joinCycleHistory(m: Map<String, String>) =
    m.entries.joinToString(",") { "${it.key}=${it.value}" }

@Composable
fun SettingsScreen(vm: MainViewModel, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val focus = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }

    val cycleCsv by remember(context) {
        context.dataStore.data
            .catch { emit(emptyPreferences()) }
            .map { it[KEY_WORK_CYCLE] ?: joinCsv(DEFAULT_WORK_CYCLE) }
    }.collectAsState(initial = joinCsv(DEFAULT_WORK_CYCLE))

    val nightDefault by remember(context) {
        context.dataStore.data
            .catch { emit(emptyPreferences()) }
            .map { it[KEY_NIGHT_DEFAULT] ?: DEFAULT_NIGHT_TIME }
    }.collectAsState(initial = DEFAULT_NIGHT_TIME)

    val dayDefault by remember(context) {
        context.dataStore.data
            .catch { emit(emptyPreferences()) }
            .map { it[KEY_DAY_DEFAULT] ?: DEFAULT_DAY_TIME }
    }.collectAsState(initial = DEFAULT_DAY_TIME)

    val cycleHistoryMap by remember(context) {
        context.dataStore.data
            .catch { emit(emptyPreferences()) }
            .map { parseCycleHistory(it[KEY_CYCLE_HISTORY]) }
    }.collectAsState(initial = emptyMap())

    val workplacesCsv by remember(context) {
        context.dataStore.data
            .catch { emit(emptyPreferences()) }
            .map { it[KEY_WORKPLACES] ?: DEFAULT_WORKPLACES }
    }.collectAsState(initial = DEFAULT_WORKPLACES)

    LaunchedEffect(workplacesCsv) { vm.applyWorkplacesCsv(workplacesCsv) }

    var cycleEdit by rememberSaveable { mutableStateOf("") }
    var nightEdit by rememberSaveable { mutableStateOf("") }
    var dayEdit by rememberSaveable { mutableStateOf("") }
    var workplacesEdit by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(cycleCsv) { cycleEdit = cycleCsv }
    LaunchedEffect(nightDefault) { nightEdit = nightDefault }
    LaunchedEffect(dayDefault) { dayEdit = dayDefault }
    LaunchedEffect(workplacesCsv) { workplacesEdit = workplacesCsv }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Surface(modifier = modifier.fillMaxSize()) {
            Column(
                Modifier
                    .padding(innerPadding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "설정",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                OutlinedCard(Modifier.fillMaxWidth()) {
                    Column(
                        Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("업무 사이클 (쉼표 구분)")
                        OutlinedTextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = cycleEdit,
                            onValueChange = { cycleEdit = it },
                            placeholder = { Text("주,주,야,야,휴,휴") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { focus.clearFocus() })
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = {
                                scope.launch {
                                    try {
                                        val app = context.applicationContext
                                        withContext(Dispatchers.IO) {
                                            app.dataStore.edit {
                                                it[KEY_WORK_CYCLE] = cycleEdit.trim()
                                            }
                                        }
                                        snackbarHostState.showSnackbar("업무 사이클을 저장했어요")
                                    } catch (e: Exception) {
                                        Log.e("SettingsScreen", "Failed to save work cycle", e)
                                        snackbarHostState.showSnackbar("저장 중 오류가 발생했어요: ${e.message ?: "unknown"}")
                                    }
                                }
                            }) { Text("저장") }
                            Text("현재: $cycleCsv", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }

                OutlinedCard(Modifier.fillMaxWidth()) {
                    Column(
                        Modifier.padding(7.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("출근 기본 시간 (주/야)")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                modifier = Modifier.weight(1f),
                                value = dayEdit,
                                onValueChange = { dayEdit = it },
                                label = { Text("주간 (HH:mm)") },
                                placeholder = { Text("09:00") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                            )
                            OutlinedTextField(
                                modifier = Modifier.weight(1f),
                                value = nightEdit,
                                onValueChange = { nightEdit = it },
                                label = { Text("야간 (HH:mm)") },
                                placeholder = { Text("22:00") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = { focus.clearFocus() })
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Button(onClick = {
                                scope.launch {
                                    try {
                                        val d = dayEdit.trim()
                                        val n = nightEdit.trim()
                                        if (!TIME_RE.matches(d) || !TIME_RE.matches(n)) {
                                            snackbarHostState.showSnackbar("형식 오류: HH:mm (예: 09:00, 22:00)")
                                            return@launch
                                        }
                                        val ym = YearMonth.now().toString()
                                        val app = context.applicationContext
                                        withContext(Dispatchers.IO) {
                                            app.dataStore.edit { prefs ->
                                                prefs[KEY_DAY_DEFAULT] = d
                                                prefs[KEY_NIGHT_DEFAULT] = n
                                                val latest =
                                                    parseCycleHistory(prefs[KEY_CYCLE_HISTORY]).toMutableMap()
                                                latest[ym] = "$d|$n"
                                                prefs[KEY_CYCLE_HISTORY] = joinCycleHistory(latest)
                                            }
                                        }
                                        snackbarHostState.showSnackbar("기본 시간과 월별 사이클을 저장했어요")
                                    } catch (e: Exception) {
                                        Log.e("SettingsScreen", "Failed to save defaults", e)
                                        snackbarHostState.showSnackbar("저장 중 오류가 발생했어요: ${e.message ?: "unknown"}")
                                    }
                                }
                            }) { Text("기본 시간 저장") }
                        }

                        Text("월별 사이클 히스토리", style = MaterialTheme.typography.titleSmall)
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            cycleHistoryMap.toSortedMap(compareByDescending { it })
                                .forEach { (k, v) ->
                                    val parts = v.split('|')
                                    val day = parts.getOrNull(0) ?: "—"
                                    val night = parts.getOrNull(1) ?: "—"
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(k)
                                        Text("주 $day / 야 $night")
                                    }
                                }
                        }
                    }
                }

                OutlinedCard(Modifier.fillMaxWidth()) {
                    Column(
                        Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("근무지 목록 (쉼표 구분)")
                        OutlinedTextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = workplacesEdit,
                            onValueChange = { workplacesEdit = it },
                            placeholder = { Text("siteA,siteB") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { focus.clearFocus() })
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = {
                                scope.launch {
                                    try {
                                        val app = context.applicationContext
                                        withContext(Dispatchers.IO) {
                                            app.dataStore.edit {
                                                val csv = workplacesEdit.trim()
                                                it[KEY_WORKPLACES] = csv
                                            }
                                        }
                                        vm.applyWorkplacesCsv(workplacesEdit.trim())
                                        snackbarHostState.showSnackbar("근무지 목록을 저장했어요")
                                    } catch (e: Exception) {
                                        Log.e("SettingsScreen", "Failed to save workplaces", e)
                                        snackbarHostState.showSnackbar("저장 중 오류가 발생했어요: ${e.message ?: "unknown"}")
                                    }
                                }
                            }) { Text("저장") }
                            Text("현재: $workplacesCsv", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }
    }
}
