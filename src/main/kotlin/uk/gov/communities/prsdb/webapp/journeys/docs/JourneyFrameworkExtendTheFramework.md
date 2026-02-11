# Extending the Journey Framework
This document covers advanced topics for developers who need to extend or modify the framework.

## Extending the DSL

The DSL is built using Kotlin builder patterns. Key extension points:

- **`JourneyBuilderDsl`** interface: Defines `step()` and `task()` functions
- **`StepInitialiser`**: Configures individual steps with `nextStep()`, `parents()`, `routeSegment()`, etc.
- **`TaskInitialiser`**: Configures tasks within journeys

To add new DSL functions, extend the relevant initialiser classes in `builders/`.

See: https://kotlinlang.org/docs/type-safe-builders.html

## Unit Tests

Unit tests for the framework live in `src/test/kotlin/uk/gov/communities/prsdb/webapp/journeys/`:

| Test File | Description |
|-----------|-------------|
| `JourneyStepTests.kt` | Tests for individual step behaviour and configuration |
| `StepConfigTests.kt` | Tests for [step configuration](JourneyFrameworkReadMe.md#glossary-of-terms) classes |
| `StepLifecycleOrchestratorTest.kt` | Tests for the request lifecycle handling |
| `ParentageTests.kt` | Tests for [parent](JourneyFrameworkReadMe.md#glossary-of-terms)/child relationships between steps |
| `TaskTests.kt` | Tests for task (step group) functionality |
| `AbstractJourneyStateTests.kt` | Tests for [journey state](JourneyFrameworkReadMe.md#glossary-of-terms) persistence |
| `builders/JourneyBuilderTest.kt` | Tests for the DSL builder |
