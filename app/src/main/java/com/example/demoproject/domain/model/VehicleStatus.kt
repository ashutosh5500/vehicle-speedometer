package com.example.demoproject.domain.model

/** Domain signals are independent: motor OFF does not imply a speed of zero. */
data class VehicleStatus(
    val speedKmh: Float? = null,
    val motorOn: Boolean? = null,
    val killSwitchActive: Boolean? = null,
    val scenario: String = "",
    val issue: SignalIssue = SignalIssue.NONE
)

enum class SignalIssue {
    NONE, INVALID_SPEED, MISSING_STATUS, MISSING_DATA, STALE, CONNECTION_ERROR
}
