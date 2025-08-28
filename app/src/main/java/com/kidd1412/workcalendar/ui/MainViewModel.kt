package com.kidd1412.workcalendar.ui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MainViewModel : ViewModel() {
	private val _currentWorkplaceLabel = MutableStateFlow("SiteA")
	val currentWorkplaceLabel: StateFlow<String> = _currentWorkplaceLabel

	fun toggleWorkplace() {
		_currentWorkplaceLabel.value = if (_currentWorkplaceLabel.value == "SiteA") "SiteB" else "SiteA"
	}
}


