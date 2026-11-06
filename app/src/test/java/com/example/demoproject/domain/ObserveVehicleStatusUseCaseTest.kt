package com.example.demoproject.domain

import com.example.demoproject.domain.model.SignalIssue
import com.example.demoproject.domain.model.VehicleStatus
import com.example.demoproject.domain.repository.VehicleRepository
import com.example.demoproject.domain.usecase.ObserveVehicleStatusUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ObserveVehicleStatusUseCaseTest {
    @Test fun silentSourceBecomesStaleAndFreshSampleRecovers() = runTest {
        val updates = MutableSharedFlow<VehicleStatus>()
        val useCase = ObserveVehicleStatusUseCase(VehicleRepository { updates })
        val received = mutableListOf<VehicleStatus>()
        val job = backgroundScope.launch { useCase().collect { received += it } }
        runCurrent()
        updates.emit(VehicleStatus(25f, true, false, "Riding"))
        runCurrent()
        advanceTimeBy(3_499)
        runCurrent()
        assertEquals(25f, received.last().speedKmh)
        advanceTimeBy(1)
        runCurrent()
        assertEquals(SignalIssue.STALE, received.last().issue)
        assertNull(received.last().speedKmh)
        assertNull(received.last().motorOn)
        updates.emit(VehicleStatus(0f, true, false, "Ready"))
        runCurrent()
        assertEquals("Ready", received.last().scenario)
        assertEquals(SignalIssue.NONE, received.last().issue)
        job.cancel()
    }

    @Test fun everySampleResetsStaleDeadline() = runTest {
        val updates = MutableSharedFlow<VehicleStatus>()
        val received = mutableListOf<VehicleStatus>()
        val useCase = ObserveVehicleStatusUseCase(VehicleRepository { updates })
        backgroundScope.launch { useCase().collect { received += it } }
        runCurrent()
        updates.emit(VehicleStatus(0f, true, false, "Ready"))
        runCurrent()
        advanceTimeBy(3_000)
        updates.emit(VehicleStatus(25f, true, false, "Riding"))
        runCurrent()
        advanceTimeBy(501)
        runCurrent()
        assertEquals("Riding", received.last().scenario)
        assertEquals(SignalIssue.NONE, received.last().issue)
    }

    @Test fun noInitialSampleAlsoBecomesStale() = runTest {
        val useCase = ObserveVehicleStatusUseCase(VehicleRepository { flow { awaitCancellation() } })
        val received = mutableListOf<VehicleStatus>()
        backgroundScope.launch { useCase().collect { received += it } }
        runCurrent()
        advanceTimeBy(3_500)
        runCurrent()
        assertEquals(SignalIssue.STALE, received.single().issue)
    }

    @Test fun sourceExceptionBecomesConnectionError() = runTest {
        val useCase = ObserveVehicleStatusUseCase(VehicleRepository {
            flow { throw IllegalStateException("offline") }
        })
        val status = useCase().first()
        assertEquals(SignalIssue.CONNECTION_ERROR, status.issue)
        assertNull(status.speedKmh)
        assertNull(status.motorOn)
    }

    @Test fun cancellationReachesSourceWithoutBecomingConnectionError() = runTest {
        var sourceCancelled = false
        val received = mutableListOf<VehicleStatus>()
        val useCase = ObserveVehicleStatusUseCase(VehicleRepository {
            flow {
                try {
                    awaitCancellation()
                } catch (cancelled: CancellationException) {
                    sourceCancelled = true
                    throw cancelled
                }
            }
        })
        val job = launch { useCase().collect { received += it } }
        runCurrent()
        job.cancel()
        job.join()
        assertTrue(sourceCancelled)
        assertTrue(received.isEmpty())
    }
}
