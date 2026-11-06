package com.example.demoproject.domain.usecase

import com.example.demoproject.domain.model.SignalIssue
import com.example.demoproject.domain.model.VehicleStatus
import com.example.demoproject.domain.repository.VehicleRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch

/**
 * Observes validated signals, invalidates silent sources after 3.5 seconds, and
 * converts source failures into domain states. Coroutine cancellation propagates.
 */
class ObserveVehicleStatusUseCase(
    private val repository: VehicleRepository
) {
    operator fun invoke(): Flow<VehicleStatus> = channelFlow {
        fun startWatchdog() = launch {
            delay(STALE_AFTER_MILLIS)
            send(VehicleStatus(issue = SignalIssue.STALE))
        }

        var watchdog = startWatchdog()
        try {
            repository.observe().catch {
                watchdog.cancel()
                emit(VehicleStatus(issue = SignalIssue.CONNECTION_ERROR))
            }.collect { status ->
                watchdog.cancel()
                send(status)
                watchdog = startWatchdog()
            }
        } finally {
            watchdog.cancel()
        }
    }

    private companion object {
        const val STALE_AFTER_MILLIS = 3_500L
    }
}
