package com.example.demoproject.dashboard

import com.example.demoproject.domain.model.SignalIssue
import com.example.demoproject.domain.model.VehicleStatus

data class DashboardState(
    val speedKmh: Float? = null,
    val motorOn: Boolean? = null,
    val killSwitchActive: Boolean? = null,
    val scenario: String = "Connecting",
    val notice: String? = "Waiting for vehicle data"
)

/** Presentation labels are kept outside the domain and data layers. */
internal fun VehicleStatus.toDashboardState(): DashboardState = DashboardState(
    speedKmh = speedKmh,
    motorOn = motorOn,
    killSwitchActive = killSwitchActive,
    scenario = when (issue) {
        SignalIssue.MISSING_DATA -> "Signal unavailable"
        SignalIssue.STALE -> "Signal stale"
        SignalIssue.CONNECTION_ERROR -> "Connection error"
        else -> scenario
    },
    notice = when (issue) {
        SignalIssue.NONE -> null
        SignalIssue.INVALID_SPEED -> "Speed signal unavailable"
        SignalIssue.MISSING_STATUS -> "Status signal unavailable"
        SignalIssue.MISSING_DATA -> "Vehicle data unavailable"
        SignalIssue.STALE -> "No fresh data · waiting to reconnect"
        SignalIssue.CONNECTION_ERROR -> "Vehicle data connection lost"
    }
)
