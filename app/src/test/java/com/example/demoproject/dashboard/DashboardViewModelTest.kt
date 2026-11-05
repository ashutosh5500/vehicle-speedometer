package com.example.demoproject.dashboard

import com.example.demoproject.data.VehicleData
import com.example.demoproject.data.VehicleDataSource
import com.example.demoproject.data.SimulatedVehicleDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before fun setup() { Dispatchers.setMain(dispatcher) }
    @After fun teardown() { Dispatchers.resetMain() }

    @Test fun simulatedSourceCoversAllFiveScenariosAtOneSecondIntervals() = runTest(dispatcher) {
        val samples = mutableListOf<VehicleData?>()
        val job = launch { SimulatedVehicleDataSource().observe().take(15).toList(samples) }
        runCurrent()
        assertEquals(1, samples.size)
        advanceTimeBy(1_000)
        runCurrent()
        assertEquals(2, samples.size)
        advanceTimeBy(13_000)
        runCurrent()
        assertEquals(listOf(0f, 0f, 25f, 50f, 0f), samples.filterIndexed { i, _ -> i % 3 == 0 }.map { it?.speedKmh })
        assertEquals(listOf(false, true, true, true, false), samples.filterIndexed { i, _ -> i % 3 == 0 }.map { it?.motorOn })
        assertEquals(true, samples.last()?.killSwitchActive)
        job.join()
    }

    @Test fun viewModelUpdatesFromSourceAndRecoversAfterMissingData() = runTest(dispatcher) {
        val updates = MutableSharedFlow<VehicleData?>()
        val model = DashboardViewModel(VehicleDataSource { updates })
        runCurrent()
        updates.emit(VehicleData(25f, true, false, "Riding"))
        runCurrent()
        assertEquals(25f, model.state.value.speedKmh)
        assertEquals(true, model.state.value.motorOn)
        updates.emit(null)
        runCurrent()
        assertNull(model.state.value.speedKmh)
        assertNull(model.state.value.motorOn)
        assertEquals("Vehicle data unavailable", model.state.value.notice)
        updates.emit(VehicleData(50f, true, false, "Accelerating"))
        runCurrent()
        assertEquals(50f, model.state.value.speedKmh)
        assertNull(model.state.value.notice)
        // Cancel the independently owned ViewModel jobs at test teardown.
        model.viewModelScopeForTestCancel()
    }

    @Test fun invalidSpeedsAreUnavailableWhileOtherSignalsRemainUsable() {
        listOf(-1f, Float.NaN, Float.POSITIVE_INFINITY, 201f, null).forEach { speed ->
            val state = VehicleData(speed, true, false, "Invalid").toDashboardState()
            assertNull(state.speedKmh)
            assertEquals(true, state.motorOn)
            assertEquals(false, state.killSwitchActive)
            assertEquals("Speed signal unavailable", state.notice)
        }
    }

    @Test fun motorOffDoesNotEraseAValidMovingSpeed() {
        val state = VehicleData(12f, false, true, "Coasting").toDashboardState()
        assertEquals(12f, state.speedKmh)
        assertEquals(false, state.motorOn)
        assertEquals(true, state.killSwitchActive)
    }

    @Test fun silentSourceBecomesStaleAndFreshSampleRecovers() = runTest(dispatcher) {
        val updates = MutableSharedFlow<VehicleData?>()
        val model = DashboardViewModel(VehicleDataSource { updates })
        runCurrent()
        updates.emit(VehicleData(25f, true, false, "Riding"))
        runCurrent()
        advanceTimeBy(3_501)
        runCurrent()
        assertEquals("Signal stale", model.state.value.scenario)
        assertNull(model.state.value.speedKmh)
        updates.emit(VehicleData(0f, true, false, "Ready"))
        runCurrent()
        assertEquals("Ready", model.state.value.scenario)
        assertNull(model.state.value.notice)
        model.viewModelScopeForTestCancel()
    }

    @Test fun sourceFailureShowsUnknownSignalsInsteadOfCrashing() = runTest(dispatcher) {
        val model = DashboardViewModel(VehicleDataSource { flow { throw IllegalStateException("offline") } })
        runCurrent()
        assertEquals("Connection error", model.state.value.scenario)
        assertNull(model.state.value.motorOn)
        assertNotNull(model.state.value.notice)
        model.viewModelScopeForTestCancel()
    }
}

private fun DashboardViewModel.viewModelScopeForTestCancel() {
    viewModelScope.cancel()
}
