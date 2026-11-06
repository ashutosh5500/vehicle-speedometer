package com.example.demoproject.data.repository

import com.example.demoproject.data.VehicleData
import com.example.demoproject.data.VehicleDataSource
import com.example.demoproject.domain.model.SignalIssue
import com.example.demoproject.domain.model.VehicleStatus
import com.example.demoproject.domain.repository.VehicleRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DefaultVehicleRepository @Inject constructor(
    private val source: VehicleDataSource
) : VehicleRepository {
    override fun observe(): Flow<VehicleStatus> = source.observe().map { it.toDomain() }
}

/** Validate at the data boundary; never manufacture OFF or zero for missing signals. */
private fun VehicleData?.toDomain(): VehicleStatus {
    if (this == null) return VehicleStatus(issue = SignalIssue.MISSING_DATA)
    val validSpeed = speedKmh?.takeIf { it.isFinite() && it in 0f..200f }
    return VehicleStatus(
        speedKmh = validSpeed,
        motorOn = motorOn,
        killSwitchActive = killSwitchActive,
        scenario = scenario,
        issue = when {
            validSpeed == null -> SignalIssue.INVALID_SPEED
            motorOn == null || killSwitchActive == null -> SignalIssue.MISSING_STATUS
            else -> SignalIssue.NONE
        }
    )
}
