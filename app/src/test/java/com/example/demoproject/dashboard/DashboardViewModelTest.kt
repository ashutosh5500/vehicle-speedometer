package com.example.demoproject.dashboard

import androidx.lifecycle.viewModelScope
import com.example.demoproject.domain.model.SignalIssue
import com.example.demoproject.domain.model.VehicleStatus
import com.example.demoproject.domain.repository.VehicleRepository
import com.example.demoproject.domain.usecase.ObserveVehicleStatusUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.StandardTestDispatcher
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

    @Test fun stateUpdatesFromDomainAndRecoversAfterMissingData() = runTest(dispatcher) {
        val updates = MutableSharedFlow<VehicleStatus>()
        val model = DashboardViewModel(ObserveVehicleStatusUseCase(VehicleRepository { updates }))
        try {
            assertEquals("Connecting", model.state.value.scenario)
            runCurrent()
            updates.emit(VehicleStatus(25f, true, false, "Riding"))
            runCurrent()
            assertEquals(25f, model.state.value.speedKmh)
            assertEquals(true, model.state.value.motorOn)

            updates.emit(VehicleStatus(issue = SignalIssue.MISSING_DATA))
            runCurrent()
            assertNull(model.state.value.speedKmh)
            assertNull(model.state.value.motorOn)
            assertEquals("Vehicle data unavailable", model.state.value.notice)

            updates.emit(VehicleStatus(50f, true, false, "Accelerating"))
            runCurrent()
            assertEquals(50f, model.state.value.speedKmh)
            assertNull(model.state.value.notice)
        } finally {
            model.viewModelScope.cancel()
        }
    }

    @Test fun mapsEveryDomainIssueToPresentationLabels() {
        val expected = mapOf(
            SignalIssue.INVALID_SPEED to "Speed signal unavailable",
            SignalIssue.MISSING_STATUS to "Status signal unavailable",
            SignalIssue.MISSING_DATA to "Vehicle data unavailable",
            SignalIssue.STALE to "No fresh data · waiting to reconnect",
            SignalIssue.CONNECTION_ERROR to "Vehicle data connection lost"
        )
        expected.forEach { (issue, notice) ->
            val state = VehicleStatus(issue = issue).toDashboardState()
            assertEquals(notice, state.notice)
            assertNull(state.speedKmh)
            assertNull(state.motorOn)
            assertNull(state.killSwitchActive)
        }
        assertEquals("Signal stale", VehicleStatus(issue = SignalIssue.STALE).toDashboardState().scenario)
        assertEquals("Connection error", VehicleStatus(issue = SignalIssue.CONNECTION_ERROR).toDashboardState().scenario)
        assertNull(VehicleStatus().toDashboardState().notice)
    }
}
