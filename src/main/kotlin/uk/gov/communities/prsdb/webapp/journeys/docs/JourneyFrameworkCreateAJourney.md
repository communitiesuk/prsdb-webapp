# Creating a New Journey with the Journey Framework
This document provides a guide for developers on how to create a new multi-page form journey using the Journey Framework.
It assumes that you are familiar with the model and terminology used in the framework.
For more information, see the [Journey Framework Model](JourneyFrameworkModel.md) document.

## How to create a New Journey
Conceptually, the process for creating a new journey involves the following steps:
1. Define the config and step classes for each page in your journey (or reuse existing ones)
    * Alternatively, define tasks for reusable groups of steps
2. Define the journey state interface and implementation
3. Define the journey structure using the DSL in a factory class
4. Add a function to initialise the journey state in the factory class
5. Add controller methods for handling requests
6. Write tests for your journey

I would recommend implementing the journey iteratively, starting with a single step and gradually adding more steps and complexity.
This will allow you to add specific state interfaces to each step when they need to reference other steps in the journey.

## Define Step Classes
The logic and data for a specific step is defined by a step configuration class that extends one of the base step config classes (e.g., `AbstractRequestableStepConfig`).
The shared logic for handling requests and rendering pages is implemented in the base `RequestableStep` class, which uses the config class to determine how to handle the step-specific logic.

### Type Parameters
This config class takes three type parameters:
- The mode enum for the step
- The form model class for the step
- The journey state interface for the step

The mode enum defines the possible outcomes for the step that affect journey structure.
This is not the same as the possible answers on the page; rather, it is derived from the user's answers.
For example, it might represent "does user's answer match our previous record?" with modes `MATCH` and `NO_MATCH` even if it is a free text field.
There are a few shared mode enums for common scenarios:
- `Complete` — The step only has one possible outcome (completed)
- `YesOrNo` — The step has a yes/no answer

The form model class defines the data structure for the form on the page.
It also defines primary validation rules for the form fields.

The journey state interface defines the data that the step needs to read from or write to the journey state other than its own form model.
For example, if the step needs to read an answer from a previous step to determine its mode, that previous step's form model must be accessible via the journey state interface.
This means that "stand alone" steps will normally depend directly on `JourneyState`, while steps that are tightly coupled will depend on a more specific state interface, e.g. one associated with a specific task.

### Required Overrides
The step config must define the following overrides:
- `formModelClass`: The KClass of the form model
    - This MUST match the second type parameter, e.g. `override val formModelClass = MyFormModel::class`
- `getStepSpecificContent()`: A function that returns a map of content specific to this step depending on the journey state
- `chooseTemplate()`: A function that returns the template name to render for this step depending on the journey state
    - If necessary, implement a new Thymeleaf template for the step
- `mode()`: A function that returns the (nullable) mode for this step depending on the journey state
    - If the step has not been completed yet, this should return `null`
    - Otherwise, it should return the appropriate enum value based on the user's answers

### Register the bean
Annotate the step config class with `@JourneyFrameworkComponent` to register it as a Spring bean.
Each bean must have a unique name, so if the step shares a name with one from another journey an explicit bean name must be set.

### Example Step Config
This is the Occupied step from the Property Registration journey, a simple step with static template and content and no additional customisation.

```kotlin

@JourneyFrameworkComponent
class OccupiedStepConfig : AbstractRequestableStepConfig<YesOrNo, OccupancyFormModel, OccupationState>() {
    override val formModelClass = OccupancyFormModel::class

    override fun getStepSpecificContent(state: OccupationState) =
        mapOf(
            "fieldSetHeading" to "forms.occupancy.fieldSetHeading",
            "fieldSetHint" to "forms.occupancy.fieldSetHint",
            "radioOptions" to
                listOf(
                    RadiosButtonViewModel(
                        value = true,
                        labelMsgKey = "forms.radios.option.yes.label",
                        hintMsgKey = "forms.occupancy.radios.option.yes.hint",
                    ),
                    RadiosButtonViewModel(
                        value = false,
                        labelMsgKey = "forms.radios.option.no.label",
                        hintMsgKey = "forms.occupancy.radios.option.no.hint",
                    ),
                ),
        )

    override fun chooseTemplate(state: OccupationState): String = "forms/propertyOccupancyForm"

    override fun mode(state: OccupationState): YesOrNo? =
        getFormModelFromStateOrNull(state)?.occupied?.let {
            when (it) {
                true -> YesOrNo.YES
                false -> YesOrNo.NO
            }
        }
}
```

