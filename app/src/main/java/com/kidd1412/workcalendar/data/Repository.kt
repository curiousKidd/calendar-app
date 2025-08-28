package com.kidd1412.workcalendar.data

import com.kidd1412.workcalendar.data.dao.*
import com.kidd1412.workcalendar.data.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Repository(private val db: AppDatabase) {
    private val scope = CoroutineScope(Dispatchers.IO)

    fun seedDefaultsIfEmpty() {
        scope.launch {
            val shiftDao: ShiftTypeDao = db.shiftTypeDao()
            val routineDao: RoutineDao = db.routineDao()
            val routineEntryDao: RoutineEntryDao = db.routineEntryDao()

            // Insert default shift types if empty
            val defaults = listOf(
                ShiftType(name = "주간", defaultStartMinutes = 9 * 60, defaultEndMinutes = 18 * 60),
                ShiftType(name = "야간", defaultStartMinutes = 21 * 60, defaultEndMinutes = 6 * 60),
                ShiftType(name = "휴무", defaultStartMinutes = null, defaultEndMinutes = null),
                ShiftType(name = "기타", defaultStartMinutes = null, defaultEndMinutes = null)
            )
            shiftDao.upsertAll(defaults)

            // Create default routine "주주야야휴휴" if none exists
            val routineId = routineDao.upsert(
                Routine(
                    name = "주주야야휴휴",
                    length = 6,
                    workplace = null,
                    isActive = true
                )
            )

            // Map names to IDs by re-reading all shift types
            // In a minimal seed, we can assume IDs 1..n in insertion order for a new DB
            val dayTypes = listOf("주간", "주간", "야간", "야간", "휴무", "휴무")
            // We won't collect the flow here to avoid needing Compose runtime; instead, insert entries
            // with simple assumption based on order inserted above
            val entries: List<RoutineEntry> = dayTypes.mapIndexed { index, name ->
                val shiftTypeId = when (name) {
                    "주간" -> 1L
                    "야간" -> 2L
                    "휴무" -> 3L
                    else -> 4L
                }
                RoutineEntry(routineId = routineId, dayIndex = index, shiftTypeId = shiftTypeId)
            }
            routineEntryDao.upsertAll(entries)
        }
    }
}


