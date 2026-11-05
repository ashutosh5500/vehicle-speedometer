package com.example.demoproject.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.demoproject.data.SimulatedVehicleDataSource
import com.example.demoproject.data.VehicleData
import com.example.demoproject.data.VehicleDataSource
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

data class DashboardState(
    val speedKmh: Float? = null,
    val motorOn: Boolean? = null,
    val killSwitchActive: Boolean? = null,
    val scenario: String = "Connecting",
    val notice: String? = "Waiting for vehicle data"
)

internal fun VehicleData?.toDashboardState(): DashboardState {
    if (this == null) return DashboardState(scenario = "Signal unavailable", notice = "Vehicle data unavailable")
    val validSpeed = speedKmh?.takeIf { it.isFinite() && it >= 0f && it <= 200f }
    return DashboardState(
        speedKmh = validSpeed,
        motorOn = motorOn,
        killSwitchActive = killSwitchActive,
        scenario = scenario,
        notice = when {
            validSpeed == null -> "Speed signal unavailable"
            motorOn == null || killSwitchActive == null -> "Status signal unavailable"
            else -> null
        }
    )
}

class DashboardViewModel(
    source: VehicleDataSource = SimulatedVehicleDataSource()
) : ViewModel() {
    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            fun startWatchdog() = launch {
                delay(3_500)
                _state.value = DashboardState(
                    scenario = "Signal stale",
                    notice = "No fresh data · waiting to reconnect"
                )
            }
            var watchdog = startWatchdog()
            try {
                source.observe().catch {
                    _state.value = DashboardState(scenario = "Connection error", notice = "Vehicle data connection lost")
                }.collect { sample ->
                    watchdog.cancel()
                    _state.value = sample.toDashboardState()
                    watchdog = startWatchdog()
                }
            } finally {
                watchdog.cancel()
            }
        }
    }
}