### Additional overrides
Other than the content and template functions, there are a number of step lifecycle functions that are not controlled by the step config class.
They are divided into two categories:

**Always the same for all journey steps:**
- Primary validation (specific rules are defined on the form model)
- Persisting data to state
- Saving state to the database for session restoration

**Defined by the journey structure:**
- Determining if the step is visitable
- Determining where to redirect if not visitable
- Determining the next step or URL after completion

Most lifecycle functions have `before` and `after` hooks for additional customisation.
Override these hook functions on the config class for custom functionality, including final form submission.

For a full list of overridable functions, see the base step config class - [`AbstractStepConfig`](../AbstractStepConfig.kt).

### Define the JourneyStep
Create a subclass of `RequestableStep` using your step config class as the StepConfig and register it as a `@JourneyFrameworkComponent`.

```kotlin
@JourneyFrameworkComponent
final class OccupiedStep(
    stepConfig: OccupiedStepConfig,
) : RequestableStep<YesOrNo, OccupancyFormModel, OccupationState>(stepConfig)
```

### Internal steps
Some steps may not have an associated page, but still require validation and parentage rules.
For these steps, create a subclass of `Step` instead of `RequestableStep` and implement the necessary lifecycle functions directly on the class.

This is a convenient way to implement logic relating to the journey or task structure without needing a separate HTTP request.
For example, if a user should be shown different pages depending on their previously recorded age, you could implement a `DateOfBirthStep` that calculates the user's age from their date of birth but does not have an associated page.

### Define a task
Create a subclass of `Task` and override `makeSubJourney` with the internal structure:

```kotlin
override fun makeSubJourney(state: OccupationState) =
    subJourney(state) {
        step(journey.nameStep) {
            nextStep { journey.dateOfBirthStep }
            routeSegment("name")
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

The first step listed in the task must be the "entry point" step, which users will be directed to when they start the task.

The `exitStep` is an internal step without an associated page.
It defines the requirements for completing the task and where to go next.

## Define the Journey State

Each journey has an associated `JourneyState` interface and implementing class that specifies:
- The possible steps and tasks in the journey
- Any additional data to persist between steps

Because each page is a separate HTTP request, the `JourneyState` is stored in the user's session between requests.

### State Interfaces

Define interfaces for tasks and journeys that specify the required steps and data:

```kotlin
interface PersonalDetailsState : JourneyState {
    val nameStep: NameStep
    val dateOfBirthStep: DateOfBirthStep
    // ... other steps and data
}
```
It is generally a good idea to define smaller interfaces for tasks that can be composed into larger interfaces for journeys.

```kotlin
interface OtherDetailsState : JourneyState {
    val favouriteColourStep: ColourStep
    val favouriteQuestStep: QuestStep
    // ... other steps and data
}

interface CombinedJourneyState : PersonalDetailsState, OtherDetailsState {
    val additionalStep: AdditionalStep
}
```

Journey state classes implement these interfaces, ensuring all required data is available.

### AbstractJourneyState and the JourneyStateDelegateProvider

To create the journey state class, extend `AbstractJourneyState` and use the `JourneyStateDelegateProvider` to automatically persist additional properties to the session:
Add all the necessary steps and tasks as `val` properties in the class constructor.
Register the class as a `@JourneyFrameworkComponent` to ensure they are all injected.

```kotlin
interface SimpleJourneyState : JourneyState {
    val nameStep: NameStep
    val questStep: QuestStep
    val colourStep: ColourStep
    val changedMindAboutColour: Boolean?
}

