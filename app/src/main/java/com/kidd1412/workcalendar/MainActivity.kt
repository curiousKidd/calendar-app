package com.kidd1412.workcalendar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.kidd1412.workcalendar.ui.WorkCalendarApp
import com.kidd1412.workcalendar.data.DatabaseProvider
import com.kidd1412.workcalendar.data.Repository

class MainActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		// Initialize database and seed defaults on first launch
		val db = DatabaseProvider.get(this)
		Repository(db).seedDefaultsIfEmpty()
		setContent {
			MaterialTheme {
				Surface(color = MaterialTheme.colorScheme.background) {
					WorkCalendarApp()
				}
			}
		}
	}
}
