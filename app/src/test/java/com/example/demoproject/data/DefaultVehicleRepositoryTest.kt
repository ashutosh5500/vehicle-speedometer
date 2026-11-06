package com.example.demoproject.data

import com.example.demoproject.data.repository.DefaultVehicleRepository
import com.example.demoproject.domain.model.SignalIssue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultVehicleRepositoryTest {
    @Test fun simulatedSourceCoversFiveScenariosAtOneSecondIntervals() = runTest {
        val samples = mutableListOf<VehicleData?>()
        val job = launch { SimulatedVehicleDataSource().observe().take(22).toList(samples) }
        runCurrent()
        assertEquals(1, samples.size)
        advanceTimeBy(1_000)
        runCurrent()
        assertEquals(2, samples.size)
        advanceTimeBy(20_000)
        runCurrent()
        assertEquals(listOf(0f, 0f, 25f, 50f, 0f), samples.take(15).filterIndexed { i, _ -> i % 3 == 0 }.map { it?.speedKmh })
        assertEquals(listOf(false, true, true, true, false), samples.take(15).filterIndexed { i, _ -> i % 3 == 0 }.map { it?.motorOn })
        assertEquals(true, samples[12]?.killSwitchActive)
        assertEquals(-5f, samples[15]?.speedKmh)
        assertNull(samples[18])
        assertEquals("Parked", samples[21]?.scenario)
        job.join()
    }

    @Test fun invalidSpeedsAreUnavailableButIndependentSignalsArePreserved() = runTest {
        listOf(-1f, Float.NaN, Float.POSITIVE_INFINITY, 201f, null).forEach { speed ->
            val repository = DefaultVehicleRepository(VehicleDataSource {
                flowOf(VehicleData(speed, true, false, "Invalid"))
            })
            val status = repository.observe().first()
            assertNull(status.speedKmh)
            assertEquals(true, status.motorOn)
            assertEquals(false, status.killSwitchActive)
            assertEquals(SignalIssue.INVALID_SPEED, status.issue)
        }
    }

    @Test fun missingSampleMakesAllSignalsUnknown() = runTest {
        val repository = DefaultVehicleRepository(VehicleDataSource { flowOf(null) })
        val status = repository.observe().first()
        assertEquals(SignalIssue.MISSING_DATA, status.issue)
        assertNull(status.speedKmh)
        assertNull(status.motorOn)
        assertNull(status.killSwitchActive)
    }

    @Test fun missingStatusPreservesValidSpeed() = runTest {
        val repository = DefaultVehicleRepository(VehicleDataSource {
            flowOf(VehicleData(25f, null, false, "Partial"))
        })
        val status = repository.observe().first()
        assertEquals(25f, status.speedKmh)
        assertNull(status.motorOn)
        assertEquals(false, status.killSwitchActive)
        assertEquals(SignalIssue.MISSING_STATUS, status.issue)
    }

    @Test fun motorOffDoesNotEraseMovingSpeed() = runTest {
        val repository = DefaultVehicleRepository(VehicleDataSource {
            flowOf(VehicleData(12f, false, true, "Coasting"))
        })
        val status = repository.observe().first()
        assertEquals(12f, status.speedKmh)
        assertEquals(false, status.motorOn)
        assertEquals(true, status.killSwitchActive)
        assertEquals(SignalIssue.NONE, status.issue)
    }
}