@JourneyFrameworkComponent
class SimpleJourney(
    override val delegateProvider: JourneyStateDelegateProvider,
    override val nameStep: NameStep,
    override val questStep: QuestStep,
    override val colourStep: ColourStep,
) : AbstractJourneyState, SimpleJourneyState {
    var changedMindAboutColour: Boolean? by delegateProvider.nullableDelegate("changedMind")
}
```

The `changedMindAboutColour` property is automatically persisted to the session using the key `"changedMind"`.

## Defining Journey Structure

The journey structure is defined using a Kotlin DSL. For each element, you define:
- Where the user goes when completing the element (for each mode)
- What previous elements must be completed for this element to be visitable (parents)
- Where to redirect if the element is not visitable
- The URL route segment for the element (if applicable)

### Basic Structure

```kotlin
val state = stateFactory.getObject()
val simpleJourney = journey(state) {
    unreachableStepStep { journey.nameStep }
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
        nextStep { journey.exampleTask.firstStep }
        routeSegment("colour")
        parents { journey.questStep.isComplete() }
    }
    task(journey.exampleTask) {
        parents { journey.colourStep.isComplete() }
        nextStep { journey.checkAnswersStep }
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

## Initialising Journey State

When a user first accesses a journey, the controller must initialise a new journey state and generate a unique journey ID. This ID becomes part of the URL and allows the session to be restored if the user returns later.

### Generating Journey IDs

Journey IDs are generated using the `generateJourneyId` method on `JourneyState`. There are two approaches:

**Seed-based generation** (preferred):
- A given seed will always produce the same journey ID
- Generates a 6-character alphanumeric string based on a hash of the seed
- By constructing the seed manually, you can control whether IDs are stable or unique:
    - **Stable**: Use a seed based only on an entity (e.g. `"Journey for user ${user.name}"` — the same user always gets the same ID)
    - **Unique**: Include a timestamp or random element in the seed (e.g. `"Journey for user ${user.name} at time ${System.currentTimeMillis()}"` — each invocation creates a new ID)

**Random generation** (default when no seed is provided):
- Generates a 7-character alphanumeric string
- Creates a unique ID on every call
- Only suitable when journeys should never be resumed

### Overriding `generateJourneyId`

Override `generateJourneyId` in your journey state class to:
1. **Type-check the base seed** — Cast to the expected type and handle unexpected seeds gracefully (by passing `null` to the super method)
2. **Create a journey-specific string** — Use string interpolation with the base seed to ensure different journeys produce different IDs even for the same base seed

```kotlin
override fun generateJourneyId(seed: Any?): String {
    val user = seed as? Principal
    return super.generateJourneyId(user?.let { generateSeedForUser(it) })
}

companion object {
    fun generateSeedForUser(user: Principal): String =
        "Property registration journey for user ${user.name} at time ${System.currentTimeMillis()}"
}
```

The journey-specific interpolable string ensures that the hash differs from other journeys that might use the same `Principal`.
Without this, two journeys seeded with the same user would produce identical IDs.

### Initialisation Methods

Add an initialisation function to your journey factory that creates the state and returns the journey ID:

```kotlin
fun initializeJourneyState(user: Principal): String =
    stateFactory.getObject().initializeState(user)
```

## Add Controller Methods

Define at least two controller methods for each journey:
- **GET**: Render pages
- **POST**: Handle form submissions

Capture the route segment as a path variable to identify the step being requested.

Each method calls a journey factory to create a map of route segments to [`StepLifecycleOrchestrator`](StepLifecycleOrchestrator.kt) instances, which wrap steps and handle the request lifecycle.

You must catch `NoSuchJourneyException` to handle the case where the user has not yet initialised a journey.
You should then call your initialisation method to create a new journey state and redirect the user to the same step with the new journey ID.

```kotlin

    @GetMapping("/{stepName}")
    @AvailableWhenFeatureEnabled(MIGRATE_PROPERTY_REGISTRATION)
    fun getJourneyStep(
        @PathVariable("stepName") stepName: String,
        principal: Principal,
    ): ModelAndView =
        try {
            val journeyMap = propertyRegistrationJourneyFactory.createJourneySteps()
            journeyMap[stepName]?.getStepModelAndView()
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Step not found")
        } catch (_: NoSuchJourneyException) {
            val journeyId = propertyRegistrationJourneyFactory.initializeJourneyState(principal)
            val redirectUrl = JourneyStateService.urlWithJourneyState(stepName, journeyId)
            ModelAndView("redirect:$redirectUrl")
        }

    @PostMapping("/{stepName}")
    @AvailableWhenFeatureEnabled(MIGRATE_PROPERTY_REGISTRATION)
    fun postJourneyData(
        @PathVariable("stepName") stepName: String,
        @RequestParam formData: PageData,
        principal: Principal,
    ): ModelAndView =
        try {
            val journeyMap = propertyRegistrationJourneyFactory.createJourneySteps()
            journeyMap[stepName]?.postStepModelAndView(formData)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Step not found")
        } catch (_: NoSuchJourneyException) {
            val journeyId = propertyRegistrationJourneyFactory.initializeJourneyState(principal)
            val redirectUrl = JourneyStateService.urlWithJourneyState(stepName, journeyId)
            ModelAndView("redirect:$redirectUrl")
        }
```

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
