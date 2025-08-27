package com.kidd1412.workcalendar.data.dao

import androidx.room.*
import com.kidd1412.workcalendar.data.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ShiftTypeDao {
	@Query("SELECT * FROM shift_types")
	fun getAll(): Flow<List<ShiftType>>

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun upsertAll(types: List<ShiftType>)
}

@Dao
interface RoutineDao {
	@Query("SELECT * FROM routines")
	fun getAll(): Flow<List<Routine>>

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun upsert(routine: Routine): Long
}

@Dao
interface RoutineEntryDao {
	@Query("SELECT * FROM routine_entries WHERE routineId = :routineId ORDER BY dayIndex ASC")
	fun entriesForRoutine(routineId: Long): Flow<List<RoutineEntry>>

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun upsertAll(entries: List<RoutineEntry>)
}

@Dao
interface YearlyWorkplaceDao {
	@Query("SELECT * FROM yearly_workplace WHERE year = :year LIMIT 1")
	fun get(year: Int): Flow<YearlyWorkplaceSelection?>

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun upsert(selection: YearlyWorkplaceSelection)
}

@Dao
interface MonthlyNightOverrideDao {
	@Query("SELECT * FROM monthly_night_override WHERE year = :year AND month = :month LIMIT 1")
	fun get(year: Int, month: Int): Flow<MonthlyNightOverride?>

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun upsert(override: MonthlyNightOverride)
}
