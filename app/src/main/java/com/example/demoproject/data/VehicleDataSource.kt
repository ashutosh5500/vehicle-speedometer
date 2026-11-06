package com.example.demoproject.data

import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

data class VehicleData(
    val speedKmh: Float?,
    val motorOn: Boolean?,
    val killSwitchActive: Boolean?,
    val scenario: String
)

fun interface VehicleDataSource {
    fun observe(): Flow<VehicleData?>
}

/** Repeats the five assignment scenarios, then two deliberate signal failures. */
class SimulatedVehicleDataSource @Inject constructor() : VehicleDataSource {
    override fun observe(): Flow<VehicleData?> = flow {
        val scenarios = listOf(
            VehicleData(0f, false, false, "Parked"),
            VehicleData(0f, true, false, "Ready"),
            VehicleData(25f, true, false, "Riding"),
            VehicleData(50f, true, false, "Accelerating"),
            VehicleData(0f, false, true, "Motor disabled"),
            VehicleData(-5f, true, false, "Invalid speed"),
            null
        )
        while (true) {
            for (sample in scenarios) {
                // Hold each scenario for three seconds, with a fresh sample each second.
                repeat(3) {
                    emit(sample)
                    delay(1_000)
                }
            }
        }
    }
}
