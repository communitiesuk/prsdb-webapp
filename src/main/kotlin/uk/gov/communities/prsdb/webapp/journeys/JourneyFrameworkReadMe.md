# Journey Framework

The Journey Framework is designed to make creating multi-page form journeys quick and easy without compromising on flexibility.

## Overview

The framework aims to:
- Commonise behaviours of multi-page form journeys without making it difficult to implement custom requirements
- Allow pages and groups of pages to be reused across different journeys
- Separate the structure of journeys from the content to make both easier to maintain and update

## Quick Start

### Creating a New Journey Checklist

1. Create step classes (define pages, validation, and data storage)
2. Define a state interface for your journey
3. Implement a journey state class
4. Define the journey structure using the DSL
5. Add controller methods for GET and POST requests
6. Write tests

### Minimal Journey Example

```kotlin
val simpleJourney = journey(state) {
    step(journey.firstStep) {
        nextStep { journey.secondStep }
        routeSegment("step-1")
        initialStep()
    }
    step(journey.secondStep) {
        nextUrl { "/home" }
        routeSegment("step-2")
        parents { journey.firstStep.isComplete() }
    }
}
```

## Core Concepts

### Graphs and Journey Elements

Journeys are modelled as directed graphs, where nodes are called "Journey Elements" or simply "Elements".
Elements can be steps (pages) or tasks (groups of steps).

A simple linear journey looks like this:

```mermaid
flowchart TD
A[What is your name?] --> B[What is your quest?] --> C[What is your favourite colour?] --> D[Check your answers] --> E([Save to the database])
```

Each edge represents both the transition from one page to the next AND the requirements for a page to be visitable—all previous pages must have been completed.

### Branching Paths

Journeys can have branching paths based on user answers:

```mermaid
flowchart TD
A[A] --> B[B]
B --> C[What is the airspeed velocity of an unladen swallow?]
C -- Over 330 mph --> D[D]
D --> E[E] --> G
C -- Under 330 mph --> F[F]
F --> G[Check your answers] --> H([Save to the database])
```

For any given answer, the requirements for a page to be visitable are that all previous pages on the path taken must have been completed.

### Parallel Requirements

Sometimes users must complete multiple pages before proceeding, but can visit them in any order:

```mermaid
flowchart TD
A[These questions all need answering] --o B[What is your favourite book?]
B --> C[Who is it by?]
A --o D[How many pets do you have?]
D --X F[Check your answers] --> G([Save to the database])
C --X F
```

### Modes and Outcomes

**Modes** represent the possible answers on a page that affect journey structure. The mode is determined by the user's answer (or external factors), not by direct user selection.

- Modes are specified using an enum class when defining a step
- If the page has not been answered yet, the mode is `null`

**Outcomes** are similar to modes but account for journey structure—they determine reachability. An outcome is the same as the mode if the page is reachable, and `null` otherwise.

## Defining Journey Structure

The journey structure is defined using a Kotlin DSL. For each element, you define:
- Where the user goes when completing the element (for each mode)
- What previous elements must be completed for this element to be visitable (parents)
- Where to redirect if the element is not visitable
- The URL route segment for the element (if applicable)

### Basic Structure

```kotlin
val simpleJourney = journey(state) {
    step(journey.nameStep) {
        nextStep { journey.questStep }
        routeSegment("name")
        initialStep()
    }
    step(journey.questStep) {
        nextStep { journey.colourStep }
        routeSegment("quest")
        parents { journey.nameStep.isComplete() }
    }
    step(journey.colourStep) {
        nextStep { journey.checkAnswersStep }
        routeSegment("colour")
        parents { journey.questStep.isComplete() }
    }
    step(journey.checkAnswersStep) {
        nextUrl { "/home" }
        parents { journey.colourStep.isComplete() }
        routeSegment("check-answers")
    }
}
```

### Branching Based on Mode

```kotlin
step(journey.swallowSpeedStep) {
    nextStep {
        when (it) {
            SwallowSpeed.OVER_330 -> journey.fastSwallowStep
            SwallowSpeed.UNDER_330 -> journey.slowSwallowStep
        }
    }
    routeSegment("swallow-speed")
    parents { journey.nameStep.isComplete() }
}
step(journey.fastSwallowStep) {
    nextStep { journey.checkAnswersStep }
    routeSegment("fast-swallow")
    parents { journey.swallowSpeedStep.hasOutcome(SwallowSpeed.OVER_330) }
}
step(journey.slowSwallowStep) {
    nextStep { journey.checkAnswersStep }
    routeSegment("slow-swallow")
    parents { journey.swallowSpeedStep.hasOutcome(SwallowSpeed.UNDER_330) }
}
```

> **Note:** `nextStep` and `parents` are independent concepts. `nextStep` defines where a user is redirected; `parents` defines when a user can visit a step. It's possible to redirect to an unreachable step.

