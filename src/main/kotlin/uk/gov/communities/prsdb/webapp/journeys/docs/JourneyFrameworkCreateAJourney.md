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

The mode enum defines the ways the step can affect journey structure.
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

This is a convenient way to implement logic relating to the journey structure without needing a separate HTTP request.
For example, if a user should be shown different pages depending on their previously recorded age, you could implement a `DateOfBirthStep` that calculates the user's age from their date of birth but does not have an associated page.

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

## Define a task

A task is a reusable group of steps with its own state. To create one:

1. Pick a base class:
    - `TaskWithoutDependencies<TState>` — if the task's logic depends only on answers collected inside
      the task itself.
    - `Task<TState, TDependencies>` — if the task needs values from the enclosing journey (e.g. "is the
      property occupied?"). Declare a small interface for what you need and take it as the second type
      parameter; the journey that mounts the task will supply the values.
2. Define a state interface for the task listing the steps and any extra persisted values it owns, and
   have the task implement it. Override `taskState` to return `this`.
3. Inject the task's steps (and any inner tasks) as constructor properties.
4. Implement `makeSubJourney(state)` using `subJourney(state) { … }` and the same DSL you use for
   journeys.
5. Register the class with `@JourneyFrameworkComponent` so it can be injected into whatever journey uses
   it.

When another element needs data from the task, read it through the task reference on the outer state
(e.g. `state.personalDetailsTask.nameStep`). Tasks don't share a common state interface with the journey
that hosts them.

### A `TaskWithoutDependencies` example

```kotlin
@JourneyFrameworkComponent
class PersonalDetailsTask(
    journeyStateService: JourneyStateService,
    override val nameStep: NameStep,
    override val dateOfBirthStep: DateOfBirthStep,
    override val parentalConsentStep: ParentalConsentStep,
    override val addressStep: AddressStep,
) : TaskWithoutDependencies<PersonalDetailsState>(journeyStateService),
    PersonalDetailsState {

    override val taskState get() = this

    override fun makeSubJourney(state: PersonalDetailsState) =
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
                        journey.parentalConsentStep.isComplete(),
                    )
                }
            }
            exitStep {
                parents { journey.addressStep.isComplete() }
            }
        }
}
```

The first step listed inside `subJourney` is the entry point users are sent to when they start the task,
and the `exitStep` — an internal step with no page — defines when the task counts as complete and where
the outer journey should continue from.

Extra values the task needs to persist (flags, IDs, working data) live on the task itself, using
`delegateProvider.nullableDelegate(...)` in the same way as on a journey state class.

### A `Task` with dependencies

If the task needs data owned by the enclosing scope (a journey or an outer task), declare a dependencies
interface and take it as the second type parameter. Inside the task's DSL you can read from it via
`dependencies`. The block that mounts the task supplies the values with `withDependencies { ... }`, so
the same task can be reused from different journeys with different sources.

```kotlin
interface GasSafetyDependencies {
    val isOccupied: Boolean
    val allowProvideCertificateLaterRoute: Boolean
}

@JourneyFrameworkComponent
class GasSafetyTask(
    journeyStateService: JourneyStateService,
    override val gasSafetyDetailsTask: GasSafetyDetailsTask,
    override val checkGasSafetyAnswersStep: CheckGasSafetyAnswersStep,
) : Task<GasSafetyState, GasSafetyDependencies>(journeyStateService),
    GasSafetyState {

    override val taskState get() = this

    override fun makeSubJourney(state: GasSafetyState) =
        subJourney(state) {
            task(journey.gasSafetyDetailsTask) {
                withDependencies { dependencies }   // forwarded to the inner task
                nextStep { journey.checkGasSafetyAnswersStep }
            }
            step(journey.checkGasSafetyAnswersStep) {
                routeSegment(CheckGasSafetyAnswersStep.ROUTE_SEGMENT)
                parents { journey.gasSafetyDetailsTask.isComplete() }
                nextStep { exitStep }
            }
            exitStep {
                parents { journey.checkGasSafetyAnswersStep.isComplete() }
            }
        }
}
```

If a task declares dependencies but `withDependencies { ... }` is missing at a mount site, the journey
factory throws `JourneyInitialisationException` at build time.

### Journey structure including a task

Tasks are added with the same `task { ... }` DSL block whether they have dependencies or not. Configure
them just like a step — `parents { }`, `nextStep { }` / `nextUrl { }` — and use `task.firstStep` to
redirect into the task from another element.

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
        parents { journey.exampleTask.isComplete() }
        routeSegment("check-answers")
    }
}
```

### Task route prefixes

A task can optionally carry its own URL segment, which is prepended to every requestable step inside it:

```kotlin
task(journey.exampleTask, routeSegment = "example") {
    parents { journey.colourStep.isComplete() }
    nextStep { journey.checkAnswersStep }
}
```

With this, a step whose `routeSegment("name")` is set inside the task is served at `example/name`, not
`name`. Nested tasks compose — mounting an inner task inside an outer task-with-route yields URLs like
`<outerTask>/<innerTask>/<stepRoute>` — and a landing redirect is registered at `/example` that
redirects to `task.firstStep`.

Delegate storage keys for values persisted on the task are prefixed with the same route, so two mounts
of the same reusable task under different routes have isolated state.

### Passing dependencies at the mount site

Tasks that declare a `TDependencies` type parameter must be given a provider inside the mount block:

```kotlin
task(journey.gasSafetyTask) {
    withDependencies { journey }                // typically the enclosing state, or an object built from it
    parents { journey.someEarlierStep.isComplete() }
    nextStep { journey.nextStep }
}
```

The provider is called once at build time. If the enclosing state already satisfies the dependencies
interface, pass it directly; otherwise, construct an inline object.

### Savable elements

Add `savable()` inside a `step { }` or `task { }` mount to mark it as a save-progress point. The
framework persists journey state to the database on completion of any savable element.

```kotlin
task(journey.gasSafetyDetailsTask) {
    withDependencies { dependencies }
    nextStep { journey.checkAnswersStep }
    savable()
}
```

## Embedding a task's steps directly with `fromTask`

Most of the time a task is added as a single block via `task(journey.someTask) { ... }`, and its
internal structure is fully defined by the task itself. Sometimes, though, you need to place individual
steps from a task directly into the outer journey — either wiring them up differently, or mounting only
one of them.

`fromTask(task) { ... }` opens an inner DSL block that has visibility of both scopes:

- `journey` — the outer journey's state (unchanged from the enclosing scope).
- `task` — the embedded task's state (its steps and members).

Inside the block you use the normal `step { }` / `task { }` DSL, but you reference the embedded task's
steps as `task.someStep` and can freely mix in references to the outer state as `journey.someStep`.
Steps added this way are registered on the outer routing map alongside everything else.

```kotlin
journey(state) {
    // ...
    fromTask(journey.ownershipAndLandlordsTask) {
        step(task.ownershipTypeStep) {
            routeSegment(OwnershipTypeStep.ROUTE_SEGMENT)
            parents { journey.propertyDetailsTask.propertyTypeStep.isComplete() }
            nextStep { journey.licensingTask.firstStep }
        }
    }
    // ...
}
```

If the embedded task declares dependencies, there is a second overload that binds them at the mount
site: `fromTask(task, dependencies) { ... }`.

## Check-your-answers sub-journeys

Journeys that let users change individual answers from a CYA page use a **second routing map** built
from the same journey state. This map contains only the step(s) relevant to the answer being changed and
finishes on a `finishCyaStep` that redirects back to the CYA page.

When the step being changed lives inside a task, `fromTask` gives access to that step so it can be
mounted on its own rather than by running through the whole task:

```kotlin
fromTask(journey.personalDetailsTask) {
    checkAnswerStep(task.nameStep, NameStep.ROUTE_SEGMENT)
}
```

For CYA journeys extending `CheckYourAnswersJourneyState`, the following extension helpers on
`JourneyBuilder` / `EmbedBuilder` wrap the boilerplate:

- `checkAnswerStep(step, route)` — mounts a single step as an initial step whose `nextStep` is
  `finishCyaStep`.
- `checkAnswerTask(task, route? = null)` and `checkAnswerTask(task, dependencies, route? = null)` —
  mounts a whole task as an initial task whose `nextStep` is `finishCyaStep`.

A typical CYA routing map dispatches on the answer being changed:

```kotlin
companion object {
    fun <T : PersonalDetailsState> checkYourAnswersJourneyMap(
        state: T,
        checkingAnswersFor: String,
    ): Map<String, StepLifecycleOrchestrator> =
        journey(state) {
            unreachableStepDestination { journey.returnToCyaPageDestination }
            configureFirst { backDestination { journey.returnToCyaPageDestination } }

            when (checkingAnswersFor) {
                NameStep.ROUTE_SEGMENT ->
                    fromTask(journey.personalDetailsTask) {
                        checkAnswerStep(task.nameStep, NameStep.ROUTE_SEGMENT)
                    }

                LookupAddressStep.ROUTE_SEGMENT ->
                    checkAnswerTask(journey.addressTask)
            }

            step(journey.finishCyaStep) {
                initialStep()
                nextDestination { Destination.Nowhere() }
            }
        }
}
```

The journey factory picks which routing map to serve based on `state.someTask.checkingAnswersFor`.

## Define the Journey State

Each journey has an associated `JourneyState` interface and implementing class that specifies:
- The steps and tasks that make up the journey
- Any additional data to persist between steps

Because each page is a separate HTTP request, the `JourneyState` is stored in the user's session between requests.

### State Interfaces

Define an interface listing every step and task the journey holds directly, plus any extra persisted
values:

```kotlin
interface SimpleJourneyState : JourneyState {
    val nameStep: NameStep
    val questStep: QuestStep
    val colourStep: ColourStep
    val personalDetailsTask: PersonalDetailsTask
    val changedMindAboutColour: Boolean?
}
```

Tasks are held by reference. When other code needs a value from a task, it reads it through the task
reference, e.g. `state.personalDetailsTask.nameStep`. This keeps each task independent and reusable
across journeys.

### AbstractJourneyState and the JourneyStateDelegateProvider

To create the journey state class, extend `AbstractJourneyState` and use the `delegateProvider` it
provides to persist additional properties to the session. Take every step and task as a constructor
property, and register the class as a `@JourneyFrameworkComponent` so Spring can inject them.

```kotlin
@JourneyFrameworkComponent
class SimpleJourney(
    override val nameStep: NameStep,
    override val questStep: QuestStep,
    override val colourStep: ColourStep,
    override val personalDetailsTask: PersonalDetailsTask,
    journeyStateService: JourneyStateService,
) : AbstractJourneyState(journeyStateService),
    SimpleJourneyState {

    override var changedMindAboutColour: Boolean? by delegateProvider.nullableDelegate("changedMind")
}
```

The `changedMindAboutColour` property is automatically persisted to the session using the key
`"changedMind"`. Anything the composed tasks persist lives on the task instances themselves and is
persisted independently.

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
- Prefer seed based generation where possible for base journeys

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

Because tasks can carry route prefixes, the step being requested may be several path segments long
(e.g. `personal-details/name`). Capture the whole thing as a wildcard path variable — `{*stepPath}` —
rather than a single `{stepName}` segment.

Controllers delegate all the routing-map lookup, journey initialisation, and base-path handling to
`JourneyStepDispatcher`, which:

- Looks the step up in the routing map returned by the journey factory.
- If the map lookup throws `NoSuchJourneyException`, initialises a new journey and redirects to the same
  step with the new journey ID.
- Records the journey's base path on the request so any URLs generated during the request are absolute.

```kotlin
@PrsdbController
@RequestMapping(REGISTER_PROPERTY_JOURNEY_ROUTE)
class RegisterPropertyController(
    private val propertyRegistrationJourneyFactory: PropertyRegistrationJourneyFactory,
) {
    @GetMapping("/{*stepPath}")
    fun getJourneyStep(
        @PathVariable stepPath: String,
        principal: Principal,
    ): ModelAndView =
        dispatchJourneyStep(stepPath, principal) { getStepModelAndView() }

    @PostMapping("/{*stepPath}")
    fun postJourneyData(
        @PathVariable stepPath: String,
        @RequestParam formData: FormData,
        principal: Principal,
    ): ModelAndView =
        dispatchJourneyStep(stepPath, principal) { postStepModelAndView(formData) }

    private fun dispatchJourneyStep(
        stepPath: String,
        principal: Principal,
        dispatch: StepLifecycleOrchestrator.() -> ModelAndView,
    ): ModelAndView =
        JourneyStepDispatcher.handleInitialisableRequest(
            stepPath,
            createRoutingMap = { propertyRegistrationJourneyFactory.createJourneySteps() },
            initialiseJourney = { propertyRegistrationJourneyFactory.initializeJourneyState(principal) },
            dispatch = dispatch,
        )
}
```

### Triggering re-initialisation on other exceptions

Some journeys need to fall back to a fresh state for exceptions other than `NoSuchJourneyException` —
for example, when an ID in the URL no longer matches the stored state. Pass `startNewJourneyOn` to opt
those exceptions in:

```kotlin
JourneyStepDispatcher.handleInitialisableRequest(
    stepPath,
    createRoutingMap = { factory.createJourneySteps() },
    initialiseJourney = { factory.initializeJourneyState(principal, propertyOwnershipId) },
    dispatch = dispatch,
    startNewJourneyOn = { it is PropertyOwnershipMismatchException },
)
```

### Journeys that cannot self-initialise

Some journeys (e.g. accepting an invitation) require an external trigger to create state and should
redirect to a well-known URL rather than silently re-initialising. Use `handleUninitialisableRequest`:

```kotlin
JourneyStepDispatcher.handleUninitialisableRequest(
    stepPath,
    createRoutingMap = { factory.createJourneySteps() },
    dispatch = dispatch,
    redirectOn = { it is InvalidInvitationException },
    getRedirect = { ModelAndView("redirect:$INVALID_INVITATION_ROUTE") },
)
```

## Testing

When adding a new journey, add integration tests to cover the journey.

### Integration Tests

Integration tests for complete journeys live in `src/test/kotlin/uk/gov/communities/prsdb/webapp/integration/`. These use Playwright for end-to-end testing of journey flows.

Example: `LandlordRegistrationJourneyTests.kt`

### Test Utilities

- **`JourneyTestHelper`** (`src/test/kotlin/uk/gov/communities/prsdb/webapp/testHelpers/JourneyTestHelper.kt`): Helper class for setting up mock users in tests.

## File Locations

### Framework Code

The core journey framework lives in `src/main/kotlin/uk/gov/communities/prsdb/webapp/journeys/`.
Some of these are base classes you will need to extend when creating a new journey, while others are internal classes that you can use but do not need to modify.

Base classes you will need to extend:

| File | Description |
|------|-------------|
| `AbstractJourneyState.kt` | Base class for journey state implementations |
| `AbstractStepConfig.kt` | Base classes for step configuration |
| `Task.kt` | Base class for reusable task groups |

Internal final classes that you can use but do not need to modify:

| File | Description |
|------|-------------|
| `JourneyStep.kt` | Core step class definition |
| `JourneyStateDelegateProvider.kt` | Handles session persistence |
| `StepLifecycleOrchestrator.kt` | Manages request lifecycle for steps |
| `Parentage.kt` | Defines parent/child relationships |
| `Destination.kt` | Navigation destination types |
| `builders/` | DSL builder classes |

### Journey Implementations

Each journey has its own subdirectory. For example, property registration:

```
src/main/kotlin/uk/gov/communities/prsdb/webapp/journeys/propertyRegistration/
├── PropertyRegistrationJourneyFactory.kt  # DSL journey definition
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
- `RegisterPropertyController.kt` — Handles property registration journey requests
