package com.kidd1412.workcalendar.data

import androidx.datastore.preferences.core.stringPreferencesKey

// Preference Keys
val KEY_WORK_CYCLE = stringPreferencesKey("work_cycle_csv")
val KEY_NIGHT_DEFAULT = stringPreferencesKey("night_default_hhmm")
val KEY_NIGHT_OVERRIDES = stringPreferencesKey("night_overrides") // legacy
val KEY_DAY_DEFAULT = stringPreferencesKey("day_default_hhmm")
val KEY_WORKPLACES = stringPreferencesKey("workplaces_csv")
val KEY_CYCLE_HISTORY = stringPreferencesKey("cycle_history") // format: YYYY-MM=DAY|NIGHT,...

// Default Values
val DEFAULT_WORK_CYCLE = listOf("주", "주", "야", "야", "휴", "휴")
const val DEFAULT_NIGHT_TIME = "18:00"
const val DEFAULT_DAY_TIME = "09:00"
const val DEFAULT_WORKPLACES = "계양,공항"