## Tasks

Tasks are groups of steps that are always completed together with the same internal structure. They allow reuse of step sequences across different journeys.

### Defining a Task

Create a subclass of `Task` and override `makeSubJourney` with the internal structure:

```kotlin
override fun makeSubJourney(state: OccupationState) =
    subJourney(state) {
        step(journey.nameStep) {
            nextStep { journey.dateOfBirthStep }
            routeSegment("name")
            initialStep()
        }
        step(journey.dateOfBirthStep) {
            nextStep {
                when (it) {
                    AgeCategory.MINOR -> journey.parentalConsentStep
                    AgeCategory.ADULT -> journey.addressStep
                }
            }
            routeSegment("date-of-birth")
            parents { journey.nameStep.isComplete() }
        }
        step(journey.parentalConsentStep) {
            nextStep { journey.addressStep }
            routeSegment("parental-consent")
            parents { journey.dateOfBirthStep.hasOutcome(AgeCategory.MINOR) }
        }
        step(journey.addressStep) {
            nextStep { exitStep }
            routeSegment("address")
            parents {
                OrParents(
                    journey.dateOfBirthStep.hasOutcome(AgeCategory.ADULT),
                    journey.parentalConsentStep.isComplete()
                )
            }
        }
        exitStep {
            parents { journey.addressStep.isComplete() }
        }
    }
```

The `exitStep` is an internal step without an associated page. It defines the requirements for completing the task and where to go next.

## Journey State

Each journey has an associated `JourneyState` class that specifies:
- The possible steps and tasks in the journey
- Any additional data to persist between steps

Because each page is a separate HTTP request, the `JourneyState` is stored in the user's session between requests.

### State Interfaces

Define interfaces for tasks and journeys that specify the required steps and data:

```kotlin
interface PersonalDetailsState {
    val nameStep: NameStep
    val dateOfBirthStep: DateOfBirthStep
    // ... other steps and data
}
```

Journey state classes implement these interfaces, ensuring all required data is available.

### The JourneyStateDelegateProvider

Use the `JourneyStateDelegateProvider` to automatically persist properties to the session:

```kotlin
class SimpleJourney(
    override val delegateProvider: JourneyStateDelegateProvider,
    override val nameStep: NameStep,
    override val questStep: QuestStep,
    override val colourStep: ColourStep,
) : JourneyState, SimpleJourneyState {
    var changedMindAboutColour: Boolean? by delegateProvider.nullableDelegate("changedMind")
}
```

The `changedMindAboutColour` property is automatically persisted to the session using the key `"changedMind"`.

### Journey IDs and Metadata

The state is stored in the user's session using a journey ID passed as a query parameter on each request. Journey metadata can allow multiple journey IDs to refer to the same data (e.g., when changing answers from a check-your-answers page).

## Rendering and Form Handling

### Controller Methods

Define at least two controller methods for each journey:
- **GET**: Render pages
- **POST**: Handle form submissions

Each method calls a journey factory to create a map of route segments to [`StepLifecycleOrchestrator`](StepLifecycleOrchestrator.kt) instances, which wrap steps and handle the request lifecycle.

### Step Functions

Step functions are divided into three categories:

**Defined by the step:**
- `getStepSpecificContent()` — provides content for the template
- `chooseTemplate()` — selects which template to render

**Always the same:**
- Validation
- Persisting data to state
- Saving state to the database for session restoration

**Defined by the journey structure:**
- Determining if the step is visitable
- Determining where to redirect if not visitable
- Determining the next step or URL after completion

### Customisation

Override methods on the step class for custom functionality, including final form submission. Most lifecycle functions have `before` and `after` hooks for additional customisation.

## Testing

The journey framework has comprehensive test coverage across multiple test categories.

### Unit Tests

Unit tests for the framework live in `src/test/kotlin/uk/gov/communities/prsdb/webapp/journeys/`:

| Test File | Description |
|-----------|-------------|
| `JourneyStepTests.kt` | Tests for individual step behaviour and configuration |
| `StepConfigTests.kt` | Tests for step configuration classes |
| `StepLifecycleOrchestratorTest.kt` | Tests for the request lifecycle handling |
| `ParentageTests.kt` | Tests for parent/child relationships between steps |
| `TaskTests.kt` | Tests for task (step group) functionality |
| `AbstractJourneyStateTests.kt` | Tests for journey state persistence |
| `builders/JourneyBuilderTest.kt` | Tests for the DSL builder |

### Integration Tests

Integration tests for complete journeys live in `src/test/kotlin/uk/gov/communities/prsdb/webapp/integration/`. These use Playwright for end-to-end testing of journey flows.

Example: `LandlordRegistrationJourneyTests.kt`

### Test Utilities

- **`JourneyTestHelper`** (`src/test/kotlin/uk/gov/communities/prsdb/webapp/testHelpers/JourneyTestHelper.kt`): Helper class for setting up mock users in tests.

