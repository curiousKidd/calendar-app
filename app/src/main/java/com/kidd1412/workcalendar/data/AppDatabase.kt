package com.kidd1412.workcalendar.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.kidd1412.workcalendar.data.dao.*
import com.kidd1412.workcalendar.data.entity.*

class Converters {
	@TypeConverter
	fun toWorkplace(value: String?): Workplace? = value?.let { Workplace.valueOf(it) }

	@TypeConverter
	fun fromWorkplace(value: Workplace?): String? = value?.name
}

@Database(
	entities = [
		ShiftType::class,
		Routine::class,
		RoutineEntry::class,
		YearlyWorkplaceSelection::class,
		MonthlyNightOverride::class
	],
	version = 1,
	exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
	abstract fun shiftTypeDao(): ShiftTypeDao
	abstract fun routineDao(): RoutineDao
	abstract fun routineEntryDao(): RoutineEntryDao
	abstract fun yearlyWorkplaceDao(): YearlyWorkplaceDao
	abstract fun monthlyNightOverrideDao(): MonthlyNightOverrideDao
}
