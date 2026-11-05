# HMI Home / Ride Dashboard

A Kotlin / Jetpack Compose implementation of the Android HMI Developer L1 assignment.
The dashboard follows the reference's black background, large speed readout, ride-mode
strip, curved speed ribbon, and motor status pill. Kill-switch state is explicit and
uses both text and color.

## Build and run

- Open this directory in Android Studio and let Gradle sync.
- Use the IDE's bundled JDK (JDK 17 or newer), Android SDK 37, and the checked-in Gradle wrapper.
- Create/start an Android emulator with API 24 or newer. Select the app configuration and Run.
- The activity requests landscape. On large screens where Android ignores orientation
  locks, rotate the emulator to landscape. Content can scroll in short windows.
- No accounts, permissions, model downloads, vehicle hardware, CAN bus, or backend are required.

Commands from the repository root:

```sh
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
./gradlew :app:lintDebug
```

APK: `app/build/outputs/apk/debug/app-debug.apk`.
Test report: `app/build/reports/tests/testDebugUnitTest/index.html`.

## Automatic demonstration

The provider emits a fresh sample approximately every second. Each scenario lasts
three samples so it can be read comfortably. The entire 21-second sequence repeats:

| Time | Scenario | Speed (km/h) | Motor | Kill switch |
| --- | --- | --- | --- | --- |
| 0–3 s | Parked | 0 | OFF | Inactive |
| 3–6 s | Ready | 0 | ON | Inactive |
| 6–9 s | Riding | 25 | ON | Inactive |
| 9–12 s | Accelerating | 50 | ON | Inactive |
| 12–15 s | Motor disabled | 0 | OFF | Active |
| 15–18 s | Invalid speed | Unavailable | ON | Inactive |
| 18–21 s | Missing data | Unavailable | Unknown | Unknown |

No refresh or user action is necessary.

## Architecture

- `data/VehicleDataSource.kt`: nullable raw signal model, injectable Flow interface,
  and coroutine-based simulated provider. Delays are cancellable.
- `dashboard/DashboardViewModel.kt`: owns read-only StateFlow, validates samples,
  handles source errors, and detects silence with a 3.5-second watchdog.
- `dashboard/DashboardScreen.kt`: stateless Compose dashboard and Canvas speed ribbon.
- `MainActivity.kt`: obtains the ViewModel and collects state with lifecycle awareness.
  ViewModel state survives configuration recreation; a new process starts the demo again.

Constructor injection allows tests to supply a fake source without a DI framework.
The assignment does not require Hilt, multiple Gradle modules, caching, or a backend.

## Edge cases and assumptions

- Negative, non-finite, missing, or over-200 km/h speeds display an em dash and a
  speed-signal notice; valid independent motor/kill-switch signals remain visible.
  The 200 km/h maximum is a demo validation assumption.
- A missing sample clears all signals to unknown instead of implying OFF or zero.
- No sample for 3.5 seconds clears the display with a stale-data notice. Fresh data
  automatically restores the dashboard.
- A provider exception shows a connection-error notice without crashing. A failed
  source is not automatically retried; the default local simulator does not throw.
- Motor OFF does not force speed to zero. A unit test covers coasting with motor OFF.
- Reference extras such as battery percentage, range, notifications, maps, cruise
  control, and hardware warning lamps are omitted because no corresponding data
  source is required. The screen explicitly identifies all values as simulated.
- No real motor or safety control is connected; kill switch is a displayed signal.

## Tests

JUnit4 and kotlinx-coroutines-test use virtual time; no device is needed for unit tests.
The dashboard test suite covers simulation cadence and all five scenarios, reactive
ViewModel updates, missing-data recovery, invalid speeds, independent motor/speed
signals, stale-source recovery, and source exceptions. See [docs/testing.md](docs/testing.md).

For a manual acceptance pass, run the emulator in landscape and watch one complete
21-second loop. Verify the five scenarios, orange ACTIVE kill switch, invalid-speed
em dash, missing-data UNKNOWN labels, and automatic return to Parked.

## Submission

Complete source and build/test instructions are included. Publish this project to
your GitHub account and provide that repository URL with the assignment submission.
A remote repository has not been created by this project.