### Writing Tests for Your Journey

1. **Unit test individual steps**: Test step validation, mode determination, and content generation
2. **Test journey structure**: Verify parentage relationships and navigation paths
3. **Integration test the full flow**: Use Playwright page objects to test the complete user journey

## File Locations

### Framework Code

The core journey framework lives in `src/main/kotlin/uk/gov/communities/prsdb/webapp/journeys/`:

| File | Description |
|------|-------------|
| `JourneyStep.kt` | Core step class definition |
| `JourneyState.kt` | Base interface for journey state |
| `AbstractJourneyState.kt` | Base class for journey state implementations |
| `AbstractStepConfig.kt` | Base classes for step configuration |
| `JourneyStateDelegateProvider.kt` | Handles session persistence |
| `StepLifecycleOrchestrator.kt` | Manages request lifecycle for steps |
| `Task.kt` | Base class for reusable task groups |
| `Parentage.kt` | Defines parent/child relationships |
| `Destination.kt` | Navigation destination types |
| `builders/` | DSL builder classes |

### Journey Implementations

Each journey has its own subdirectory. For example, property registration:

```
src/main/kotlin/uk/gov/communities/prsdb/webapp/journeys/propertyRegistration/
├── NewPropertyRegistrationJourneyFactory.kt  # DSL journey definition
├── states/                                    # State interfaces and implementations
│   ├── OccupationState.kt
│   ├── LicensingState.kt
│   └── ...
├── steps/                                     # Step configuration classes
│   ├── PropertyTypeStepConfig.kt
│   ├── BedroomsStepConfig.kt
│   └── ...
└── tasks/                                     # Reusable task definitions
    ├── OccupationTask.kt
    ├── LicensingTask.kt
    └── ...
```

### Shared Components

Reusable step configurations and states live in `src/main/kotlin/uk/gov/communities/prsdb/webapp/journeys/shared/`:

- `stepConfig/` — Shared step configuration classes
- `states/` — Shared state interfaces
- `helpers/` — Utility classes

If you want to reuse steps from another journey, move them here first.

### Controllers

Journey controllers are in `src/main/kotlin/uk/gov/communities/prsdb/webapp/controllers/`. For example:
- `NewRegisterPropertyController.kt` — Handles property registration journey requests

## Implementation Notes

This section covers advanced topics for developers who need to extend or modify the framework.

### Adding a New Step

1. Create a step config class extending `AbstractRequestableStepConfig`:

```kotlin
@Component
class MyStepConfig : AbstractRequestableStepConfig<MyMode, MyFormModel, MyJourneyState>() {
    override val formModelClass = MyFormModel::class

    override fun getStepSpecificContent(state: MyJourneyState) = mapOf(
        "someData" to state.someValue
    )

    override fun chooseTemplate(state: MyJourneyState) = "templates/my-step"

    override fun mode(state: MyJourneyState): MyMode? =
        state.myAnswer?.let { MyMode.fromAnswer(it) }
}
```

2. Create a step instance in your journey state class
3. Reference the step in the journey DSL

### Creating a New Task

1. Create a task class extending `Task<TState>`:

```kotlin
class MyTask : Task<MyJourneyState>() {
    override fun makeSubJourney(state: MyJourneyState) = subJourney(state) {
        step(step1) {
            routeSegment("step1")
            initialStep()
            nextStep { step2 }
        }
        step(step2) {
            routeSegment("step2")
            parents { step1.isComplete() }
            nextStep { exitStep }
        }
        exitStep {
            parents { step2.isComplete() }
        }
    }
}
```

2. Create a state interface for the task if needed
3. Use the task in journey definitions with the `task()` DSL function

### Extending the DSL

The DSL is built using Kotlin builder patterns. Key extension points:

- **`JourneyBuilderDsl`** interface: Defines `step()` and `task()` functions
- **`StepInitialiser`**: Configures individual steps with `nextStep()`, `parents()`, `routeSegment()`, etc.
- **`TaskInitialiser`**: Configures tasks within journeys

To add new DSL functions, extend the relevant initialiser classes in `builders/`.

See: https://kotlinlang.org/docs/type-safe-builders.html

### Custom Lifecycle Orchestrators

The `StepLifecycleOrchestrator` sealed class has two implementations:
- `VisitableStepLifecycleOrchestrator` — For regular steps that render pages
- `RedirectingStepLifecycleOrchestrator` — For internal steps that redirect

To add custom behaviour, override the `getStepLifecycleOrchestrator()` method in your step config.

### Session Persistence

Journey state is persisted to the session via `JourneyStateDelegateProvider`. Key concepts:

- Use `delegateProvider.nullableDelegate("key")` for nullable properties
- Use `delegateProvider.requiredDelegate("key", defaultValue)` for non-nullable properties
- The delegate automatically saves to session on set and loads on get
