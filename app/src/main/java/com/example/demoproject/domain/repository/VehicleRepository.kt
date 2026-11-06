package com.example.demoproject.domain.repository

import com.example.demoproject.domain.model.VehicleStatus
import kotlinx.coroutines.flow.Flow

/** Exposes validated signals without revealing the vehicle data source. */
fun interface VehicleRepository {
    fun observe(): Flow<VehicleStatus>
}
