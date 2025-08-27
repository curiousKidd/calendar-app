package com.kidd1412.workcalendar.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class Workplace { SiteA, SiteB }

@Entity(tableName = "shift_types")
data class ShiftType(
	@PrimaryKey(autoGenerate = true) val id: Long = 0,
	val name: String,
	val defaultStartMinutes: Int?,
	val defaultEndMinutes: Int?
)

@Entity(tableName = "routines")
data class Routine(
	@PrimaryKey(autoGenerate = true) val id: Long = 0,
	val name: String,
	val length: Int,
	val workplace: Workplace?,
	val isActive: Boolean = false
)

@Entity(tableName = "routine_entries")
data class RoutineEntry(
	@PrimaryKey(autoGenerate = true) val id: Long = 0,
	val routineId: Long,
	val dayIndex: Int,
	val shiftTypeId: Long
)

@Entity(tableName = "yearly_workplace")
data class YearlyWorkplaceSelection(
	@PrimaryKey val year: Int,
	val workplace: Workplace
)

@Entity(tableName = "monthly_night_override")
data class MonthlyNightOverride(
	@PrimaryKey(autoGenerate = true) val id: Long = 0,
	val year: Int,
	val month: Int,
	val workplace: Workplace?,
	val nightStartMinutes: Int
)
