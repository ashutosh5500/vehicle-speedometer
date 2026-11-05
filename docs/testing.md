# Testing strategy

The starter project already supplied JUnit4, AndroidX instrumentation/Espresso,
and Compose instrumentation test dependencies. It had no mocking, database,
screenshot, or dependency injection framework. Production UI is entirely Compose.

The assignment's business logic is tested locally with JUnit4 and
kotlinx-coroutines-test. Sources are injected through VehicleDataSource, so tests
use real Flow objects as fakes and do not require mocks or Android services.
Virtual time makes the one-second cadence and stale-data watchdog deterministic.

Run `./gradlew :app:testDebugUnitTest`. DashboardViewModelTest covers the provider,
mapper, ViewModel, recovery, and failure paths. The default starter example tests
remain separate from the assignment tests.

Run `./gradlew :app:assembleDebug :app:lintDebug` for compilation and static checks.
For UI acceptance, run in a landscape emulator and observe a complete 21-second loop.
Check text as well as colors for motor, kill switch, and unknown signals.
A Compose preview is provided in DashboardScreen.kt for layout review.

No screenshot baselines or extra test infrastructure are required for this assignment.
